//package com.bothash.crmbot.service.impl;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.Message;
//import com.google.firebase.messaging.Notification;
//import com.google.firebase.messaging.WebpushConfig;
//import com.google.firebase.messaging.WebpushNotification;
//
//import lombok.extern.slf4j.Slf4j;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//@Service
//@Slf4j
//public class PushNotificationService {
//
//    @Value("${firebase.credentials.file}")
//    private String firebaseCredentialsPath;
//
//    public void initializeFirebase() throws IOException {
//    	InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream();
//
//
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
//
//        FirebaseApp.initializeApp(options);
//    }
//
//    public void sendPushNotification(String token, String title, String body) throws Exception {
//    	 // Make sure Firebase is initialized before sending the notification
//        try {
//        	initializeFirebase();
//        }catch (Exception e) {
//			log.error("unable to initialize firebasae");
//		}
//    	
//        
//        
//        // Web Push notification
//        WebpushNotification webpushNotification = WebpushNotification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .setIcon("https://www.vmedify.com/logo192.png") // Replace with your favicon or logo
//                .build();
//
//        WebpushConfig webpushConfig = WebpushConfig.builder()
//                .setNotification(webpushNotification)
//                .putHeader("Urgency", "high") // Boost delivery
//                .build();
//
//        // Message object for web only
//        Message message = Message.builder()
//                .setToken(token)
//                .setWebpushConfig(webpushConfig)
//                .build();
//
//
//        // Send notification
//        String response = FirebaseMessaging.getInstance().send(message);
//        System.out.println("Successfully sent message: " + response);
//    }
//}
