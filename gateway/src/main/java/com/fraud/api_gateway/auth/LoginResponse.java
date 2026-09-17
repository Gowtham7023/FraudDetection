package com.fraud.api_gateway.auth;

public record LoginResponse(String token, String tokenType, long expiresIn) {
}
