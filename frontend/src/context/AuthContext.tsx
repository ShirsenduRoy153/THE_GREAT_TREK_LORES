import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiClient, type ApiResponse } from '../api/client';
import type { AuthUser, UserRole } from '../types/auth';

interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string, role: UserRole) => Promise<void>;
  logout: () => void;
  quickDemoLogin: (role: UserRole) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('trek_auth_token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    if (token) {
      apiClient
        .get<ApiResponse<AuthUser>>('/auth/me')
        .then((res) => {
          if (res.data.success) {
            setUser(res.data.data);
          }
        })
        .catch(() => {
          logout();
        })
        .finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, [token]);

  const login = async (email: string, password: string) => {
    const res = await apiClient.post<ApiResponse<AuthUser & { token: string }>>('/auth/login', {
      email,
      password,
    });
    if (res.data.success) {
      const { token: jwtToken, ...userData } = res.data.data;
      localStorage.setItem('trek_auth_token', jwtToken);
      setToken(jwtToken);
      setUser(userData);
    }
  };

  const register = async (email: string, password: string, displayName: string, role: UserRole) => {
    const res = await apiClient.post<ApiResponse<AuthUser & { token: string }>>('/auth/register', {
      email,
      password,
      displayName,
      role,
    });
    if (res.data.success) {
      const { token: jwtToken, ...userData } = res.data.data;
      localStorage.setItem('trek_auth_token', jwtToken);
      setToken(jwtToken);
      setUser(userData);
    }
  };

  const logout = () => {
    localStorage.removeItem('trek_auth_token');
    setToken(null);
    setUser(null);
  };

  const quickDemoLogin = async (role: UserRole) => {
    const demoCredentials: Record<UserRole, { email: string; pass: string; name: string }> = {
      USER: { email: 'trekker@demo.com', pass: 'password123', name: 'Aarav Sharma (Trekker)' },
      ORGANIZER: { email: 'organizer@demo.com', pass: 'password123', name: 'Vikram Negi (Guide)' },
      ADMIN: { email: 'admin@demo.com', pass: 'password123', name: 'Safety Admin' },
    };

    const creds = demoCredentials[role];
    try {
      await login(creds.email, creds.pass);
    } catch {
      // If user not yet seeded, register them on the fly!
      await register(creds.email, creds.pass, creds.name, role);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        quickDemoLogin,
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
