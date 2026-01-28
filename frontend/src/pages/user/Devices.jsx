import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import DeviceCard from '../../components/DeviceCard';
import deviceService from '../../services/deviceService';

export default function Devices() {
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    fetchDevices();
  }, []);

  const fetchDevices = async () => {
    try {
      const response = await deviceService.getUserDevices();
      if (response.success) {
        setDevices(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch devices:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredDevices = devices.filter((device) => {
    if (filter === 'all') return true;
    if (filter === 'online') {
      const isOnline = device.lastHeartbeat &&
        new Date(device.lastHeartbeat) > new Date(Date.now() - 2 * 60 * 1000);
      return isOnline;
    }
    if (filter === 'offline') {
      const isOnline = device.lastHeartbeat &&
        new Date(device.lastHeartbeat) > new Date(Date.now() - 2 * 60 * 1000);
      return !isOnline;
    }
    if (filter === 'faults') {
      return device.unresolvedFaults > 0;
    }
    return true;
  });

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Devices</h1>
          <p className="text-gray-500">View and control devices in your teams</p>
        </div>
        <div className="flex items-center space-x-2">
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 text-sm"
          >
            <option value="all">All Devices</option>
            <option value="online">Online Only</option>
            <option value="offline">Offline Only</option>
            <option value="faults">With Faults</option>
          </select>
        </div>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="spinner"></div>
        </div>
      ) : filteredDevices.length === 0 ? (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8 text-center">
          <svg className="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
          </svg>
          <p className="text-gray-500">
            {filter === 'all'
              ? 'No devices assigned to your teams yet.'
              : `No ${filter} devices found.`}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredDevices.map((device) => (
            <Link key={device.id} to={`/user/devices/${device.id}`}>
              <DeviceCard device={device} />
            </Link>
          ))}
        </div>
      )}
    </Layout>
  );
}
