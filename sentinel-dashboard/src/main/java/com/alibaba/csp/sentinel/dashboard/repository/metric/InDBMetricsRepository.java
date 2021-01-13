package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.jpa.MetricJpaRepository;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.jpa.MetricPO;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author hbj
 */
@Component("inDBMetricsRepository")
public class InDBMetricsRepository implements MetricsRepository<MetricEntity> {
    @Autowired
    private MetricJpaRepository metricsJpaRepository;

    @Override
    public void save(MetricEntity metric) {
        if (metric == null || StringUtil.isBlank(metric.getApp())) {
            return;
        }
        MetricPO metricPO = new MetricPO();
        BeanUtils.copyProperties(metric, metricPO);
        metricsJpaRepository.save(metricPO);
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        List<MetricPO> metricPOList = new ArrayList<>();
        metrics.forEach(metric -> {
            MetricPO metricPO = new MetricPO();
            BeanUtils.copyProperties(metric, metricPO);
            metricPOList.add(metricPO);
        });
        metricsJpaRepository.saveAll(metricPOList);
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        if (StringUtil.isBlank(app)) {
            return new ArrayList<>();
        }
        if (StringUtil.isBlank(resource)) {
            return new ArrayList<>();
        }
        List<MetricPO> metricPOList = metricsJpaRepository.findByAppAndResourceAndTimestampBetween(app, resource, new Date(startTime), new Date(endTime));
        if (CollectionUtils.isEmpty(metricPOList)) {
            return new ArrayList<>();
        }
        return metricPOList.stream().map(metricPO -> {
            MetricEntity metric = new MetricEntity();
            BeanUtils.copyProperties(metricPO, metric);
            return metric;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> listResourcesOfApp(String app, long startTime) {
        if (StringUtil.isBlank(app)) {
            return new ArrayList<>();
        }
//        long startTime = System.currentTimeMillis() - 1000 * 60;
        List<MetricPO> metricPOList = metricsJpaRepository.findByAppAndTimestampAfter(app, new Date(startTime));
        if (CollectionUtils.isEmpty(metricPOList)) {
            return new ArrayList<>();
        }
        List<MetricEntity> metrics = metricPOList.stream().map(metricPO -> {
            MetricEntity metric = new MetricEntity();
            BeanUtils.copyProperties(metricPO, metric);
            return metric;
        }).collect(Collectors.toList());
        Map<String, MetricEntity> resourceCount = new ConcurrentHashMap<>(32);
        for (MetricEntity metricEntity : metrics) {
            String resource = metricEntity.getResource();
            if (resourceCount.containsKey(resource)) {
                MetricEntity oldEntity = resourceCount.get(resource);
                oldEntity.addPassQps(metricEntity.getPassQps());
                oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
                oldEntity.addBlockQps(metricEntity.getBlockQps());
                oldEntity.addExceptionQps(metricEntity.getExceptionQps());
                oldEntity.addCount(1);
            } else {
                resourceCount.put(resource, MetricEntity.copyOf(metricEntity));
            }
        }
        // Order by last minute b_qps DESC.
        return resourceCount.entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    MetricEntity e1 = o1.getValue();
                    MetricEntity e2 = o2.getValue();
                    int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                    if (t != 0) {
                        return t;
                    }
                    return e2.getPassQps().compareTo(e1.getPassQps());
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
