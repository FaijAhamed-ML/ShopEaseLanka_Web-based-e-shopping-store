package com.shopease.usermanagement.repository;

import com.shopease.usermanagement.entity.LoginActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginActivityRepository extends JpaRepository<LoginActivity, Integer> {
    List<LoginActivity> findTop5ByUser_UserIdOrderByLoginTimeDesc(Integer userId);
    List<LoginActivity> findByUser_UserIdOrderByLoginTimeDesc(Integer userId);
    List<LoginActivity> findAllByOrderByLoginTimeDesc();
    List<LoginActivity> findBySuspiciousFlagTrueOrderByLoginTimeDesc();
    long deleteByLoginTimeBefore(LocalDateTime cutoff);
    void deleteByUser_UserId(Integer userId);
}
