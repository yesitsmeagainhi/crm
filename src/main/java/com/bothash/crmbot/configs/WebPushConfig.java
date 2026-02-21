package com.bothash.crmbot.configs;

import java.security.GeneralSecurityException;
import java.security.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import nl.martijndwars.webpush.PushService;

@Configuration
public class WebPushConfig {

  // replace with your VAPID keys (see §7)
  private static final String VAPID_PUBLIC  = "BGwlv_PnnimH8XKXj5MebSIbIgAQqI2Pl-pc1xi9HjQrbHX2rAkfZ_my80140TKJHQKdIpMGi21vwRJjEUeP2kQ";
  private static final String VAPID_PRIVATE = "b1Q1rXt9fbr983mMqi5ScB1thMFi2ZsfMF-_X5i6vp0";

  static {
      // Register BouncyCastle provider only once
      if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
          Security.addProvider(new BouncyCastleProvider());
      }
  }
  @Bean
  public PushService pushService() {
    try {
		return new PushService(VAPID_PUBLIC,
		                       VAPID_PRIVATE,
		                       "mailto:admin@example.com");
	} catch (GeneralSecurityException e) {
		throw new RuntimeException("Failed to create PushService bean", e);
	}
  }
}
