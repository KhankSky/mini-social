package com.example.social.dto.response.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResAuthError {
    private int status;
    private String error;
    private Object message;
    private String data;
}
