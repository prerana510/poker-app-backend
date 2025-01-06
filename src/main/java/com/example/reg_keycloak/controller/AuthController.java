package com.example.reg_keycloak.controller;

import com.example.reg_keycloak.security.JwtConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtConverter jwtConverter;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public AuthController(JwtConverter jwtConverter, JwtDecoder jwtDecoder) {
        this.jwtConverter = jwtConverter;
        this.jwtDecoder = jwtDecoder;
    }
    @PostMapping("/roles")
    public Map<String, Object> getRolesAndToken(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        // Step 1: Fetch Access Token
        String tokenEndpoint = "http://localhost:8080/realms/myapp/protocol/openid-connect/token";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = String.format(
                "grant_type=password&client_id=myapp-api&username=%s&password=%s",
                username,
                password
        );

        HttpEntity<String> tokenRequest = new HttpEntity<>(body, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                tokenEndpoint, HttpMethod.POST, tokenRequest, Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to fetch access token: " + tokenResponse.getBody());
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // Step 2: Decode JWT and Extract Roles
        Jwt jwt = jwtDecoder.decode(accessToken);

        Collection<String> roles = jwtConverter.extractResourceRoles(jwt).stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .toList();

        // Step 3: Return Access Token and Roles
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("roles", roles);

        return response;
    }


//    @PostMapping("/roles")
//    public Map<String, Object> getRolesAndToken(@RequestBody Map<String, String> request) {
//        String username = request.get("username");
//        String password = request.get("password");
//
//        if (username == null || password == null) {
//            throw new IllegalArgumentException("Username and password are required.");
//        }
//
//        // Step 1: Fetch Access Token
//        String tokenEndpoint = "http://localhost:8080/realms/myapp/protocol/openid-connect/token";
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        Map<String, String> body = new HashMap<>();
//        body.put("grant_type", "password");
//        body.put("client_id", "myapp-api");
//        //body.put("client_secret", "your-client-secret");
//        body.put("username", username);
//        body.put("password", password);
//
//        HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(body, headers);
//
//        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, tokenRequest, Map.class);
//
//        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException("Failed to fetch access token: " + tokenResponse.getBody());
//        }
//
//        String accessToken = (String) tokenResponse.getBody().get("access_token");
//
//        // Step 2: Decode JWT and Extract Roles
//        Jwt jwt = jwtDecoder.decode(accessToken);
//
//        Collection<String> roles = jwtConverter.extractResourceRoles(jwt).stream()
//                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
//                .toList();
//
//        // Step 3: Return Access Token and Roles
//        Map<String, Object> response = new HashMap<>();
//        response.put("access_token", accessToken);
//        response.put("roles", roles);
//
//        return response;
//    }
}

