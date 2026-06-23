package com.project.taskmanagement.config;

import com.project.taskmanagement.security.CurrentUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(CurrentUser.username());
    }
}
