import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import * as api from '../api/client';

export default function NotificationsPage() {
  const queryClient = useQueryClient();

  const notificationsQuery = useQuery({
    queryKey: ['notifications'],
    queryFn: () => api.getNotifications(false, 50),
    refetchInterval: 20000,
  });

  const markMutation = useMutation({
    mutationFn: (vars: { ids: string[] | null; all: boolean }) =>
      api.markRead(vars.ids, vars.all),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  const unreadCount = notificationsQuery.data?.unreadCount ?? 0;
  const items = notificationsQuery.data?.items ?? [];

  return (
    <div className="page">
      <div className="page-head">
        <h1>Уведомления</h1>
        <span className="badge">Непрочитанных: {unreadCount}</span>
        <span className="spacer" />
        <button
          type="button"
          className="btn btn-secondary"
          disabled={unreadCount === 0 || markMutation.isPending}
          onClick={() => markMutation.mutate({ ids: null, all: true })}
        >
          Прочитать все
        </button>
      </div>

      {notificationsQuery.isPending && <div className="card empty">Загрузка…</div>}
      {notificationsQuery.isError && (
        <div className="alert alert-error">
          {api.apiErrorMessage(notificationsQuery.error)}
        </div>
      )}

      {notificationsQuery.data !== undefined && items.length === 0 && (
        <div className="card empty">Уведомлений пока нет</div>
      )}

      {items.length > 0 && (
        <ul className="notif-list">
          {items.map((notification) => {
            const unread = notification.readAt === null;
            return (
              <li
                key={notification.id}
                className={unread ? 'card notif-card notif-unread' : 'card notif-card'}
              >
                <div className="notif-main">
                  <p className="notif-message">{notification.message}</p>
                  <span className="notif-date">
                    {new Date(notification.createdAt).toLocaleString('ru-RU')}
                  </span>
                </div>
                {unread && (
                  <button
                    type="button"
                    className="btn btn-ghost"
                    disabled={markMutation.isPending}
                    onClick={() =>
                      markMutation.mutate({ ids: [notification.id], all: false })
                    }
                  >
                    Прочитать
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}