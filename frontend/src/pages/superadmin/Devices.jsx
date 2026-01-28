import { useState, useEffect, useCallback } from 'react';
import Layout from '../../components/Layout';
import Modal from '../../components/Modal';
import Pagination from '../../components/Pagination';
import deviceService from '../../services/deviceService';
import userService from '../../services/userService';

export default function SuperAdminDevices() {
  const [devices, setDevices] = useState([]);
  const [owners, setOwners] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingDevice, setEditingDevice] = useState(null);
  const [formData, setFormData] = useState({ deviceId: '', name: '', ownerId: '', licenseActive: true });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Pagination state
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [devicesRes, ownersRes] = await Promise.all([
        deviceService.getAllDevices(page, pageSize),
        userService.getDeviceOwners(0, 100),
      ]);
      if (devicesRes.success) {
        const data = devicesRes.data;
        setDevices(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      }
      if (ownersRes.success) {
        setOwners(ownersRes.data.content || ownersRes.data || []);
      }
    } catch (error) {
      console.error('Failed to fetch data:', error);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      let response;
      if (editingDevice) {
        response = await deviceService.updateDevice(editingDevice.id, {
          name: formData.name,
          ownerId: formData.ownerId || null,
          licenseActive: formData.licenseActive,
        });
      } else {
        response = await deviceService.createDevice({
          deviceId: formData.deviceId,
          name: formData.name,
          ownerId: formData.ownerId || null,
        });
      }

      if (response.success) {
        setIsModalOpen(false);
        setEditingDevice(null);
        setFormData({ deviceId: '', name: '', ownerId: '', licenseActive: true });
        fetchData();
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save device');
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (device) => {
    setEditingDevice(device);
    setFormData({
      deviceId: device.id,
      name: device.name,
      ownerId: device.ownerId || '',
      licenseActive: device.licenseActive,
    });
    setIsModalOpen(true);
  };

  const handleDelete = async (deviceId) => {
    if (!confirm('Are you sure you want to delete this device?')) return;

    try {
      await deviceService.deleteDevice(deviceId);
      fetchData();
    } catch (error) {
      console.error('Failed to delete device:', error);
    }
  };

  const toggleLicense = async (device) => {
    try {
      await deviceService.updateDevice(device.id, {
        licenseActive: !device.licenseActive,
      });
      fetchData();
    } catch (error) {
      console.error('Failed to toggle license:', error);
    }
  };

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Devices</h1>
          <p className="text-gray-500">Manage all HVAC devices and licenses</p>
        </div>
        <button
          onClick={() => {
            setEditingDevice(null);
            setFormData({ deviceId: '', name: '', ownerId: '', licenseActive: true });
            setIsModalOpen(true);
          }}
          className="px-4 py-2 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 transition-colors"
        >
          Add Device
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="spinner"></div>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Device ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Owner</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Team</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">License</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {devices.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-8 text-center text-gray-500">
                    No devices found
                  </td>
                </tr>
              ) : (
                devices.map((device) => (
                  <tr key={device.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 font-medium text-gray-900">{device.id}</td>
                    <td className="px-6 py-4 text-gray-600">{device.name}</td>
                    <td className="px-6 py-4 text-gray-600">{device.ownerName || 'Unassigned'}</td>
                    <td className="px-6 py-4 text-gray-600">{device.teamName || 'None'}</td>
                    <td className="px-6 py-4">
                      <button
                        onClick={() => toggleLicense(device)}
                        className={`px-2 py-1 text-xs font-medium rounded ${
                          device.licenseActive
                            ? 'bg-green-100 text-green-700 hover:bg-green-200'
                            : 'bg-red-100 text-red-700 hover:bg-red-200'
                        }`}
                      >
                        {device.licenseActive ? 'Active' : 'Inactive'}
                      </button>
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-1 text-xs font-medium rounded ${
                        device.powerStatus === 'ON' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'
                      }`}>
                        {device.powerStatus || 'OFF'}
                      </span>
                    </td>
                    <td className="px-6 py-4">
                      <button
                        onClick={() => handleEdit(device)}
                        className="text-primary-600 hover:text-primary-700 mr-3"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(device.id)}
                        className="text-red-600 hover:text-red-700"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
          <Pagination
            currentPage={page}
            totalPages={totalPages}
            totalElements={totalElements}
            pageSize={pageSize}
            onPageChange={setPage}
            onPageSizeChange={(size) => {
              setPageSize(size);
              setPage(0);
            }}
          />
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setEditingDevice(null);
        }}
        title={editingDevice ? 'Edit Device' : 'Add Device'}
      >
        {error && (
          <div className="mb-4 p-3 bg-red-50 text-red-700 rounded-md text-sm">
            {error}
          </div>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          {!editingDevice && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Device ID</label>
              <input
                type="text"
                required
                value={formData.deviceId}
                onChange={(e) => setFormData({ ...formData, deviceId: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                placeholder="e.g., YORK-005"
              />
            </div>
          )}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Owner</label>
            <select
              value={formData.ownerId}
              onChange={(e) => setFormData({ ...formData, ownerId: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
            >
              <option value="">Unassigned</option>
              {owners.map((owner) => (
                <option key={owner.id} value={owner.id}>
                  {owner.name}
                </option>
              ))}
            </select>
          </div>
          {editingDevice && (
            <div className="flex items-center">
              <input
                type="checkbox"
                id="licenseActive"
                checked={formData.licenseActive}
                onChange={(e) => setFormData({ ...formData, licenseActive: e.target.checked })}
                className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label htmlFor="licenseActive" className="ml-2 text-sm text-gray-700">
                License Active
              </label>
            </div>
          )}
          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={() => {
                setIsModalOpen(false);
                setEditingDevice(null);
              }}
              className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
            >
              {submitting ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </Modal>
    </Layout>
  );
}
