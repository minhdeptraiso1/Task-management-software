package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.request.dashboard.MyTaskSearchRequest;
import com.project.taskmanagement.dto.response.dashboard.MyDashboardResponse;
import com.project.taskmanagement.dto.response.dashboard.MyTaskPageResponse;
import com.project.taskmanagement.dto.response.dashboard.MyTimeSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface MyDashboardService {

    MyDashboardResponse getMyDashboard();

    MyTaskPageResponse getMyTasks(
            MyTaskSearchRequest request,
            Pageable pageable
    );

    MyTimeSummaryResponse getMyTimeSummary();
}