import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import * as api from '../api/client';
import {
  ensurePushSubscription,
  isWatchEnabled,
  notificationPermission,
  notificationsSupported,
  pushSupported,
  requestNotificationPermission,
  setWatchEnabled,
  showSystemNotification,
  type NotificationPermissionState,
} from './webNotifications';

const BASE_TITLE = 'Link Tracker';
const POLL_INTERVAL_MS = 15000;
const MAX_BURST = 3;

interface NotificationsContextValue {
  supported: boolean;
  permission: NotificationPermissionState;
  watchEnabled: boolean;
  unreadCount: number;
  enableWatch: () => Promise<void>;
  disableWatch: () => void;
}

const NotificationsContext = createContext<NotificationsContextValue | null>(null);

export function NotificationWatcher({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [permission, setPermission] = useState<NotificationPermissionState>(() =>
    notificationPermission(),
  );
  const [watchEnabled, setWatchEnabledState] = useState<boolean>(() => isWatchEnabled());
  const [unreadCount, setUnreadCount] = useState(0);

  const seenIdsRef = useRef<Set<string> | null>(null);
  const pushRegisteredRef = useRef(false);

  const notificationsQuery = useQuery({
    queryKey: ['notifications'],
    queryFn: () => api.getNotifications(false, 50),
    refetchInterval: POLL_INTERVAL_MS,
    refetchIntervalInBackground: true,
  });

  useEffect(() => {
    if (pushRegisteredRef.current || notificationPermission() !== 'granted') {
      return;
    }
    pushRegisteredRef.current = true;

    let cancelled = false;
    (async () => {
      try {
        if (!pushSupported()) {
          return;
        }
        const { publicKey } = await api.getPushPublicKey();
        if (cancelled || publicKey.length === 0) {
          return;
        }
        const subscription = await ensurePushSubscription(publicKey);
        if (cancelled || !subscription.endpoint || !subscription.keys) {
          return;
        }
        await api.subscribePush(subscription.endpoint, {
          p256dh: subscription.keys.p256dh,
          auth: subscription.keys.auth,
        });
      } catch {
        // если пуш недоступен
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    const data = notificationsQuery.data;
    if (data === undefined) {
      return;
    }

    const seen = seenIdsRef.current;
    if (seen === null) {
      seenIdsRef.current = new Set(data.items.map((item) => item.id));
      setUnreadCount(data.unreadCount);
      return;
    }

    const fresh = data.items.filter((item) => !seen.has(item.id));
    for (const item of data.items) {
      seen.add(item.id);
    }
    setUnreadCount(data.unreadCount);

    if (
      fresh.length === 0 ||
      !isWatchEnabled() ||
      notificationPermission() !== 'granted'
    ) {
      return;
    }

    const openNotifications = () => navigate('/notifications');
    for (const item of fresh.slice(0, MAX_BURST)) {
      showSystemNotification('Новое обновление по ссылке', item.message, {
        tag: item.id,
        onClick: openNotifications,
      });
    }
    if (fresh.length > MAX_BURST) {
      showSystemNotification(
        'Новые обновления',
        `Ещё новых уведомлений: ${fresh.length - MAX_BURST}`,
        { tag: 'link-tracker-aggregate', onClick: openNotifications },
      );
    }
  }, [notificationsQuery.data, navigate]);

  useEffect(() => {
    document.title = unreadCount > 0 ? `(${unreadCount}) ${BASE_TITLE}` : BASE_TITLE;
  }, [unreadCount]);

  const enableWatch = useCallback(async () => {
    const result = await requestNotificationPermission();
    setPermission(result);
    if (result !== 'granted') {
      return;
    }
    setWatchEnabled(true);
    setWatchEnabledState(true);

    try {
      if (pushSupported()) {
        const { publicKey } = await api.getPushPublicKey();
        if (publicKey.length > 0) {
          const subscription = await ensurePushSubscription(publicKey);
          if (subscription.endpoint && subscription.keys) {
            await api.subscribePush(subscription.endpoint, {
              p256dh: subscription.keys.p256dh,
              auth: subscription.keys.auth,
            });
          }
        }
      }
    } catch {
      // без Web Push остаются локальные уведомления при открытой вкладке
    }

    showSystemNotification(
      'Уведомления включены',
      'Вы будете получать оповещения о новых обновлениях по ссылкам',
      { tag: 'link-tracker-enabled', onClick: () => navigate('/notifications') },
    );
  }, [navigate]);

  const disableWatch = useCallback(() => {
    setWatchEnabled(false);
    setWatchEnabledState(false);

    (async () => {
      try {
        if (pushSupported()) {
          const registration = await navigator.serviceWorker.getRegistration();
          const subscription = await registration?.pushManager.getSubscription();
          if (subscription) {
            await api.unsubscribePush(subscription.endpoint).catch(() => undefined);
            await subscription.unsubscribe();
          }
        }
      } catch {
        // не критично
      }
    })();
  }, []);

  const value = useMemo<NotificationsContextValue>(
    () => ({
      supported: notificationsSupported(),
      permission,
      watchEnabled,
      unreadCount,
      enableWatch,
      disableWatch,
    }),
    [permission, watchEnabled, unreadCount, enableWatch, disableWatch],
  );

  return <NotificationsContext.Provider value={value}>{children}</NotificationsContext.Provider>;
}

export function useNotifications(): NotificationsContextValue {
  const ctx = useContext(NotificationsContext);
  if (ctx === null) {
    throw new Error('useNotifications должен использоваться внутри <NotificationWatcher>');
  }
  return ctx;
}
