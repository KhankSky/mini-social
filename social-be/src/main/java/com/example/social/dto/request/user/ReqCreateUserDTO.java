package com.example.social.dto.request.user;

import com.example.social.config.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReqCreateUserDTO {

    @NotBlank(message = "Email must not be blank")
    @Pattern(regexp = Constants.LOGIN_REGEX, message = "Invalid email")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;

    private String username;
    private String avatarUrl;
    private String bio;
}
