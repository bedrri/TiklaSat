import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Gavel } from 'lucide-react';

function useCountdown(initialSeconds) {
  const [seconds, setSeconds] = useState(initialSeconds);

  useEffect(() => {
    const id = setInterval(() => {
      setSeconds((s) => (s > 0 ? s - 1 : initialSeconds));
    }, 1000);
    return () => clearInterval(id);
  }, [initialSeconds]);

  const m = Math.floor(seconds / 60).toString().padStart(2, '0');
  const s = (seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}

const STATS = [
  { value: '120sn', label: 'Sniper koruma penceresi' },
  { value: '<10ms', label: 'Teklif kilit süresi' },
  { value: '%100', label: 'Admin onaylı ilan' },
];

const Hero = () => {
  const countdown = useCountdown(97);

  return (
    <section className="relative overflow-hidden">
      <div className="grain-overlay" />
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background:
            'radial-gradient(60% 50% at 78% 15%, rgba(200,154,61,0.14) 0%, transparent 70%)',
        }}
      />

      <div className="relative mx-auto max-w-7xl px-4 pb-20 pt-16 sm:px-6 lg:px-8 lg:pt-24">
        <div className="grid items-center gap-14 lg:grid-cols-[1.15fr_0.85fr] lg:gap-10">
          {/* Left — mesaj */}
          <div>
            <div className="inline-flex items-center gap-2 border-b border-ember-500/40 pb-1 font-mono text-xs uppercase tracking-[0.25em] text-ember-400">
              <span className="relative flex h-1.5 w-1.5">
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-ember-500 opacity-75" />
                <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-ember-500" />
              </span>
              Canlı müzayede
            </div>

            <h1 className="mt-6 font-display text-5xl leading-[1.05] tracking-tight text-paper sm:text-6xl lg:text-[4.2rem]">
              Son teklif,
              <br />
              <span className="italic text-gold-400">son saniyede</span> verilir.
            </h1>

            <p className="mt-6 max-w-lg font-sans text-lg leading-relaxed text-paper/60">
              TıklaSat'ta her açık artırma bir yarış. Kapanışa saniyeler kala gelen
              teklif süreyi otomatik uzatır — kimse haksız yere pas geçilmez.
            </p>

            <div className="mt-9 flex flex-col gap-3 sm:flex-row">
              <Link to="/kayit" className="btn-primary">
                Hemen Katıl
                <ArrowRight size={18} />
              </Link>
              <Link to="/giris" className="btn-outline">
                Giriş Yap
              </Link>
            </div>

            <dl className="mt-14 flex flex-wrap gap-x-10 gap-y-5 border-t border-ink-700 pt-8">
              {STATS.map((stat) => (
                <div key={stat.label}>
                  <dt className="font-mono text-2xl text-gold-400">{stat.value}</dt>
                  <dd className="mt-1 font-sans text-xs text-paper/45">{stat.label}</dd>
                </div>
              ))}
            </dl>
          </div>

          {/* Right — canlı lot bileti */}
          <div className="relative lg:justify-self-end">
            <div className="ticket w-full max-w-sm p-6 sm:p-7">
              <div className="ticket-perforation" />

              <div className="flex items-center justify-between pt-2">
                <span className="lot-tag">Lot № 0231</span>
                <span className="flex items-center gap-1.5 rounded-full bg-ember-500/10 px-2.5 py-1 font-mono text-[11px] uppercase tracking-wider text-ember-600">
                  <span className="h-1.5 w-1.5 rounded-full bg-ember-500" />
                  Açık
                </span>
              </div>

              <div className="mt-5 flex h-40 items-center justify-center rounded-2xl border border-ink-950/10 bg-gradient-to-br from-ink-800 to-ink-950">
                <Gavel size={40} className="text-gold-400/70" strokeWidth={1.5} />
              </div>

              <h3 className="mt-5 font-display text-xl text-ink-950">1965 Model Kronograf Saat</h3>
              <p className="mt-1 font-sans text-sm text-ink-950/50">28 teklif · İstanbul</p>

              <div className="mt-5 flex items-end justify-between border-t border-dashed border-paper-line pt-5">
                <div>
                  <p className="font-sans text-[11px] uppercase tracking-wider text-ink-950/40">Güncel teklif</p>
                  <p className="font-mono text-3xl text-ink-950">₺14.250</p>
                </div>
                <div className="text-right">
                  <p className="font-sans text-[11px] uppercase tracking-wider text-ink-950/40">Kapanışa</p>
                  <p className="font-mono text-2xl text-ember-600">{countdown}</p>
                </div>
              </div>

              <Link to="/kayit" className="btn-primary mt-6 w-full">
                Teklif Vermek İçin Katıl
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default Hero;
