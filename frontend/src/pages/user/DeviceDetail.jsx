import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import Layout from '../../components/Layout';
import TelemetryChart from '../../components/TelemetryChart';
import ControlPanel from '../../components/ControlPanel';
import FaultTable from '../../components/FaultTable';
import PredictionCard from '../../components/PredictionCard';
import deviceService from '../../services/deviceService';
import telemetryService from '../../services/telemetryService';
import controlService from '../../services/controlService';
import predictionService from '../../services/predictionService';

export default function DeviceDetail() {
  const { deviceId } = useParams();
  const [device, setDevice] = useState(null);
  const [telemetry, setTelemetry] = useState([]);
  const [faults, setFaults] = useState([]);
  const [predictions, setPredictions] = useState(null);
  const [loading, setLoading] = useState(true);
  const [controlling, setControlling] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');

  const fetchData = useCallback(async () => {
    try {
      const [deviceRes, telemetryRes, faultsRes, predictionsRes] = await Promise.all([
        deviceService.getUserDevice(deviceId),
        telemetryService.getTelemetry(deviceId, 'user'),
        telemetryService.getFaults(deviceId, 'user'),
        predictionService.getPredictions(deviceId),
      ]);

      if (deviceRes.success) setDevice(deviceRes.data);
      if (telemetryRes.success) setTelemetry(telemetryRes.data);
      if (faultsRes.success) setFaults(faultsRes.data);
      if (predictionsRes.success) setPredictions(predictionsRes.data);
    } catch (error) {
      console.error('Failed to fetch device data:', error);
    } finally {
      setLoading(false);
    }
  }, [deviceId]);

  useEffect(() => {
    fetchData();
    // Poll for updates every 10 seconds
    const interval = setInterval(() => {
      fetchData();
    }, 10000);

    return () => clearInterval(interval);
  }, [fetchData]);

  const handleControl = async (type, value) => {
    setControlling(true);
    try {
      let response;
      switch (type) {
        case 'power':
          response = await controlService.setPower(deviceId, value);
          break;
        case 'mode':
          response = await controlService.setMode(deviceId, value);
          break;
        case 'fanSpeed':
          response = await controlService.setFanSpeed(deviceId, value);
          break;
        case 'temperature':
          response = await controlService.setTemperature(deviceId, value);
          break;
        default:
          return;
      }
      if (response.success) {
        fetchData();
      } else {
        alert(response.message);
      }
    } catch (error) {
      alert(error.response?.data?.message || 'Control command failed');
    } finally {
      setControlling(false);
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center py-12">
          <div className="spinner"></div>
        </div>
      </Layout>
    );
  }

  if (!device) {
    return (
      <Layout>
        <div className="text-center py-12">
          <p className="text-gray-500">Device not found or access denied</p>
        </div>
      </Layout>
    );
  }

  const latestTelemetry = telemetry[0] || {};
  const isOnline = device.lastHeartbeat &&
    new Date(device.lastHeartbeat) > new Date(Date.now() - 2 * 60 * 1000);

  return (
    <Layout>
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{device.name}</h1>
            <p className="text-gray-500">Device ID: {device.id}</p>
          </div>
          <div className="flex items-center space-x-4">
            <span className={`px-3 py-1 rounded-full text-sm font-medium ${
              isOnline ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            }`}>
              {isOnline ? 'Online' : 'Offline'}
            </span>
            {!device.licenseActive && (
              <span className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-sm font-medium">
                License Inactive
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="flex space-x-8">
          {['overview', 'telemetry', 'faults'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`py-4 px-1 border-b-2 font-medium text-sm capitalize ${
                activeTab === tab
                  ? 'border-primary-500 text-primary-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {tab}
            </button>
          ))}
        </nav>
      </div>

      {/* Content */}
      {activeTab === 'overview' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Live Status */}
          <div className="lg:col-span-2 bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Live Status</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-blue-50 rounded-lg p-4">
                <p className="text-sm text-blue-600 mb-1">Room Temp</p>
                <p className="text-2xl font-bold text-blue-700">
                  {latestTelemetry.roomTemp?.toFixed(1) || '--'}°C
                </p>
              </div>
              <div className="bg-green-50 rounded-lg p-4">
                <p className="text-sm text-green-600 mb-1">Supply Temp</p>
                <p className="text-2xl font-bold text-green-700">
                  {latestTelemetry.supplyTemp?.toFixed(1) || '--'}°C
                </p>
              </div>
              <div className="bg-purple-50 rounded-lg p-4">
                <p className="text-sm text-purple-600 mb-1">Humidity</p>
                <p className="text-2xl font-bold text-purple-700">
                  {latestTelemetry.humidity?.toFixed(0) || '--'}%
                </p>
              </div>
              <div className="bg-orange-50 rounded-lg p-4">
                <p className="text-sm text-orange-600 mb-1">Power</p>
                <p className="text-2xl font-bold text-orange-700">
                  {latestTelemetry.power?.toFixed(2) || '--'} kW
                </p>
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm text-gray-600 mb-1">Return Temp</p>
                <p className="text-xl font-semibold text-gray-700">
                  {latestTelemetry.returnTemp?.toFixed(1) || '--'}°C
                </p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm text-gray-600 mb-1">Outdoor Temp</p>
                <p className="text-xl font-semibold text-gray-700">
                  {latestTelemetry.outdoorTemp?.toFixed(1) || '--'}°C
                </p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm text-gray-600 mb-1">Energy</p>
                <p className="text-xl font-semibold text-gray-700">
                  {latestTelemetry.energy?.toFixed(2) || '--'} kWh
                </p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-sm text-gray-600 mb-1">Filter</p>
                <p className="text-xl font-semibold text-gray-700">
                  {latestTelemetry.filterCondition?.toFixed(0) || '--'} Pa
                </p>
              </div>
            </div>
          </div>

          {/* Control Panel */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Control Panel</h2>
            <ControlPanel
              device={device}
              onControl={handleControl}
              disabled={controlling || !device.licenseActive}
            />
          </div>

          {/* Predictions */}
          <div className="lg:col-span-3">
            <PredictionCard predictions={predictions} />
          </div>
        </div>
      )}

      {activeTab === 'telemetry' && (
        <div className="space-y-6">
          <TelemetryChart
            data={telemetry}
            type="temperature"
            title="Temperature History"
          />
          <TelemetryChart
            data={telemetry}
            type="energy"
            title="Energy Consumption"
          />
          <TelemetryChart
            data={telemetry}
            type="humidity"
            title="Humidity Levels"
          />
        </div>
      )}

      {activeTab === 'faults' && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200">
          <div className="p-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">Fault Logs</h2>
          </div>
          <FaultTable faults={faults} canResolve={false} />
        </div>
      )}
    </Layout>
  );
}
