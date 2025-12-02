package com.example.social.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResLoginDTO {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
//    private ResUserLoginDTO user;
}
