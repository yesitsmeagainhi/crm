package com.bothash.crmbot.configs;

import java.security.SecureRandom;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;


@KeycloakConfiguration
public class KeycloakSecurity extends KeycloakWebSecurityConfigurerAdapter {
	
	@Value("${keycloak.auth-server-url}")
	private String keycloackUrl;
	
	@Value("${keycloak.realm}")
	private String keycloackRealm;
	
	@Value("${crmbot-client-id}")
	private String crmbotClientId;
	
	@Value("${keycloack.admin.username}")
	private String adminUserName;
	
	@Value("${keycloack.admin.password}")
	private String adminPassword;

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(
                new SessionRegistryImpl());
    }

     @Override
    protected void configure(HttpSecurity http) throws Exception {
         super.configure(http);
        http.authorizeRequests()
        		.antMatchers("/bothash/**").permitAll()
                .antMatchers("/crmbot/**").hasAnyRole("user","admin","manager","counsellor","telecaller","supervisor")
                //.antMatchers("/admin/**").hasAnyRole("admin","supervisor")
                .anyRequest().permitAll();
        http.csrf().disable();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }
    
    @Override
	public void configure(WebSecurity web) throws Exception {
	    web
	       .ignoring()
	       .antMatchers("/authenticate",
                   "/swagger-resources/**",
                   "/swagger-ui/**",
                   "/swagger-ui/index.html" ,
                   "/v3/api-docs/**",
                   "/webjars/**",
                   "/swagger-ui/**","/resources/**", "/static/**", "/css/**", "/js/**","/js/plugin/**", "/img/**","service-worker.js");
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder(10,new SecureRandom());
    }
    
    @Bean
    Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloackUrl)
                .realm("master") // or your realm name
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli")
                .username(adminUserName)
                .password(adminPassword)
                .build();
    }
    
 
}
