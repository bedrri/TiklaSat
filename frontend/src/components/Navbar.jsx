import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Gavel, Search, Bell, Menu, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <nav className="sticky top-0 z-50 border-b border-ink-700/60 bg-ink-950/90 backdrop-blur-md">
      <div className="mx-auto flex h-20 max-w-7xl items-center justify-between gap-6 px-4 sm:px-6 lg:px-8">
        {/* Wordmark */}
        <Link to="/" className="flex shrink-0 items-center gap-2.5">
          <span className="flex h-9 w-9 items-center justify-center rounded-full border border-gold-500/40 text-gold-400">
            <Gavel size={18} strokeWidth={2} />
          </span>
          <span className="font-display text-2xl italic tracking-tight text-paper">
            Tıkla<span className="text-gold-400">Sat</span>
          </span>
        </Link>

        {/* Search (desktop) */}
        <div className="hidden max-w-md flex-1 md:block">
          <div className="group relative">
            <Search
              size={16}
              className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-ink-500 transition-colors group-focus-within:text-gold-500"
            />
            <input
              type="text"
              placeholder="Lot ara — örn. iPhone 15"
              className="w-full rounded-full border border-ink-600 bg-ink-900 py-2.5 pl-10 pr-4
                         font-sans text-sm text-paper placeholder:text-ink-500
                         transition-colors focus:border-gold-500/60 focus:outline-none"
            />
          </div>
        </div>

        {/* Right actions */}
        <div className="flex items-center gap-2 sm:gap-4">
          <button
            type="button"
            aria-label="Bildirimler"
            className="relative rounded-full p-2 text-ink-400 transition-colors hover:bg-ink-800 hover:text-paper"
          >
            <Bell size={19} />
            <span className="absolute right-1.5 top-1.5 block h-2 w-2 rounded-full bg-ember-500 ring-2 ring-ink-950" />
          </button>

          {isAuthenticated ? (
            <div className="hidden items-center gap-3 border-l border-ink-700 pl-4 sm:flex">
              <span className="font-sans text-sm text-paper/80">
                Hoş geldin, <span className="font-semibold text-paper">{user.fullName.split(' ')[0]}</span>
              </span>
              <button type="button" onClick={handleLogout} className="btn-outline !px-4 !py-2 text-sm">
                Çıkış Yap
              </button>
            </div>
          ) : (
            <div className="hidden items-center gap-3 border-l border-ink-700 pl-4 sm:flex">
              <Link to="/giris" className="font-sans text-sm font-medium text-paper/80 transition-colors hover:text-paper">
                Giriş Yap
              </Link>
              <Link to="/kayit" className="btn-primary !px-5 !py-2 text-sm">
                Kayıt Ol
              </Link>
            </div>
          )}

          <button
            type="button"
            onClick={() => setMobileOpen((v) => !v)}
            aria-label="Menü"
            className="rounded-lg p-2 text-paper sm:hidden"
          >
            {mobileOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </div>
      </div>

      {mobileOpen && (
        <div className="flex flex-col gap-3 border-t border-ink-700 bg-ink-950 px-4 py-4 sm:hidden">
          {isAuthenticated ? (
            <>
              <span className="font-sans text-sm text-paper/70">Hoş geldin, {user.fullName}</span>
              <button type="button" onClick={handleLogout} className="btn-outline w-full text-sm">
                Çıkış Yap
              </button>
            </>
          ) : (
            <>
              <Link to="/giris" onClick={() => setMobileOpen(false)} className="btn-outline w-full text-sm">
                Giriş Yap
              </Link>
              <Link to="/kayit" onClick={() => setMobileOpen(false)} className="btn-primary w-full text-sm">
                Kayıt Ol
              </Link>
            </>
          )}
        </div>
      )}
    </nav>
  );
};

export default Navbar;
