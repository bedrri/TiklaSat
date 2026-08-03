package com.gib.tiklasat.entity;

import com.gib.tiklasat.support.InetJdbcType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Yenileme token'ı (Refresh Token).
 * BR-S-008: Token'ın kendisi DEĞİL, SHA-256 özeti saklanır.
 * Veritabanı sızarsa bile saldırgan token'ları doğrudan kullanamaz.
 *
 * Token rotasyonu: Kullanılan token revoke edilir, yeni token üretilir,
 * replaced_by_id ile zincir kurulur. Eski token tekrar sunulursa
 * → hırsızlık tespiti yapılır, tüm zincir iptal edilir.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_refresh_tokens_user"))
    private User user;

    /** SHA-256 hex özeti. Asla ham token saklanmaz. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Token revoke edildiyse zaman damgası. NULL = aktif. */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /** Rotasyon zinciri — bu token kullanıldıktan sonra hangi token üretildi? */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_id",
                foreignKey = @ForeignKey(name = "fk_refresh_tokens_replaced_by"))
    private RefreshToken replacedBy;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @JdbcType(InetJdbcType.class)
    @Column(name = "ip_address", columnDefinition = "INET")
    private String ipAddress;

    // ── Yardımcı metotlar ────────────────────────────

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive() {
        return !isExpired() && !isRevoked();
    }
}
