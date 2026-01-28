import { Link } from 'react-router-dom';
import { getUser, getRolePath } from '../utils/roleUtils';

export default function DeviceCard({ device }) {
  const user = getUser();
  const basePath = getRolePath(user?.role);

  const isOnline = device.lastHeartbeat &&
    new Date(device.lastHeartbeat) > new Date(Date.now() - 2 * 60 * 1000);

  return (
    <Link
      to={`${basePath}/devices/${device.id}`}
      className="block bg-white rounded-lg shadow-sm border border-gray-200 p-6 card-hover"
    >
      <div className="flex items-start justify-between">
        <div className="flex items-center">
          <div className={`p-3 rounded-lg ${device.licenseActive ? 'bg-primary-100' : 'bg-gray-100'}`}>
            <svg className={`w-6 h-6 ${device.licenseActive ? 'text-primary-600' : 'text-gray-400'}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
            </svg>
          </div>
          <div className="ml-4">
            <h3 className="text-lg font-semibold text-gray-900">{device.name}</h3>
            <p className="text-sm text-gray-500">{device.id}</p>
          </div>
        </div>
        <div className="flex items-center space-x-2">
          {device.unresolvedFaults > 0 && (
            <span className="px-2 py-1 text-xs font-medium bg-red-100 text-red-700 rounded-full">
              {device.unresolvedFaults} fault{device.unresolvedFaults > 1 ? 's' : ''}
            </span>
          )}
          <span className={`status-dot ${isOnline ? 'status-online' : 'status-offline'}`}></span>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-2 gap-4">
        <div>
          <p className="text-xs text-gray-500">Status</p>
          <p className={`text-sm font-medium ${device.powerStatus === 'ON' ? 'text-green-600' : 'text-gray-600'}`}>
            {device.powerStatus || 'OFF'}
          </p>
        </div>
        <div>
          <p className="text-xs text-gray-500">Mode</p>
          <p className="text-sm font-medium text-gray-900">{device.mode || 'N/A'}</p>
        </div>
        <div>
          <p className="text-xs text-gray-500">Temperature</p>
          <p className="text-sm font-medium text-gray-900">{device.temperatureSetpoint}°C</p>
        </div>
        <div>
          <p className="text-xs text-gray-500">Fan</p>
          <p className="text-sm font-medium text-gray-900">{device.fanSpeed || 'N/A'}</p>
        </div>
      </div>

      <div className="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between">
        <div className="flex items-center">
          <span className={`px-2 py-1 text-xs font-medium rounded ${device.licenseActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
            {device.licenseActive ? 'Licensed' : 'Unlicensed'}
          </span>
        </div>
        {device.teamName && (
          <span className="text-xs text-gray-500">Team: {device.teamName}</span>
        )}
      </div>
    </Link>
  );
}
