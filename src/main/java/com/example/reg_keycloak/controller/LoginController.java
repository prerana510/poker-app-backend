package com.example.reg_keycloak.controller;

import com.example.reg_keycloak.security.JwtConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/auth")
public class LoginController {

    private final JwtConverter jwtConverter;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public LoginController(JwtConverter jwtConverter, JwtDecoder jwtDecoder) {
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
                "grant_type=password&client_secret=KXXZIpd6Habx8WgDtmsGT3clSFJccURk&client_id=myapp-api&username=%s&password=%s",
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

        // Use the JwtConverter to extract the roles from the decoded JWT
        Collection<String> roles = jwtConverter.convert(jwt).getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))  // Remove "ROLE_" prefix
                .collect(Collectors.toList());

        // Step 3: Return Access Token and Roles
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("roles", roles);

        return response;
    }}



