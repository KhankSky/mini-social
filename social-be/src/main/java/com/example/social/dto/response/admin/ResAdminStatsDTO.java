package com.example.social.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResAdminStatsDTO {
    private Long totalUsers;
    private Long totalPosts;
    private Long totalComments;
    private Long activeToday;
    private Long newUsersThisWeek;
}
