package com.blog.repository;

import com.blog.entity.DevServiceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevServiceConfigRepository extends JpaRepository<DevServiceConfig, Long> {
    List<DevServiceConfig> findByProjectIdOrderByStartOrderAsc(Long projectId);
    List<DevServiceConfig> findByProjectIdAndEnabledTrueOrderByStartOrderAsc(Long projectId);
    void deleteByProjectId(Long projectId);
}
