import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useNotifications } from '../notifications/NotificationWatcher';

export default function AccountPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { supported, permission, watchEnabled, enableWatch, disableWatch } = useNotifications();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const handleWatchToggle = () => {
    if (watchEnabled) {
      disableWatch();
    } else {
      void enableWatch();
    }
  };

  const renderNotificationCard = () => {
    if (!supported) {
      return <p className="muted">Этот браузер не поддерживает системные уведомления</p>;
    }
    if (permission === 'denied') {
      return (
        <>
          <p className="muted">
            Уведомления заблокированы в настройках браузера. Разрешите их для этого сайта,
            чтобы получать оповещения о новых обновлениях.
          </p>
          <button type="button" className="btn btn-secondary" disabled>
            🔕 Уведомления заблокированы
          </button>
        </>
      );
    }
    return (
      <>
        <p className="muted">
          {watchEnabled
            ? 'Уведомления включены: оповещения приходят даже когда вкладка неактивна или закрыта.'
            : 'Включите уведомления, чтобы узнавать о новых обновлениях без открытой страницы.'}
        </p>
        <button
          type="button"
          className={watchEnabled ? 'btn btn-secondary' : 'btn btn-primary'}
          onClick={handleWatchToggle}
        >
          {watchEnabled ? '🔔 Отключить уведомления' : '🔔 Включить уведомления'}
        </button>
      </>
    );
  };

  return (
    <div className="page page-narrow">
      <div className="page-head">
        <h1>Аккаунт</h1>
      </div>

      <div className="card">
        <div className="kv">
          <span className="kv-key">Email</span>
          <span className="kv-value">{user?.email || '—'}</span>
        </div>
        <div className="kv">
          <span className="kv-key">Telegram привязан</span>
          <span className="kv-value">{user?.telegramLinked ? 'да' : 'нет'}</span>
        </div>
      </div>

      <div className="card">
        <h2 className="card-title">Уведомления браузера</h2>
        {renderNotificationCard()}
      </div>

      <div className="card empty">
        <p>Привязка Telegram будет доступна позже</p>
      </div>

      <div>
        <button type="button" className="btn btn-danger" onClick={handleLogout}>
          Выйти
        </button>
      </div>
    </div>
  );
}