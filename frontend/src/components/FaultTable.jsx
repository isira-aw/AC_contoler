import { useState } from 'react';
import telemetryService from '../services/telemetryService';
import { getUser, ROLES } from '../utils/roleUtils';

export default function FaultTable({ faults, deviceId, onUpdate }) {
  const [resolvingId, setResolvingId] = useState(null);
  const [exporting, setExporting] = useState(false);
  const user = getUser();
  const isOwner = user?.role === ROLES.DEVICE_OWNER;

  const handleResolve = async (faultId) => {
    setResolvingId(faultId);
    try {
      await telemetryService.resolveFault(faultId);
      onUpdate?.();
    } catch (error) {
      console.error('Failed to resolve fault:', error);
    } finally {
      setResolvingId(null);
    }
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      const blob = await telemetryService.exportFaultsPdf(deviceId);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `fault_log_${deviceId}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Failed to export PDF:', error);
    } finally {
      setExporting(false);
    }
  };

  const getFaultTypeColor = (type) => {
    switch (type) {
      case 'Overcurrent':
        return 'bg-red-100 text-red-700';
      case 'Phase Failure':
        return 'bg-orange-100 text-orange-700';
      case 'Overheating':
        return 'bg-yellow-100 text-yellow-700';
      case 'Filter Choke':
        return 'bg-purple-100 text-purple-700';
      case 'Sensor Failure':
        return 'bg-blue-100 text-blue-700';
      case 'Device Offline':
        return 'bg-gray-100 text-gray-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      <div className="p-6 border-b border-gray-200 flex items-center justify-between">
        <h3 className="text-lg font-semibold text-gray-900">Fault Logs</h3>
        {isOwner && (
          <button
            onClick={handleExport}
            disabled={exporting}
            className="px-4 py-2 bg-primary-600 text-white text-sm font-medium rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50"
          >
            {exporting ? 'Exporting...' : 'Export PDF'}
          </button>
        )}
      </div>

      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Timestamp</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Value</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
              {isOwner && (
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Action</th>
              )}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {faults.length === 0 ? (
              <tr>
                <td colSpan={isOwner ? 5 : 4} className="px-6 py-8 text-center text-gray-500">
                  No fault logs found
                </td>
              </tr>
            ) : (
              faults.map((fault) => (
                <tr key={fault.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {new Date(fault.timestamp).toLocaleString()}
                  </td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 text-xs font-medium rounded ${getFaultTypeColor(fault.faultType)}`}>
                      {fault.faultType}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">{fault.value || 'N/A'}</td>
                  <td className="px-6 py-4">
                    {fault.resolved ? (
                      <span className="px-2 py-1 text-xs font-medium bg-green-100 text-green-700 rounded">
                        Resolved
                      </span>
                    ) : (
                      <span className="px-2 py-1 text-xs font-medium bg-red-100 text-red-700 rounded">
                        Unresolved
                      </span>
                    )}
                  </td>
                  {isOwner && (
                    <td className="px-6 py-4">
                      {!fault.resolved && (
                        <button
                          onClick={() => handleResolve(fault.id)}
                          disabled={resolvingId === fault.id}
                          className="text-sm text-primary-600 hover:text-primary-700 font-medium disabled:opacity-50"
                        >
                          {resolvingId === fault.id ? 'Resolving...' : 'Resolve'}
                        </button>
                      )}
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
