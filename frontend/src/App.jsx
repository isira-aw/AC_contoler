import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
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

function App() {
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route
          path="/login"
          element={
            <AuthRoute>
              <Login />
            </AuthRoute>
          }
        />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/reset-password/:token" element={<ResetPasswordConfirm />} />

        {/* SuperAdmin Routes */}
        <Route
          path="/superadmin/dashboard"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <SuperAdminDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/superadmin/device-owners"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <SuperAdminDeviceOwners />
            </ProtectedRoute>
          }
        />
        <Route
          path="/superadmin/devices"
          element={
            <ProtectedRoute allowedRoles={['SUPER_ADMIN']}>
              <SuperAdminDevices />
            </ProtectedRoute>
          }
        />

        {/* DeviceOwner Routes */}
        <Route
          path="/owner/dashboard"
          element={
            <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
              <OwnerDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/teams"
          element={
            <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
              <OwnerTeams />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/users"
          element={
            <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
              <OwnerUsers />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/devices"
          element={
            <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
              <OwnerDevices />
            </ProtectedRoute>
          }
        />
        <Route
          path="/owner/devices/:deviceId"
          element={
            <ProtectedRoute allowedRoles={['DEVICE_OWNER']}>
              <OwnerDeviceDetail />
            </ProtectedRoute>
          }
        />

        {/* DeviceUser Routes */}
        <Route
          path="/user/dashboard"
          element={
            <ProtectedRoute allowedRoles={['DEVICE_USER']}>
              <UserDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/user/devices"
          element={
            <ProtectedRoute allowedRoles={['DEVICE_USER']}>
              <UserDevices />
            </ProtectedRoute>
          }
        />
        <Route
          path="/user/devices/:deviceId"
          element={
            <ProtectedRoute allowedRoles={['DEVICE_USER']}>
              <UserDeviceDetail />
            </ProtectedRoute>
          }
        />

        {/* Default Redirect */}
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
