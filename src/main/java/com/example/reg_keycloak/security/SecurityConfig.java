package com.example.reg_keycloak.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ADMIN = "admin";
    public static final String USER = "user";
    private final JwtConverter jwtConverter;

    public SecurityConfig(JwtConverter jwtConverter) {
        this.jwtConverter = jwtConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((authz) ->
                authz.requestMatchers(HttpMethod.POST, "/api/auth/roles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/user/**").hasRole(USER)
                        .requestMatchers(HttpMethod.GET, "/api/admin-and-user/**").hasAnyRole(ADMIN,USER)
                        .anyRequest().authenticated());
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sess -> sess.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS));
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter((Converter<Jwt, ? extends AbstractAuthenticationToken>) jwtConverter)));

        return http.build();
    }
}