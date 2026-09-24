package com.altis.library.dashboard.models.dtos;

import com.altis.library.loans.models.dtos.LoanResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardResponse {

    private long totalUsers;
    private long totalBooks;
    private long totalPublishers;
    private long activeLoans;
    private long overdueLoans;
    private long returnedLoans;
    private long pendingLoans;
    private List<LoanResponse> latestLoans;
}
