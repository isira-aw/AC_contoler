import api from '../utils/axios';

export const userService = {
  // SuperAdmin - Device Owner management
  createDeviceOwner: async (ownerData) => {
    const response = await api.post('/superadmin/device-owners', ownerData);
    return response.data;
  },

  getDeviceOwners: async (page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') => {
    const response = await api.get('/superadmin/device-owners', {
      params: { page, size, sortBy, sortDir }
    });
    return response.data;
  },

  getDeviceOwner: async (id) => {
    const response = await api.get(`/superadmin/device-owners/${id}`);
    return response.data;
  },

  // DeviceOwner - Team management
  createTeam: async (teamData) => {
    const response = await api.post('/owner/teams', teamData);
    return response.data;
  },

  getTeams: async () => {
    const response = await api.get('/owner/teams');
    return response.data;
  },

  deleteTeam: async (teamId) => {
    const response = await api.delete(`/owner/teams/${teamId}`);
    return response.data;
  },

  // DeviceOwner - User management
  createDeviceUser: async (userData) => {
    const response = await api.post('/owner/users', userData);
    return response.data;
  },

  getDeviceUsers: async (page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') => {
    const response = await api.get('/owner/users', {
      params: { page, size, sortBy, sortDir }
    });
    return response.data;
  },

  updateDeviceUser: async (userId, userData) => {
    const response = await api.put(`/owner/users/${userId}`, userData);
    return response.data;
  },

  deleteDeviceUser: async (userId) => {
    const response = await api.delete(`/owner/users/${userId}`);
    return response.data;
  },

  // Dashboard stats
  getSuperAdminStats: async () => {
    const response = await api.get('/superadmin/dashboard/stats');
    return response.data;
  },

  getOwnerStats: async () => {
    const response = await api.get('/owner/dashboard/stats');
    return response.data;
  },

  getUserStats: async () => {
    const response = await api.get('/user/dashboard/stats');
    return response.data;
  },
};

export default userService;
