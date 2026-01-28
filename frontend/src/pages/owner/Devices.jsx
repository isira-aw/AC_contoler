import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../../components/Layout';
import DeviceCard from '../../components/DeviceCard';
import Modal from '../../components/Modal';
import deviceService from '../../services/deviceService';
import userService from '../../services/userService';

export default function Devices() {
  const [devices, setDevices] = useState([]);
  const [owners, setOwners] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
  const [selectedDevice, setSelectedDevice] = useState(null);
  const [newOwnerId, setNewOwnerId] = useState('');
  const [transferring, setTransferring] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const [devicesRes, ownersRes] = await Promise.all([
        deviceService.getOwnerDevices(),
        userService.getDeviceOwners().catch(() => ({ success: true, data: [] })),
      ]);
      if (devicesRes.success) {
        // Handle paginated response - extract content array
        const deviceData = devicesRes.data;
        setDevices(deviceData.content || deviceData || []);
      }
      if (ownersRes.success) {
        // Handle paginated response for owners as well
        const ownerData = ownersRes.data;
        setOwners(ownerData.content || ownerData || []);
      }
    } catch (error) {
      console.error('Failed to fetch devices:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleTransfer = async () => {
    if (!selectedDevice || !newOwnerId) return;

    setTransferring(true);
    try {
      const response = await deviceService.transferDevice(selectedDevice.id, newOwnerId);
      if (response.success) {
        setIsTransferModalOpen(false);
        setSelectedDevice(null);
        setNewOwnerId('');
        fetchData();
      } else {
        alert(response.message);
      }
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to transfer device');
    } finally {
      setTransferring(false);
    }
  };

  const openTransferModal = (device) => {
    setSelectedDevice(device);
    setNewOwnerId('');
    setIsTransferModalOpen(true);
  };

  return (
    <Layout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">My Devices</h1>
        <p className="text-gray-500">Monitor and control your HVAC devices</p>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="spinner"></div>
        </div>
      ) : devices.length === 0 ? (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8 text-center">
          <svg className="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
          </svg>
          <p className="text-gray-500">No devices assigned to you yet.</p>
          <p className="text-gray-400 text-sm mt-1">Contact your administrator to get devices assigned.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {devices.map((device) => (
            <div key={device.id} className="relative">
              <Link to={`/owner/devices/${device.id}`}>
                <DeviceCard device={device} />
              </Link>
              <button
                onClick={(e) => {
                  e.preventDefault();
                  openTransferModal(device);
                }}
                className="absolute top-4 right-4 p-2 text-gray-400 hover:text-gray-600 bg-white rounded-full shadow-sm"
                title="Transfer Ownership"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                </svg>
              </button>
            </div>
          ))}
        </div>
      )}

      <Modal
        isOpen={isTransferModalOpen}
        onClose={() => {
          setIsTransferModalOpen(false);
          setSelectedDevice(null);
        }}
        title="Transfer Device Ownership"
      >
        <p className="text-gray-600 mb-4">
          Transfer <strong>{selectedDevice?.name}</strong> to another device owner.
        </p>
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            New Owner
          </label>
          <select
            value={newOwnerId}
            onChange={(e) => setNewOwnerId(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
          >
            <option value="">Select owner...</option>
            {owners.map((owner) => (
              <option key={owner.id} value={owner.id}>
                {owner.name} ({owner.email})
              </option>
            ))}
          </select>
        </div>
        <div className="flex justify-end space-x-3">
          <button
            onClick={() => {
              setIsTransferModalOpen(false);
              setSelectedDevice(null);
            }}
            className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
          >
            Cancel
          </button>
          <button
            onClick={handleTransfer}
            disabled={!newOwnerId || transferring}
            className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
          >
            {transferring ? 'Transferring...' : 'Transfer'}
          </button>
        </div>
      </Modal>
    </Layout>
  );
}
