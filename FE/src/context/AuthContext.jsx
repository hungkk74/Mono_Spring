import { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { authApi } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [loading, setLoading] = useState(false);

  const isLoggedIn = !!token;
  const isAdmin = user?.role === 'ADMIN';
  const isStaff = user?.role === 'STAFF';
  const isAdminOrStaff = isAdmin || isStaff;

  const login = useCallback(async (email, password) => {
    const res = await authApi.login(email, password);
    if (res.ok) {
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data.user));
      setToken(res.data.token);
      setUser(res.data.user);
    }
    return res;
  }, []);

  const loginWithGoogle = useCallback(async (idToken) => {
    const res = await authApi.loginWithGoogle(idToken);
    if (res.ok) {
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data.user));
      setToken(res.data.token);
      setUser(res.data.user);
    }
    return res;
  }, []);

  const register = useCallback(async (data) => {
    const res = await authApi.register(data);
    if (res.ok) {
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('user', JSON.stringify(res.data.user));
      setToken(res.data.token);
      setUser(res.data.user);
    }
    return res;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const refreshProfile = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    try {
      const res = await authApi.getMe();
      if (res.ok) {
        setUser(res.data);
        localStorage.setItem('user', JSON.stringify(res.data));
      }
    } catch (e) {
      console.error('Failed to refresh profile', e);
    } finally {
      setLoading(false);
    }
  }, [token]);

  const updateProfile = useCallback(async (data) => {
    const res = await authApi.updateProfile(data);
    if (res.ok) {
      setUser(res.data);
      localStorage.setItem('user', JSON.stringify(res.data));
    }
    return res;
  }, []);

  const value = useMemo(() => ({
    user, token, isLoggedIn, isAdmin, isStaff, isAdminOrStaff, loading,
    login, loginWithGoogle, register, logout, refreshProfile, updateProfile,
  }), [user, token, isLoggedIn, isAdmin, isStaff, isAdminOrStaff, loading,
       login, loginWithGoogle, register, logout, refreshProfile, updateProfile]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
