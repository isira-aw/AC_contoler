export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  DEVICE_OWNER: 'DEVICE_OWNER',
  DEVICE_USER: 'DEVICE_USER',
};

export const getRolePath = (role) => {
  switch (role) {
    case ROLES.SUPER_ADMIN:
      return '/superadmin';
    case ROLES.DEVICE_OWNER:
      return '/owner';
    case ROLES.DEVICE_USER:
      return '/user';
    default:
      return '/login';
  }
};

export const getRoleLabel = (role) => {
  switch (role) {
    case ROLES.SUPER_ADMIN:
      return 'Super Admin';
    case ROLES.DEVICE_OWNER:
      return 'Device Owner';
    case ROLES.DEVICE_USER:
      return 'Device User';
    default:
      return 'Unknown';
  }
};

export const getUser = () => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }
  return null;
};

export const getRole = () => {
  const user = getUser();
  return user ? user.role : null;
};

export const isAuthenticated = () => {
  return !!localStorage.getItem('token') && !!getUser();
};

export const hasRole = (allowedRoles) => {
  const user = getUser();
  if (!user) return false;
  if (Array.isArray(allowedRoles)) {
    return allowedRoles.includes(user.role);
  }
  return user.role === allowedRoles;
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = '/login';
};
