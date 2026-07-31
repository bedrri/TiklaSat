package com.gib.tiklasat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gib.tiklasat.dto.auction.AuctionCreateRequest;
import com.gib.tiklasat.dto.auction.AuctionDto;
import com.gib.tiklasat.dto.auction.BidRequest;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.AuctionExtension;
import com.gib.tiklasat.entity.Bid;
import com.gib.tiklasat.entity.BidIncrementTier;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.OutboxEvent;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.AuctionExtensionRepository;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidIncrementTierRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.repository.OutboxEventRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final AuctionExtensionRepository extensionRepository;
    private final BidIncrementTierRepository bidIncrementTierRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    private static final int SNIPER_WINDOW_SECONDS = 120; // Son 2 dakika
    private static final int EXTENSION_SECONDS = 120; // 2 dakika uzat
    private static final int MAX_EXTENSIONS = 20;
    private static final int HARD_CAP_MINUTES = 60; // BR-A-006 · mutlak tavan, bağımsız güvenlik ağı

    /**
     * İlan için yeni bir açık artırma başlatır.
     */
    @Transactional
    public AuctionDto createAuction(AuctionCreateRequest request, String sellerEmail) {
        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new IllegalArgumentException("İlan bulunamadı"));

        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new IllegalStateException("Sadece kendi ilanınızı açık artırmaya çıkarabilirsiniz");
        }

        Auction auction = new Auction();
        auction.setListing(listing);
        auction.setStartPrice(request.getStartPrice());
        auction.setCurrentPrice(request.getStartPrice());
        auction.setReservePrice(request.getReservePrice());
        auction.setCurrency("TRY");
        auction.setBidCount(0);
        auction.setStartsAt(Instant.now());
        auction.setOriginalEndsAt(request.getEndsAt());
        auction.setEndsAt(request.getEndsAt());
        auction.setExtensionCount((short) 0);
        auction.setStatus("ACTIVE");

        auction = auctionRepository.save(auction);
        
        // İlan durumunu da güncelle
        listing.setStatus("APPROVED");
        listing.setPublishedAt(Instant.now());
        listingRepository.save(listing);

        return mapToDto(auction);
    }

    /**
     * Teklif Verme Motoru (PESSIMISTIC_WRITE Kilitli)
     * Veritabanı seviyesinde satırı kilitler. İki kişi aynı anda aynı müzayedeye
     * teklif vermeye çalışırsa, biri beklemek zorundadır.
     */
    @Transactional
    public void placeBid(BidRequest request, String bidderEmail, String ipAddress, String userAgent) {
        User bidder = userRepository.findByEmail(bidderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        // PESİMİSTİK KİLİT - Açık artırmayı getir ve satırı KİLİTLE!
        // Aynı anda sadece 1 thread bu kodu geçebilir.
        Auction auction = auctionRepository.findByIdForUpdate(request.getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("Açık artırma bulunamadı"));

        // Tek bir "şimdi" referansı: transaction boyunca aynı ana anlamlı kalsın diye
        // Instant.now() tekrar tekrar çağrılmaz, tek noktada okunur.
        Instant now = Instant.now();

        // Kurallar
        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new IllegalStateException("Bu açık artırma aktif değil (Durum: " + auction.getStatus() + ")");
        }

        if (now.isAfter(auction.getEndsAt())) {
            throw new IllegalStateException("Açık artırma süresi dolmuş!");
        }

        if (auction.getListing().getSeller().getId().equals(bidder.getId())) {
            throw new IllegalStateException("Kendi ürününüze teklif veremezsiniz!");
        }

        // BR-B-003 · İlk teklif start_price'ın (= mevcut fiyatın) üzerinde olmalı;
        // sonraki her teklif, bid_increment_tiers tablosundan okunan kademeli
        // artış kadar fazla olmalı. Artış kodda SABİT değildir.
        BigDecimal minRequiredBid = auction.getCurrentPrice();
        if (auction.getBidCount() > 0) {
            minRequiredBid = calculateMinimumNextBid(auction.getCurrentPrice());
        }

        if (request.getAmount().compareTo(minRequiredBid) < 0) {
            throw new IllegalArgumentException("Teklifiniz çok düşük! Minimum verilmesi gereken teklif: " + minRequiredBid);
        }

        // Teklifi oluştur
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(request.getAmount());
        bid.setMaxAmount(request.getMaxAmount()); // Opsiyonel proxy bid limit
        bid.setIsProxy(false);
        bid.setStatus("WINNING");
        bid.setIpAddress(ipAddress);
        bid.setUserAgent(userAgent);

        // Önceki teklifleri OUTBID (Geçildi) yap
        UUID previousBidderId = null;
        if (auction.getHighestBid() != null) {
            Bid previousHighest = auction.getHighestBid();
            previousHighest.setStatus("OUTBID");
            previousBidderId = previousHighest.getBidder().getId();
            bidRepository.save(previousHighest);
        }

        bid = bidRepository.save(bid);

        // Açık artırmayı güncelle
        auction.setCurrentPrice(bid.getAmount());
        auction.setBidCount(auction.getBidCount() + 1);
        auction.setHighestBid(bid);

        // BR-A-006 · Anti-Sniper Koruması.
        // ⚠ FORMÜL KRİTİK: "şimdi + 120sn" kullanılır, "eski bitiş + 120sn" DEĞİL.
        // İkincisi, art arda gelen teklifler arasında sadece birkaç saniye olsa
        // bile bitişi 2'şer dakika ileri atar; süre teklif sayısıyla şişer.
        // "Şimdi + 120sn" ise değişmez bir garanti verir: son teklifin üzerinden
        // 120 saniye geçmeden artırma bitmez (business-rules.md §0 D-2).
        boolean extensionApplied = false;
        long secondsLeft = ChronoUnit.SECONDS.between(now, auction.getEndsAt());
        if (secondsLeft >= 0 && secondsLeft <= SNIPER_WINDOW_SECONDS
                && auction.getExtensionCount() < MAX_EXTENSIONS) {

            Instant previousEndsAt = auction.getEndsAt();
            Instant newEndsAt = now.plusSeconds(EXTENSION_SECONDS);

            // Mutlak tavan: orijinal bitişten HARD_CAP_MINUTES'tan fazla uzayamaz.
            // Bugünkü parametrelerle (120sn pencere/uzatma, 20 sayaç) bu satıra
            // pratikte hiç ulaşılmaz (gerçek tavan ~40dk'da dolar); pencere veya
            // uzatma süresi ileride büyütülürse diye konmuş bağımsız bir güvenlik
            // ağıdır — ck_auctions_extension_window kısıtıyla veritabanında da
            // zorlanır (ADR-0006).
            Instant hardCap = auction.getOriginalEndsAt().plus(HARD_CAP_MINUTES, ChronoUnit.MINUTES);
            if (newEndsAt.isAfter(hardCap)) {
                newEndsAt = hardCap;
            }

            if (newEndsAt.isAfter(previousEndsAt)) {
                auction.setEndsAt(newEndsAt);
                auction.setExtensionCount((short) (auction.getExtensionCount() + 1));
                extensionApplied = true;

                AuctionExtension extension = new AuctionExtension();
                extension.setAuction(auction);
                extension.setBid(bid);
                extension.setExtensionNo(auction.getExtensionCount());
                extension.setPreviousEndsAt(previousEndsAt);
                extension.setNewEndsAt(newEndsAt);
                extensionRepository.save(extension);
            }
        }

        auctionRepository.save(auction);

        // BR-N-007 · WebSocket yayını BURADA yapılmaz. Doğrudan yayın yapılsaydı,
        // transaction geri alındığında kullanıcılar var olmayan bir teklifi görebilir
        // ya da yayın kaybolursa ekranlar donuk kalabilirdi. Olay bunun yerine AYNI
        // transaction içinde outbox tablosuna yazılır; OutboxPublisherJob onu ayrı
        // bir işlemde okuyup WebSocket'e yayınlar (concurrency-design.md §7).
        appendBidPlacedEvent(auction, bid, extensionApplied);
        if (previousBidderId != null) {
            appendUserOutbidEvent(previousBidderId, auction, bid);
        }
    }

    private void appendBidPlacedEvent(Auction auction, Bid bid, boolean extensionApplied) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("AUCTION");
        event.setAggregateId(auction.getId());
        event.setEventType("BID_PLACED");
        event.setPayload(toJson(Map.of(
                "auctionId", auction.getId().toString(),
                "bidId", bid.getId().toString(),
                "amount", bid.getAmount(),
                "currentPrice", auction.getCurrentPrice(),
                "bidCount", auction.getBidCount(),
                "endsAt", auction.getEndsAt().toString(),
                "extended", extensionApplied)));
        outboxEventRepository.save(event);
    }

    private void appendUserOutbidEvent(UUID previousBidderId, Auction auction, Bid bid) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("USER");
        event.setAggregateId(previousBidderId);
        event.setEventType("USER_OUTBID");
        event.setPayload(toJson(Map.of(
                "auctionId", auction.getId().toString(),
                "newAmount", bid.getAmount())));
        outboxEventRepository.save(event);
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox payload serileştirilemedi", e);
        }
    }

    /**
     * BR-B-003 · Minimum sonraki teklif = mevcut fiyat + kademe artışı.
     * Kademeler VERİTABANINDAN okunur (bid_increment_tiers) — kodda sabit değildir.
     */
    private BigDecimal calculateMinimumNextBid(BigDecimal currentPrice) {
        BidIncrementTier tier = bidIncrementTierRepository.findMatchingTier(currentPrice)
                .orElseThrow(() -> new IllegalStateException(
                        "Fiyat için artış kademesi bulunamadı: " + currentPrice));

        BigDecimal increment = "PERCENTAGE".equals(tier.getIncrementType())
                ? currentPrice.multiply(tier.getIncrementValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.CEILING)
                : tier.getIncrementValue();

        // BR-B-003 · yukarı yuvarlama: kullanıcıya gösterilen minimum tutar
        // gerçek minimumun altında kalmasın.
        return currentPrice.add(increment).setScale(2, RoundingMode.CEILING);
    }

    public AuctionDto getAuction(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Açık artırma bulunamadı"));
        return mapToDto(auction);
    }

    private AuctionDto mapToDto(Auction auction) {
        boolean reserveMet = true;
        if (auction.getReservePrice() != null) {
            reserveMet = auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0;
        }

        return AuctionDto.builder()
                .id(auction.getId())
                .listingId(auction.getListing().getId())
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .bidCount(auction.getBidCount())
                .startsAt(auction.getStartsAt())
                .endsAt(auction.getEndsAt())
                .status(auction.getStatus())
                .reservePriceMet(reserveMet) // Gizli taban fiyata ulaşıldı mı bilgisi
                .build();
    }
}
