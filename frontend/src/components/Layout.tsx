import { Link, Navigate, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Layout() {
  const { user, loading, logout } = useAuth();
  const navigate = useNavigate();

  if (loading) {
    return (
      <div className="loading-screen">
        <p className="loading">Загрузка…</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="layout">
      <header className="header">
        <Link to="/links" className="logo">
          <span className="logo-mark" aria-hidden="true">🔗</span>
          <span>Link Tracker</span>
        </Link>
        <nav className="nav" aria-label="Основная навигация">
          <NavLink
            to="/links"
            className={({ isActive }) => (isActive ? 'nav-link nav-link-active' : 'nav-link')}
          >
            Ссылки
          </NavLink>
          <NavLink
            to="/notifications"
            className={({ isActive }) => (isActive ? 'nav-link nav-link-active' : 'nav-link')}
          >
            Уведомления
          </NavLink>
          <NavLink
            to="/account"
            className={({ isActive }) => (isActive ? 'nav-link nav-link-active' : 'nav-link')}
          >
            Аккаунт
          </NavLink>
        </nav>
        <button type="button" className="btn btn-ghost" onClick={handleLogout}>
          Выйти
        </button>
      </header>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
