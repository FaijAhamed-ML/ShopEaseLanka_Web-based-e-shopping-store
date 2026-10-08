package com.shopease.usermanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_activity")
public class LoginActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Integer activityId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "login_time")
    private LocalDateTime loginTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoginStatus status;

    @Column(name = "suspicious_flag")
    private Boolean suspiciousFlag = false;

    public LoginActivity() {}

    public LoginActivity(User user, String ipAddress, LoginStatus status, Boolean suspiciousFlag) {
        this.user = user;
        this.ipAddress = ipAddress;
        this.status = status;
        this.suspiciousFlag = suspiciousFlag != null ? suspiciousFlag : false;
        this.loginTime = LocalDateTime.now();
    }

    public Integer getActivityId() { return activityId; }
    public void setActivityId(Integer activityId) { this.activityId = activityId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LoginStatus getStatus() { return status; }
    public void setStatus(LoginStatus status) { this.status = status; }

    public Boolean getSuspiciousFlag() { return suspiciousFlag; }
    public void setSuspiciousFlag(Boolean suspiciousFlag) { this.suspiciousFlag = suspiciousFlag; }
}
