package com.example.reg_keycloak.controller;

import com.example.reg_keycloak.DTO.UserDTO;
import com.example.reg_keycloak.KeycloakConfig;
import com.example.reg_keycloak.service.KeyCloakService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static com.example.reg_keycloak.KeycloakConfig.realm;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(path = "/api/keycloak")
public class KeyCloakController {

    @Autowired
    KeyCloakService service;

    @PostMapping
    public String addUser(@RequestBody UserDTO userDTO){
        service.addUser(userDTO);
        return "User Added Successfully.";
    }

    @GetMapping(path = "/{userName}")
    public List<UserRepresentation> getUser(@PathVariable("userName") String userName){
        List<UserRepresentation> user = service.getUser(userName);
        return user;
    }

    @PutMapping(path = "/update/{userId}")
    public String updateUser(@PathVariable("userId") String userId, @RequestBody UserDTO userDTO){
        service.updateUser(userId, userDTO);
        return "User Details Updated Successfully.";
    }

    @DeleteMapping(path = "/delete/{userId}")
    public String deleteUser(@PathVariable("userId") String userId){
        service.deleteUser(userId);
        return "User Deleted Successfully.";
    }

    @PostMapping(value = "/users/{id}/roles/{roleName}")
    public Response createRole(@PathVariable("id") String id,
                               @PathVariable("roleName") String roleName) {
        Keycloak keycloak = KeycloakConfig.getInstance();
        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(id).roles().realmLevel().add(Arrays.asList(role));
        return Response.ok().build();
    }



    @GetMapping(path = "/verification-link/{userId}")
    public ResponseEntity<String> sendVerificationLink(@PathVariable("userId") String userId) {
        service.sendVerificationLink(userId);
        return ResponseEntity.ok("Verification Link Sent to Registered E-mail Id.");
    }

    @GetMapping(path = "/reset-password/{userId}")
    public ResponseEntity<String> sendResetPassword(@PathVariable("userId") String userId) {
        service.sendResetPassword(userId);
        return ResponseEntity.ok("Reset Password Link Sent Successfully to Registered E-mail Id.");
    }


}

