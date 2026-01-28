import api from '../utils/axios';

export const predictionService = {
  getPredictions: async (deviceId) => {
    const response = await api.get(`/predictions/${deviceId}`);
    return response.data;
  },
};

export default predictionService;
