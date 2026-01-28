package com.hvac.iot.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private final String userId;
    private final String email;
    private final String role;

    public UUID getUserIdAsUUID() {
        return UUID.fromString(userId);
    }
}
