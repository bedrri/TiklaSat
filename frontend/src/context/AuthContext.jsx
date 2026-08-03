import { createContext, useContext, useEffect, useState } from 'react';
import { loginUser, registerUser } from '../lib/api';

const AuthContext = createContext(null);

const STORAGE_KEY = 'tiklasat.auth';

function readStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => readStoredAuth());

  useEffect(() => {
    if (auth) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [auth]);

  async function login(credentials) {
    const response = await loginUser(credentials);
    setAuth(response);
    return response;
  }

  async function register(payload) {
    const response = await registerUser(payload);
    setAuth(response);
    return response;
  }

  function logout() {
    setAuth(null);
  }

  const value = {
    user: auth ? { email: auth.email, fullName: auth.fullName } : null,
    accessToken: auth?.accessToken ?? null,
    isAuthenticated: Boolean(auth),
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth, AuthProvider içinde kullanılmalıdır');
  }
  return ctx;
}
