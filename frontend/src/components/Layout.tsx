import { Link, Navigate, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { NotificationWatcher, useNotifications } from '../notifications/NotificationWatcher';

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
    <NotificationWatcher>
      <div className="layout">
        <Header onLogout={handleLogout} />
        <main className="main">
          <Outlet />
        </main>
      </div>
    </NotificationWatcher>
  );
}

function Header({ onLogout }: { onLogout: () => void }) {
  const { supported, permission, watchEnabled, unreadCount, enableWatch, disableWatch } =
    useNotifications();

  const handleBellClick = () => {
    if (watchEnabled) {
      disableWatch();
    } else {
      void enableWatch();
    }
  };

  const renderBell = () => {
    if (!supported) {
      return null;
    }
    if (permission === 'denied') {
      return (
        <button
          type="button"
          className="btn btn-ghost btn-bell btn-bell-blocked"
          disabled
          title="Уведомления браузера заблокированы — разрешите их в настройках сайта"
        >
          🔕
        </button>
      );
    }
    if (permission === 'default') {
      return (
        <button
          type="button"
          className="btn btn-secondary btn-bell"
          onClick={handleBellClick}
          title="Включить системные уведомления о новых обновлениях"
        >
          🔔 Включить уведомления
        </button>
      );
    }
    return (
      <button
        type="button"
        className={watchEnabled ? 'btn btn-ghost btn-bell btn-bell-on' : 'btn btn-ghost btn-bell'}
        aria-pressed={watchEnabled}
        onClick={handleBellClick}
        title={
          watchEnabled
            ? 'Системные уведомления включены — нажмите, чтобы отключить'
            : 'Системные уведомления выключены — нажмите, чтобы включить'
        }
      >
        {watchEnabled ? '🔔' : '🔕'}
      </button>
    );
  };

  return (
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
          {unreadCount > 0 && (
            <span className="nav-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
          )}
        </NavLink>
        <NavLink
          to="/account"
          className={({ isActive }) => (isActive ? 'nav-link nav-link-active' : 'nav-link')}
        >
          Аккаунт
        </NavLink>
      </nav>
      {renderBell()}
      <button type="button" className="btn btn-ghost" onClick={onLogout}>
        Выйти
      </button>
    </header>
  );
}
