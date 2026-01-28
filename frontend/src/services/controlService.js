import api from '../utils/axios';

export const controlService = {
  setOnOff: async (deviceId, status) => {
    const response = await api.post(`/control/${deviceId}/on-off`, { status });
    return response.data;
  },

  setMode: async (deviceId, mode) => {
    const response = await api.post(`/control/${deviceId}/mode`, { mode });
    return response.data;
  },

  setFanSpeed: async (deviceId, speed) => {
    const response = await api.post(`/control/${deviceId}/fan-speed`, { speed });
    return response.data;
  },

  setTemperature: async (deviceId, setpoint) => {
    const response = await api.post(`/control/${deviceId}/temperature`, { setpoint });
    return response.data;
  },
};

export default controlService;
