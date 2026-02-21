// firebase-messaging-sw.js
importScripts('https://www.gstatic.com/firebasejs/8.10.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.10.0/firebase-messaging.js');

firebase.initializeApp({
   apiKey: "AIzaSyAqLDa22F2dgs5UkWVxR9IlzJJVLLh8cWk",
   authDomain: "crmbot-89388.firebaseapp.com",
   projectId: "crmbot-89388",
   storageBucket: "crmbot-89388.firebasestorage.app",
   messagingSenderId: "45002731909",
   appId: "1:45002731909:web:6eebc03ab7a7de63c754ec",
   measurementId: "G-K5FJ2NQECE"
});

const messaging = firebase.messaging();

messaging.setBackgroundMessageHandler(function(payload) {
  const title = payload.data.title || 'New Notification';
  const options = {
    body: payload.data.body,
    icon: payload.data.icon || '/images/notification-icon.png',
  };
  return self.registration.showNotification(title, options);
});
