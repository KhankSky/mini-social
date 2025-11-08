package com.example.social.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReqUpdateUserDTO {

    @NotNull(message = "Id must not be null")
    private Long id;

    @NotBlank(message = "Username must not be blank")
    private String username;

    private String avatarUrl;
    private String bio;
}
