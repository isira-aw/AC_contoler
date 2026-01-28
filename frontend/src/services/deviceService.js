import api from '../utils/axios';
import { getUser } from '../utils/roleUtils';

const getBasePath = () => {
  const user = getUser();
  if (!user) return '/user';
  switch (user.role) {
    case 'SUPER_ADMIN':
      return '/superadmin';
    case 'DEVICE_OWNER':
      return '/owner';
    default:
      return '/user';
  }
};

export const deviceService = {
  // SuperAdmin endpoints
  createDevice: async (deviceData) => {
    const response = await api.post('/superadmin/devices', deviceData);
    return response.data;
  },

  getAllDevices: async (page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') => {
    const response = await api.get('/superadmin/devices', {
      params: { page, size, sortBy, sortDir }
    });
    return response.data;
  },

  updateDevice: async (deviceId, deviceData) => {
    const response = await api.put(`/superadmin/devices/${deviceId}`, deviceData);
    return response.data;
  },

  deleteDevice: async (deviceId) => {
    const response = await api.delete(`/superadmin/devices/${deviceId}`);
    return response.data;
  },

  // Owner endpoints
  getOwnerDevices: async (page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc') => {
    const response = await api.get('/owner/devices', {
      params: { page, size, sortBy, sortDir }
    });
    return response.data;
  },

  getOwnerDevice: async (deviceId) => {
    const response = await api.get(`/owner/devices/${deviceId}`);
    return response.data;
  },

  transferDevice: async (deviceId, newOwnerId) => {
    const response = await api.put(`/owner/devices/${deviceId}/transfer`, { newOwnerId });
    return response.data;
  },

  // User endpoints
  getUserDevices: async () => {
    const response = await api.get('/user/devices');
    return response.data;
  },

  getUserDevice: async (deviceId) => {
    const response = await api.get(`/user/devices/${deviceId}`);
    return response.data;
  },

  // Generic - uses role-based path
  getDevices: async () => {
    const basePath = getBasePath();
    if (basePath === '/superadmin') {
      return deviceService.getAllDevices();
    }
    const response = await api.get(`${basePath}/devices`);
    return response.data;
  },

  getDevice: async (deviceId) => {
    const basePath = getBasePath();
    if (basePath === '/superadmin') {
      const response = await api.get(`/superadmin/devices/${deviceId}`);
      return response.data;
    }
    const response = await api.get(`${basePath}/devices/${deviceId}`);
    return response.data;
  },
};

export default deviceService;
