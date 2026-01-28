import { createBrowserRouter, RouterProvider, Navigate, Outlet } from 'react-router-dom';
import { getRole, isAuthenticated } from './utils/roleUtils';

// Public Pages
import Login from './pages/Login';
import ResetPassword from './pages/ResetPassword';
import ResetPasswordConfirm from './pages/ResetPasswordConfirm';

// SuperAdmin Pages
import SuperAdminDashboard from './pages/superadmin/Dashboard';
import SuperAdminDeviceOwners from './pages/superadmin/DeviceOwners';
import SuperAdminDevices from './pages/superadmin/Devices';

// DeviceOwner Pages
import OwnerDashboard from './pages/owner/Dashboard';
import OwnerTeams from './pages/owner/Teams';
import OwnerUsers from './pages/owner/Users';
import OwnerDevices from './pages/owner/Devices';
import OwnerDeviceDetail from './pages/owner/DeviceDetail';

// DeviceUser Pages
import UserDashboard from './pages/user/Dashboard';
import UserDevices from './pages/user/Devices';
import UserDeviceDetail from './pages/user/DeviceDetail';

// Protected Route Component
function ProtectedRoute({ children, allowedRoles }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }

  const role = getRole();
  if (allowedRoles && !allowedRoles.includes(role)) {
    // Redirect to appropriate dashboard based on role
    switch (role) {
      case 'SUPER_ADMIN':
        return <Navigate to="/superadmin/dashboard" replace />;
      case 'DEVICE_OWNER':
        return <Navigate to="/owner/dashboard" replace />;
      case 'DEVICE_USER':
        return <Navigate to="/user/dashboard" replace />;
      default:
        return <Navigate to="/login" replace />;
    }
  }

  return children;
}

// Redirect authenticated users to their dashboard
function AuthRoute({ children }) {
  if (isAuthenticated()) {
    const role = getRole();
    switch (role) {
      case 'SUPER_ADMIN':
        return <Navigate to="/superadmin/dashboard" replace />;
      case 'DEVICE_OWNER':
        return <Navigate to="/owner/dashboard" replace />;
      case 'DEVICE_USER':
        return <Navigate to="/user/dashboard" replace />;
      default:
        break;
    }
  }
  return children;
}

// Create router with future flags to prevent deprecation warnings
const router = createBrowserRouter(
  [
    // Public Routes
    {
      path: '/',
      element: <Navigate to="/home" replace />,
    },
    {
      path: '/home',
      lazy: () => import('./pages/Home').then((m) => ({ Component: m.default })),
    },
    {
      path: '/login',
      element: (
        <AuthRoute>
          <Login />
        </AuthRoute>
      ),
    },
    {
      path: '/reset-password',
      element: <ResetPassword />,
    },
    {
      path: '/reset-password/:token',
      element: <ResetPasswordConfirm />,
    },
    // SuperAdmin Routes
    {
      path: '/superadmin/dashboard',
      element: (
        <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
          <SuperAdminDashboard />
        </ProtectedRoute>
      ),
    },
    {
      path: '/superadmin/device-owners',
      element: (
        <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
          <SuperAdminDeviceOwners />
        </ProtectedRoute>
      ),
    },
    {
      path: '/superadmin/devices',
      element: (
        <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
          <SuperAdminDevices />
        </ProtectedRoute>
      ),
    },
    // DeviceOwner Routes
    {
      path: '/owner/dashboard',
      element: (
        <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
          <OwnerDashboard />
        </ProtectedRoute>
      ),
    },
    {
      path: '/owner/teams',
      element: (
        <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
          <OwnerTeams />
        </ProtectedRoute>
      ),
    },
    {
      path: '/owner/users',
      element: (
        <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
          <OwnerUsers />
        </ProtectedRoute>
      ),
    },
    {
      path: '/owner/devices',
      element: (
        <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
          <OwnerDevices />
        </ProtectedRoute>
      ),
    },
    {
      path: '/owner/devices/:deviceId',
      element: (
        <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
          <OwnerDeviceDetail />
        </ProtectedRoute>
      ),
    },
    // DeviceUser Routes
    {
      path: '/user/dashboard',
      element: (
        <ProtectedRoute allowedRoles={['DEVICE_USER']}>
          <UserDashboard />
        </ProtectedRoute>
      ),
    },
    {
      path: '/user/devices',
      element: (
        <ProtectedRoute allowedRoles={['DEVICE_USER']}>
          <UserDevices />
        </ProtectedRoute>
      ),
    },
    {
      path: '/user/devices/:deviceId',
      element: (
        <ProtectedRoute allowedRoles={['DEVICE_USER']}>
          <UserDeviceDetail />
        </ProtectedRoute>
      ),
    },
    // Catch-all
    {
      path: '*',
      element: <Navigate to="/home" replace />,
    },
  ],
  {
    future: {
      v7_startTransition: true,
      v7_relativeSplatPath: true,
    },
  }
);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
