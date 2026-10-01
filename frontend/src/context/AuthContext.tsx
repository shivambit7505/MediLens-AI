import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, AuthResponse } from '../types';
import { authApi, LoginPayload, RegisterPayload } from '../api/authApi';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem('medilens_token');
    const savedUser = localStorage.getItem('medilens_user');

    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
      } catch (e) {
        console.error('Failed to parse cached user:', e);
        localStorage.removeItem('medilens_token');
        localStorage.removeItem('medilens_user');
      }
    }
    setIsLoading(false);
  }, []);

  const handleAuthSuccess = (data: AuthResponse) => {
    setToken(data.accessToken);
    setUser(data.user);
    localStorage.setItem('medilens_token', data.accessToken);
    localStorage.setItem('medilens_user', JSON.stringify(data.user));
  };

  const login = async (payload: LoginPayload) => {
    const data = await authApi.login(payload);
    handleAuthSuccess(data);
  };

  const register = async (payload: RegisterPayload) => {
    const data = await authApi.register(payload);
    handleAuthSuccess(data);
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('medilens_token');
    localStorage.removeItem('medilens_user');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        isLoading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
