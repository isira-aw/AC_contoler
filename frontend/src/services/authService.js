import api from '../utils/axios';

export const authService = {
  login: async (email, password) => {
    const response = await api.post('/auth/login', { email, password });
    if (response.data.success && response.data.data) {
      const { token, role, userId, name, email: userEmail } = response.data.data;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify({ userId, role, name, email: userEmail }));
    }
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  refreshToken: async () => {
    const response = await api.post('/auth/refresh');
    if (response.data.success && response.data.data) {
      localStorage.setItem('token', response.data.data.token);
    }
    return response.data;
  },

  requestPasswordReset: async (email) => {
    const response = await api.post('/auth/reset-password-request', { email });
    return response.data;
  },

  resetPassword: async (token, newPassword) => {
    const response = await api.post('/auth/reset-password', { token, newPassword });
    return response.data;
  },
};

export default authService;
