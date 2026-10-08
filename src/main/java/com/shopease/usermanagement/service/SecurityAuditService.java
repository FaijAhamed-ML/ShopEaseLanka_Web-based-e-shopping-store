package com.shopease.usermanagement.service;

import com.shopease.usermanagement.entity.LoginActivity;
import com.shopease.usermanagement.entity.LoginStatus;
import com.shopease.usermanagement.entity.User;
import com.shopease.usermanagement.repository.LoginActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityAuditService {

    private final LoginActivityRepository loginActivityRepository;

    public SecurityAuditService(LoginActivityRepository loginActivityRepository) {
        this.loginActivityRepository = loginActivityRepository;
    }

    @Transactional
    public LoginActivity recordLoginAttempt(User user, String ipAddress, boolean isSuccess) {
        LoginStatus status = isSuccess ? LoginStatus.SUCCESS : LoginStatus.FAILURE;
        boolean suspicious = false;

        if (!isSuccess && user != null) {
            // Check consecutive failed attempts
            List<LoginActivity> recentLogs = loginActivityRepository.findTop5ByUser_UserIdOrderByLoginTimeDesc(user.getUserId());
            int consecutiveFailures = 1; // including current one
            for (LoginActivity log : recentLogs) {
                if (log.getStatus() == LoginStatus.FAILURE) {
                    consecutiveFailures++;
                } else {
                    break;
                }
            }
            if (consecutiveFailures >= 3) {
                suspicious = true;
            }
        }

        // Check irregular login hours (e.g. 1 AM - 4 AM) as extra heuristic
        int hour = LocalDateTime.now().getHour();
        if (hour >= 1 && hour <= 4 && !isSuccess) {
            suspicious = true;
        }

        LoginActivity activity = new LoginActivity(user, ipAddress, status, suspicious);
        return loginActivityRepository.save(activity);
    }

    public List<LoginActivity> getUserLogs(Integer userId) {
        return loginActivityRepository.findByUser_UserIdOrderByLoginTimeDesc(userId);
    }

    public List<LoginActivity> getAllLogs() {
        return loginActivityRepository.findAllByOrderByLoginTimeDesc();
    }

    public List<LoginActivity> getSuspiciousLogs() {
        return loginActivityRepository.findBySuspiciousFlagTrueOrderByLoginTimeDesc();
    }

    @Transactional
    public LoginActivity toggleSuspiciousFlag(Integer activityId) {
        LoginActivity activity = loginActivityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Login activity not found"));
        activity.setSuspiciousFlag(!Boolean.TRUE.equals(activity.getSuspiciousFlag()));
        return loginActivityRepository.save(activity);
    }

    @Transactional
    public void deleteLog(Integer activityId) {
        if (!loginActivityRepository.existsById(activityId)) {
            throw new RuntimeException("Login activity log not found: " + activityId);
        }
        loginActivityRepository.deleteById(activityId);
    }

    @Transactional
    public long clearOldLogs(int days) {
        if (days <= 0) {
            long count = loginActivityRepository.count();
            loginActivityRepository.deleteAll();
            return count;
        } else {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
            return loginActivityRepository.deleteByLoginTimeBefore(cutoff);
        }
    }
}
