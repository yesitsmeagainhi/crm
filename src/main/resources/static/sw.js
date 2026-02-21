self.addEventListener('push', function(event) {
  const data = event.data ? event.data.json() : {};
  
  // Default title and body if not provided in the push message
  const title = data.title || 'Notification';
  const body = data.body || 'You have a new notification!';
  const url = data.url || '/';

  event.waitUntil(
    self.registration.showNotification(title, {
      body: body,
      icon: '/icon.png', // Optional
      data: { url: url }
    })
  );
});

self.addEventListener('notificationclick', function(event) {
  event.notification.close();
  event.waitUntil(
    clients.openWindow(event.notification.data.url)
  );
});
self.addEventListener('install', function(event) {
    console.log('Service Worker installed');
});

self.addEventListener('activate', function(event) {
    console.log('Service Worker activated');
});
