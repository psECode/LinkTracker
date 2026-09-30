import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function AccountPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
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