package com.alibaba.csp.sentinel.dashboard.datasource.entity.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author hbj
 * @date 2021/1/13 4:16 下午
 */
public interface MetricJpaRepository extends JpaRepository<MetricPO, Long> {
    List<MetricPO> findByAppAndResourceAndTimestampBetween(String app, String resource, Date startTime, Date endTime);

    List<MetricPO> findByAppAndTimestampAfter(String app, Date startTime);
}
