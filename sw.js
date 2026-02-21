self.addEventListener('push', evt => {
	debugger
  const data = evt.data?.json() || {};
  evt.waitUntil(
    self.registration.showNotification(data.title || 'Notification', {
      body: data.body,
      icon: '/icon.png',
      data: data.url
    })
  );
});

self.addEventListener('notificationclick', evt => {
  evt.notification.close();
  evt.waitUntil(
    clients.openWindow(evt.notification.data)
  );
});
