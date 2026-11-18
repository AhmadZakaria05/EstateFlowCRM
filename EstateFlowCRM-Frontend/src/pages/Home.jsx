import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Home.css';

export default function Home() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleBadgeClass = (role) => {
    switch (role) {
      case 'OVERVIEW_ADMIN': return 'badge-admin';
      case 'SALES_MANAGER': return 'badge-manager';
      case 'SALES_AGENT': return 'badge-agent';
      default: return '';
    }
  };

  const formatRole = (role) => {
    switch (role) {
      case 'OVERVIEW_ADMIN': return 'Admin';
      case 'SALES_MANAGER': return 'Sales Manager';
      case 'SALES_AGENT': return 'Sales Agent';
      default: return role;
    }
  };

  return (
    <div className="home-page">
      <nav className="home-nav">
        <div className="nav-brand">
          <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" width="36" height="36">
            <rect width="40" height="40" rx="10" fill="url(#nav-gradient)" />
            <path d="M12 28V16L20 10L28 16V28H22V22H18V28H12Z" fill="white" />
            <defs>
              <linearGradient id="nav-gradient" x1="0" y1="0" x2="40" y2="40">
                <stop stopColor="#6366f1" />
                <stop offset="1" stopColor="#8b5cf6" />
              </linearGradient>
            </defs>
          </svg>
          <span>EstateFlow CRM</span>
        </div>
        <div className="nav-actions">
          {user?.role === 'OVERVIEW_ADMIN' && (
            <button className="nav-btn register-btn" onClick={() => navigate('/register')}>
              <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
                <path d="M10 5a3 3 0 11-6 0 3 3 0 016 0zM1.615 16.428a1.224 1.224 0 01-.569-1.175 6.002 6.002 0 0111.908 0c.058.467-.172.92-.57 1.174A9.953 9.953 0 017 18a9.953 9.953 0 01-5.385-1.572zM16.25 5.75a.75.75 0 00-1.5 0v2h-2a.75.75 0 000 1.5h2v2a.75.75 0 001.5 0v-2h2a.75.75 0 000-1.5h-2v-2z" />
              </svg>
              Register User
            </button>
          )}
          <button className="nav-btn logout-btn" onClick={handleLogout}>
            <svg viewBox="0 0 20 20" fill="currentColor" width="18" height="18">
              <path fillRule="evenodd" d="M3 4.25A2.25 2.25 0 015.25 2h5.5A2.25 2.25 0 0113 4.25v2a.75.75 0 01-1.5 0v-2a.75.75 0 00-.75-.75h-5.5a.75.75 0 00-.75.75v11.5c0 .414.336.75.75.75h5.5a.75.75 0 00.75-.75v-2a.75.75 0 011.5 0v2A2.25 2.25 0 0110.75 18h-5.5A2.25 2.25 0 013 15.75V4.25z" clipRule="evenodd" />
              <path fillRule="evenodd" d="M19 10a.75.75 0 00-.75-.75H8.704l1.048-.943a.75.75 0 10-1.004-1.114l-2.5 2.25a.75.75 0 000 1.114l2.5 2.25a.75.75 0 101.004-1.114l-1.048-.943h9.546A.75.75 0 0019 10z" clipRule="evenodd" />
            </svg>
            Logout
          </button>
        </div>
      </nav>

      <main className="home-main">
        <div className="home-content">
          <div className="welcome-section">
            <div className="welcome-icon">
              <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect width="48" height="48" rx="14" fill="url(#welcome-gradient)" opacity="0.1" />
                <path d="M15 34V20L24 12L33 20V34H27V27H21V34H15Z" fill="url(#welcome-gradient)" />
                <defs>
                  <linearGradient id="welcome-gradient" x1="0" y1="0" x2="48" y2="48">
                    <stop stopColor="#6366f1" />
                    <stop offset="1" stopColor="#8b5cf6" />
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <h1 className="home-title">Home</h1>
            <p className="home-subtitle">
              Welcome back, <strong>{user?.fullName}</strong>
            </p>
            <span className={`role-badge ${getRoleBadgeClass(user?.role)}`}>
              {formatRole(user?.role)}
            </span>
          </div>

          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon" style={{ background: 'rgba(99, 102, 241, 0.1)', color: '#6366f1' }}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="24" height="24">
                  <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                  <polyline points="9,22 9,12 15,12 15,22" />
                </svg>
              </div>
              <div className="stat-info">
                <span className="stat-label">Properties</span>
                <span className="stat-value">—</span>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon" style={{ background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="24" height="24">
                  <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M23 21v-2a4 4 0 00-3-3.87" />
                  <path d="M16 3.13a4 4 0 010 7.75" />
                </svg>
              </div>
              <div className="stat-info">
                <span className="stat-label">Clients</span>
                <span className="stat-value">—</span>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-icon" style={{ background: 'rgba(245, 158, 11, 0.1)', color: '#f59e0b' }}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="24" height="24">
                  <rect x="1" y="4" width="22" height="16" rx="2" ry="2" />
                  <line x1="1" y1="10" x2="23" y2="10" />
                </svg>
              </div>
              <div className="stat-info">
                <span className="stat-label">Deals</span>
                <span className="stat-value">—</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

