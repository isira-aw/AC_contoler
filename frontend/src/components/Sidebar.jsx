import { Link, useLocation } from 'react-router-dom';
import { getUser, getRolePath, ROLES } from '../utils/roleUtils';

export default function Sidebar() {
  const location = useLocation();
  const user = getUser();
  const basePath = getRolePath(user?.role);

  const getMenuItems = () => {
    switch (user?.role) {
      case ROLES.SUPER_ADMIN:
        return [
          { name: 'Dashboard', path: `${basePath}/dashboard`, icon: 'home' },
          { name: 'Device Owners', path: `${basePath}/device-owners`, icon: 'users' },
          { name: 'Devices', path: `${basePath}/devices`, icon: 'cpu' },
        ];
      case ROLES.DEVICE_OWNER:
        return [
          { name: 'Dashboard', path: `${basePath}/dashboard`, icon: 'home' },
          { name: 'Teams', path: `${basePath}/teams`, icon: 'users-group' },
          { name: 'Users', path: `${basePath}/users`, icon: 'users' },
          { name: 'Devices', path: `${basePath}/devices`, icon: 'cpu' },
        ];
      case ROLES.DEVICE_USER:
        return [
          { name: 'Dashboard', path: `${basePath}/dashboard`, icon: 'home' },
          { name: 'Devices', path: `${basePath}/devices`, icon: 'cpu' },
        ];
      default:
        return [];
    }
  };

  const renderIcon = (iconName) => {
    switch (iconName) {
      case 'home':
        return (
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
        );
      case 'users':
        return (
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
        );
      case 'users-group':
        return (
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
        );
      case 'cpu':
        return (
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
          </svg>
        );
      default:
        return null;
    }
  };

  const menuItems = getMenuItems();

  return (
    <aside className="w-64 bg-white shadow-sm border-r border-gray-200 min-h-[calc(100vh-4rem)]">
      <nav className="p-4 space-y-1">
        {menuItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center px-4 py-3 text-sm font-medium rounded-lg transition-colors ${
                isActive
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
              }`}
            >
              <span className={isActive ? 'text-primary-600' : 'text-gray-400'}>
                {renderIcon(item.icon)}
              </span>
              <span className="ml-3">{item.name}</span>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
