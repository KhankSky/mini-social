package com.example.social.dto.request.friend;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReqSendFriendRequestDTO {

    @NotNull(message = "Receiver ID must not be null")
    private Long receiverId;
}
