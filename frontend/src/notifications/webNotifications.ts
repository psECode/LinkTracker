const ENABLED_KEY = 'lt_notify_enabled';

export type NotificationPermissionState = 'granted' | 'denied' | 'default' | 'unsupported';

export function notificationsSupported(): boolean {
  return typeof window !== 'undefined' && 'Notification' in window;
}

export function notificationPermission(): NotificationPermissionState {
  if (!notificationsSupported()) {
    return 'unsupported';
  }
  return Notification.permission;
}

export function isWatchEnabled(): boolean {
  try {
    return localStorage.getItem(ENABLED_KEY) === '1';
  } catch {
    return false;
  }
}

export function setWatchEnabled(enabled: boolean): void {
  try {
    if (enabled) {
      localStorage.setItem(ENABLED_KEY, '1');
    } else {
      localStorage.removeItem(ENABLED_KEY);
    }
  } catch {
    // localStorage может быть недоступен — игнорируем
  }
}

export async function requestNotificationPermission(): Promise<NotificationPermissionState> {
  if (!notificationsSupported()) {
    return 'unsupported';
  }
  try {
    return await Notification.requestPermission();
  } catch {
    return notificationPermission();
  }
}

export function showSystemNotification(
  title: string,
  body: string,
  options?: { tag?: string; onClick?: () => void },
): boolean {
  if (!notificationsSupported() || Notification.permission !== 'granted') {
    return false;
  }
  try {
    const notification = new Notification(title, {
      body,
      tag: options?.tag,
    });
    notification.onclick = () => {
      window.focus();
      options?.onClick?.();
      notification.close();
    };
    return true;
  } catch {
    return false;
  }
}

export function serviceWorkerSupported(): boolean {
  return typeof navigator !== 'undefined' && 'serviceWorker' in navigator;
}

export function pushSupported(): boolean {
  return (
    serviceWorkerSupported() &&
    'PushManager' in window &&
    notificationsSupported()
  );
}

function urlBase64ToUint8Array(base64UrlString: string): Uint8Array<ArrayBuffer> {
  const padding = '='.repeat((4 - (base64UrlString.length % 4)) % 4);
  const base64 = (base64UrlString + padding).replace(/-/g, '+').replace(/_/g, '/');
  const raw = atob(base64);
  const output = new Uint8Array(new ArrayBuffer(raw.length));
  for (let i = 0; i < raw.length; i++) {
    output[i] = raw.charCodeAt(i);
  }
  return output;
}

export async function ensurePushSubscription(publicKey: string): Promise<PushSubscriptionJSON> {
  const registration = await navigator.serviceWorker.register('/sw.js');
  const readyRegistration = await navigator.serviceWorker.ready;

  let subscription = await readyRegistration.pushManager.getSubscription();
  if (subscription === null) {
    subscription = await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: urlBase64ToUint8Array(publicKey),
    });
  }
  return subscription.toJSON();
}
