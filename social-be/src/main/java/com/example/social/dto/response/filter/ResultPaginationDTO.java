package com.example.social.dto.response.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResultPaginationDTO {
    private Pagination pagination;
    private Object result;
}
