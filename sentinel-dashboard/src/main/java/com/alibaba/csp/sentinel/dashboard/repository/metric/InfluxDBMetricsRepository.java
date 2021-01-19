package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.influxdb.MetricPO;
import com.alibaba.csp.sentinel.dashboard.util.InfluxDBUtils;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hbj
 */
@Component("influxDBMetricsRepository")
public class InfluxDBMetricsRepository implements MetricsRepository<MetricEntity> {

    /**
     * 时间格式
     */
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 数据库名称
     */
    private static final String SENTINEL_DATABASE = "oem_monitor";

    /**
     * 数据表名称
     */
    private static final String METRIC_MEASUREMENT = "sentinel_metric";

    @Override
    public void save(MetricEntity metric) {
        if (metric == null || StringUtil.isBlank(metric.getApp())) {
            return;
        }
        InfluxDBUtils.insert(SENTINEL_DATABASE, new InfluxDBUtils.InfluxDBInsertCallback() {
            @Override
            public void doCallBack(String database, InfluxDB influxDB) {
                if (metric.getId() == null) {
                    metric.setId(System.currentTimeMillis());
                }
                doSave(influxDB, metric);
            }
        });
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        Iterator<MetricEntity> iterator = metrics.iterator();
        boolean next = iterator.hasNext();
        if (!next) {
            return;
        }
        InfluxDBUtils.insert(SENTINEL_DATABASE, new InfluxDBUtils.InfluxDBInsertCallback() {
            @Override
            public void doCallBack(String database, InfluxDB influxDB) {
                while (iterator.hasNext()) {
                    MetricEntity metric = iterator.next();
                    if (metric.getId() == null) {
                        metric.setId(System.currentTimeMillis());
                    }
                    doSave(influxDB, metric);
                }
            }
        });
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        if (StringUtil.isBlank(app)) {
            return new ArrayList<>();
        }
        if (StringUtil.isBlank(resource)) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM " + METRIC_MEASUREMENT);
        sql.append(" WHERE app=$app");
        sql.append(" AND resource=$resource");
        sql.append(" AND time>=$startTime");
        sql.append(" AND time<=$endTime");

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("app", app);
        paramMap.put("resource", resource);
        paramMap.put("startTime", startTime * 1000000);
        paramMap.put("endTime", endTime * 1000000);

        List<MetricPO> metricPOList = InfluxDBUtils.queryList(SENTINEL_DATABASE, sql.toString(), paramMap, MetricPO.class);

        if (CollectionUtils.isEmpty(metricPOList)) {
            return new ArrayList<>();
        }
        return metricPOList.stream().map(this::convertToMetricEntity).collect(Collectors.toList());
    }

    @Override
    public List<String> listResourcesOfApp(String app, long startTime) {
        if (StringUtil.isBlank(app)) {
            return new ArrayList<>();
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM " + METRIC_MEASUREMENT);
        sql.append(" WHERE app=$app");
        sql.append(" AND time>=$startTime");

        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put("app", app);
        paramMap.put("startTime", startTime * 1000000);

        List<MetricPO> metricPOList = InfluxDBUtils.queryList(SENTINEL_DATABASE, sql.toString(), paramMap, MetricPO.class);

        if (CollectionUtils.isEmpty(metricPOList)) {
            return new ArrayList<>();
        }
        List<MetricEntity> metrics = metricPOList.stream().map(this::convertToMetricEntity).collect(Collectors.toList());
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

    private MetricEntity convertToMetricEntity(MetricPO metricPO) {
        MetricEntity metricEntity = new MetricEntity();

        metricEntity.setId(metricPO.getId());
        metricEntity.setGmtCreate(metricPO.getGmtCreate() == null ? null : new Date(metricPO.getGmtCreate()));
        metricEntity.setGmtModified(metricPO.getGmtModified() == null ? null : new Date(metricPO.getGmtCreate()));
        metricEntity.setApp(metricPO.getApp());
        // 查询数据减8小时(InfluxDB使用的是UTC时间)
        metricEntity.setTimestamp(Date.from(metricPO.getTime()));
        metricEntity.setResource(metricPO.getResource());
        metricEntity.setPassQps(metricPO.getPassQps());
        metricEntity.setSuccessQps(metricPO.getSuccessQps());
        metricEntity.setBlockQps(metricPO.getBlockQps());
        metricEntity.setExceptionQps(metricPO.getExceptionQps());
        metricEntity.setRt(metricPO.getRt());
        metricEntity.setCount(metricPO.getCount());

        return metricEntity;
    }

    private void doSave(InfluxDB influxDB, MetricEntity metric) {
        influxDB.write(Point.measurement(METRIC_MEASUREMENT)
                // 因InfluxDB默认UTC时间，按北京时间算写入数据加8小时
                .time(metric.getTimestamp().getTime(), TimeUnit.MILLISECONDS)
                .tag("app", metric.getApp())
                .tag("resource", metric.getResource())
                .addField("id", metric.getId())
                .addField("gmtCreate", metric.getGmtCreate().getTime())
                .addField("gmtModified", metric.getGmtModified().getTime())
                .addField("passQps", metric.getPassQps())
                .addField("successQps", metric.getSuccessQps())
                .addField("blockQps", metric.getBlockQps())
                .addField("exceptionQps", metric.getExceptionQps())
                .addField("rt", metric.getRt())
                .addField("count", metric.getCount())
                .addField("resourceCode", metric.getResourceCode())
                .build());
    }
}
