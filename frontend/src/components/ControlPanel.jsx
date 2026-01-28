import { useState } from 'react';
import controlService from '../services/controlService';

export default function ControlPanel({ device, onUpdate }) {
  const [loading, setLoading] = useState({});
  const [error, setError] = useState(null);
  const [tempSetpoint, setTempSetpoint] = useState(device.temperatureSetpoint || 24);

  const handlePowerToggle = async () => {
    setLoading(prev => ({ ...prev, power: true }));
    setError(null);
    try {
      const newStatus = device.powerStatus === 'ON' ? 'OFF' : 'ON';
      await controlService.setOnOff(device.id, newStatus);
      onUpdate?.();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to toggle power');
    } finally {
      setLoading(prev => ({ ...prev, power: false }));
    }
  };

  const handleModeChange = async (mode) => {
    setLoading(prev => ({ ...prev, mode: true }));
    setError(null);
    try {
      await controlService.setMode(device.id, mode);
      onUpdate?.();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to change mode');
    } finally {
      setLoading(prev => ({ ...prev, mode: false }));
    }
  };

  const handleFanSpeedChange = async (speed) => {
    setLoading(prev => ({ ...prev, fan: true }));
    setError(null);
    try {
      await controlService.setFanSpeed(device.id, speed);
      onUpdate?.();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to change fan speed');
    } finally {
      setLoading(prev => ({ ...prev, fan: false }));
    }
  };

  const handleTemperatureChange = async () => {
    setLoading(prev => ({ ...prev, temp: true }));
    setError(null);
    try {
      await controlService.setTemperature(device.id, tempSetpoint);
      onUpdate?.();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to set temperature');
    } finally {
      setLoading(prev => ({ ...prev, temp: false }));
    }
  };

  const isDisabled = !device.licenseActive;

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Control Panel</h3>

      {error && (
        <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-md text-sm">
          {error}
        </div>
      )}

      {isDisabled && (
        <div className="mb-4 p-3 bg-yellow-50 text-yellow-700 rounded-md text-sm">
          Device license is inactive. Controls are disabled.
        </div>
      )}

      <div className="space-y-6">
        {/* Power Toggle */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Power</label>
          <button
            onClick={handlePowerToggle}
            disabled={isDisabled || loading.power}
            className={`w-full py-3 px-4 rounded-lg font-medium transition-colors ${
              device.powerStatus === 'ON'
                ? 'bg-green-500 hover:bg-green-600 text-white'
                : 'bg-gray-200 hover:bg-gray-300 text-gray-800'
            } ${isDisabled ? 'opacity-50 cursor-not-allowed' : ''}`}
          >
            {loading.power ? (
              <span className="flex items-center justify-center">
                <div className="spinner mr-2"></div>
                Updating...
              </span>
            ) : (
              device.powerStatus === 'ON' ? 'ON' : 'OFF'
            )}
          </button>
        </div>

        {/* Mode Selection */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Mode</label>
          <div className="flex space-x-2">
            {['COOLING', 'HEATING'].map((mode) => (
              <button
                key={mode}
                onClick={() => handleModeChange(mode)}
                disabled={isDisabled || loading.mode}
                className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${
                  device.mode === mode
                    ? mode === 'COOLING' ? 'bg-blue-500 text-white' : 'bg-orange-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                } ${isDisabled ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                {mode}
              </button>
            ))}
          </div>
        </div>

        {/* Fan Speed */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Fan Speed</label>
          <div className="flex space-x-2">
            {['LOW', 'MED', 'HIGH'].map((speed) => (
              <button
                key={speed}
                onClick={() => handleFanSpeedChange(speed)}
                disabled={isDisabled || loading.fan}
                className={`flex-1 py-2 px-4 rounded-lg font-medium transition-colors ${
                  device.fanSpeed === speed
                    ? 'bg-primary-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                } ${isDisabled ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                {speed}
              </button>
            ))}
          </div>
        </div>

        {/* Temperature Setpoint */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Temperature Setpoint: {tempSetpoint}°C
          </label>
          <div className="flex items-center space-x-4">
            <input
              type="range"
              min="16"
              max="30"
              value={tempSetpoint}
              onChange={(e) => setTempSetpoint(parseFloat(e.target.value))}
              disabled={isDisabled}
              className="flex-1"
            />
            <button
              onClick={handleTemperatureChange}
              disabled={isDisabled || loading.temp}
              className={`px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors ${
                isDisabled ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {loading.temp ? 'Setting...' : 'Set'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
