import { createContext, useContext, useState } from 'react';
import { logout as apiLogout, getToken } from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getToken());

  function handleLogin(newToken) {
    setToken(newToken);
  }

  function handleLogout() {
    apiLogout();
    setToken(null);
  }

  return (
    <AuthContext.Provider value={{ token, isLoggedIn: !!token, onLogin: handleLogin, onLogout: handleLogout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
