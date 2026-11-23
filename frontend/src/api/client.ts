import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT token to requests if present in localStorage
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('trek_auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface HealthData {
  status: string;
  application: string;
  profile: string;
  version: string;
  timestamp: string;
}

export const checkHealth = async (): Promise<ApiResponse<HealthData>> => {
  const response = await apiClient.get<ApiResponse<HealthData>>('/health');
  return response.data;
};
