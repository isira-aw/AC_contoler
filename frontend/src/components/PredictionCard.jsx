import { useState, useEffect } from 'react';
import predictionService from '../services/predictionService';

export default function PredictionCard({ deviceId }) {
  const [predictions, setPredictions] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchPredictions();
  }, [deviceId]);

  const fetchPredictions = async () => {
    try {
      setLoading(true);
      const response = await predictionService.getPredictions(deviceId);
      if (response.success) {
        setPredictions(response.data);
      }
    } catch (err) {
      setError('Failed to load predictions');
    } finally {
      setLoading(false);
    }
  };

  const getEfficiencyColor = (score) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getEfficiencyBg = (score) => {
    if (score >= 80) return 'bg-green-100';
    if (score >= 60) return 'bg-yellow-100';
    return 'bg-red-100';
  };

  if (loading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Predictions</h3>
        <div className="flex items-center justify-center py-8">
          <div className="spinner"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Predictions</h3>
        <div className="text-red-600 text-sm">{error}</div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Predictions & Analytics</h3>

      <div className="grid grid-cols-2 gap-4">
        {/* Efficiency Score */}
        <div className={`col-span-2 p-4 rounded-lg ${getEfficiencyBg(predictions?.efficiencyScore || 0)}`}>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">Efficiency Score</p>
              <p className={`text-3xl font-bold ${getEfficiencyColor(predictions?.efficiencyScore || 0)}`}>
                {predictions?.efficiencyScore?.toFixed(1) || 'N/A'}%
              </p>
            </div>
            <div className="w-16 h-16 relative">
              <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                <path
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  fill="none"
                  stroke="#e5e7eb"
                  strokeWidth="3"
                />
                <path
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                  fill="none"
                  stroke={predictions?.efficiencyScore >= 80 ? '#10b981' : predictions?.efficiencyScore >= 60 ? '#f59e0b' : '#ef4444'}
                  strokeWidth="3"
                  strokeDasharray={`${predictions?.efficiencyScore || 0}, 100`}
                />
              </svg>
            </div>
          </div>
        </div>

        {/* Runtime Estimate */}
        <div className="p-4 bg-gray-50 rounded-lg">
          <p className="text-sm font-medium text-gray-600">Runtime Remaining</p>
          <p className="text-2xl font-bold text-gray-900">
            {predictions?.runtimeEstimate?.toFixed(1) || 'N/A'}
          </p>
          <p className="text-xs text-gray-500">hours this week</p>
        </div>

        {/* Daily Energy */}
        <div className="p-4 bg-gray-50 rounded-lg">
          <p className="text-sm font-medium text-gray-600">Daily Energy</p>
          <p className="text-2xl font-bold text-gray-900">
            {predictions?.dailyEnergyPrediction?.toFixed(2) || 'N/A'}
          </p>
          <p className="text-xs text-gray-500">kWh predicted</p>
        </div>

        {/* Monthly Energy */}
        <div className="col-span-2 p-4 bg-gray-50 rounded-lg">
          <p className="text-sm font-medium text-gray-600">Monthly Energy Prediction</p>
          <p className="text-2xl font-bold text-gray-900">
            {predictions?.monthlyEnergyPrediction?.toFixed(2) || 'N/A'} kWh
          </p>
          <p className="text-xs text-gray-500">estimated based on current usage patterns</p>
        </div>
      </div>
    </div>
  );
}
