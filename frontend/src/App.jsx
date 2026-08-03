import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Hero from './components/Hero'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'

function HomePage() {
  return (
    <>
      <Hero />
      {/* Gelecek güncellemelerde Canlı Müzayedeler kartları buraya eklenecek */}
    </>
  )
}

function App() {
  return (
    <div className="flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-grow">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/giris" element={<LoginPage />} />
          <Route path="/kayit" element={<RegisterPage />} />
        </Routes>
      </main>

      <footer className="border-t border-ink-700 py-10">
        <div className="mx-auto max-w-7xl px-4 text-center font-sans text-sm text-paper/40">
          <p>© 2026 TıklaSat. Tüm hakları saklıdır.</p>
          <p className="mt-2">Bu bir geliştirme projesidir.</p>
        </div>
      </footer>
    </div>
  )
}

export default App
