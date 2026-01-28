import api from '../utils/axios';
import { getUser } from '../utils/roleUtils';

const getBasePath = () => {
  const user = getUser();
  if (!user) return '/user';
  return user.role === 'DEVICE_OWNER' ? '/owner' : '/user';
};

export const telemetryService = {
  getTelemetry: async (deviceId, from, to) => {
    const basePath = getBasePath();
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    const response = await api.get(`${basePath}/devices/${deviceId}/telemetry${queryString}`);
    return response.data;
  },

  getLatestTelemetry: async (deviceId) => {
    const basePath = getBasePath();
    const response = await api.get(`${basePath}/devices/${deviceId}/telemetry/latest`);
    return response.data;
  },

  getFaults: async (deviceId) => {
    const basePath = getBasePath();
    const response = await api.get(`${basePath}/devices/${deviceId}/faults`);
    return response.data;
  },

  resolveFault: async (faultId) => {
    const response = await api.put(`/owner/faults/${faultId}/resolve`);
    return response.data;
  },

  exportFaultsPdf: async (deviceId) => {
    const response = await api.get(`/owner/devices/${deviceId}/faults/export-pdf`, {
      responseType: 'blob',
    });
    return response.data;
  },
};

export default telemetryService;
