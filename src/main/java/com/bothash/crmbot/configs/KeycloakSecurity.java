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


@KeycloakConfiguration
public class KeycloakSecurity extends KeycloakWebSecurityConfigurerAdapter {

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
                .antMatchers("/crmbot/**").hasAnyRole("user","admin","manager","counsellor","telecaller")
                .antMatchers("/admin/**").hasAnyRole("admin")
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
}
