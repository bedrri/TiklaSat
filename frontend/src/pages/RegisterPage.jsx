import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Gavel, ArrowRight } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const initialForm = { fullName: '', email: '', password: '', phone: '' };

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function update(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="relative flex min-h-[calc(100vh-5rem)] items-center justify-center overflow-hidden px-4 py-16">
      <div className="grain-overlay" />
      <div
        className="pointer-events-none absolute inset-0"
        style={{
          background: 'radial-gradient(50% 40% at 50% 0%, rgba(200,154,61,0.12) 0%, transparent 70%)',
        }}
      />

      <div className="ticket relative w-full max-w-md p-8 sm:p-10">
        <div className="ticket-perforation" />

        <div className="flex items-center gap-2 pt-2 text-ink-950">
          <span className="flex h-9 w-9 items-center justify-center rounded-full border border-gold-600/40 text-gold-600">
            <Gavel size={17} />
          </span>
          <span className="lot-tag !text-ink-950/50">Üyelik bileti</span>
        </div>

        <h1 className="mt-6 font-display text-3xl text-ink-950">Paddle'ını al</h1>
        <p className="mt-2 font-sans text-sm text-ink-950/55">
          Kayıt ol, ilan ver ya da ilk teklifini bugün ver.
        </p>

        <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-5">
          <label className="block">
            <span className="mb-1.5 block font-sans text-xs font-medium uppercase tracking-wide text-ink-950/50">
              Ad Soyad
            </span>
            <input
              type="text"
              required
              maxLength={100}
              autoComplete="name"
              value={form.fullName}
              onChange={update('fullName')}
              placeholder="Ayşe Yılmaz"
              className="w-full rounded-xl border border-paper-line bg-white/60 px-4 py-3 font-sans text-ink-950
                         placeholder:text-ink-950/30 focus:border-gold-600 focus:outline-none focus:ring-2 focus:ring-gold-600/20"
            />
          </label>

          <label className="block">
            <span className="mb-1.5 block font-sans text-xs font-medium uppercase tracking-wide text-ink-950/50">
              E-posta
            </span>
            <input
              type="email"
              required
              autoComplete="email"
              value={form.email}
              onChange={update('email')}
              placeholder="ayse@mail.com"
              className="w-full rounded-xl border border-paper-line bg-white/60 px-4 py-3 font-sans text-ink-950
                         placeholder:text-ink-950/30 focus:border-gold-600 focus:outline-none focus:ring-2 focus:ring-gold-600/20"
            />
          </label>

          <label className="block">
            <span className="mb-1.5 block font-sans text-xs font-medium uppercase tracking-wide text-ink-950/50">
              Telefon
            </span>
            <input
              type="tel"
              required
              maxLength={20}
              autoComplete="tel"
              value={form.phone}
              onChange={update('phone')}
              placeholder="05xx xxx xx xx"
              className="w-full rounded-xl border border-paper-line bg-white/60 px-4 py-3 font-sans text-ink-950
                         placeholder:text-ink-950/30 focus:border-gold-600 focus:outline-none focus:ring-2 focus:ring-gold-600/20"
            />
          </label>

          <label className="block">
            <span className="mb-1.5 block font-sans text-xs font-medium uppercase tracking-wide text-ink-950/50">
              Parola
            </span>
            <input
              type="password"
              required
              minLength={8}
              maxLength={128}
              autoComplete="new-password"
              value={form.password}
              onChange={update('password')}
              placeholder="En az 8 karakter"
              className="w-full rounded-xl border border-paper-line bg-white/60 px-4 py-3 font-sans text-ink-950
                         placeholder:text-ink-950/30 focus:border-gold-600 focus:outline-none focus:ring-2 focus:ring-gold-600/20"
            />
          </label>

          {error && (
            <p className="rounded-lg bg-ember-500/10 px-3 py-2 font-sans text-sm text-ember-600">{error}</p>
          )}

          <button type="submit" disabled={loading} className="btn-primary mt-1 w-full disabled:opacity-60">
            {loading ? 'Kayıt oluşturuluyor…' : 'Kayıt Ol'}
            {!loading && <ArrowRight size={18} />}
          </button>
        </form>

        <p className="mt-7 border-t border-dashed border-paper-line pt-5 text-center font-sans text-sm text-ink-950/55">
          Zaten üye misin?{' '}
          <Link to="/giris" className="font-semibold text-gold-600 hover:underline">
            Giriş yap
          </Link>
        </p>
      </div>
    </main>
  );
}
