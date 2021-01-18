package com.alibaba.csp.sentinel.dashboard.datasource.entity.influxdb;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author hbj
 * @date 2021/1/13 4:13 下午
 */
@Measurement(name = "sentinel_metric")
public class MetricPO implements Serializable {

    private static final long serialVersionUID = -499670661853195313L;

    @Column(name = "time")
    private Instant time;

    /**
     * id，主键
     */
    @Column(name = "id")
    private Long id;

    /**
     * 创建时间
     */
    @Column(name = "gmtCreate")
    private Long gmtCreate;

    /**
     * 修改时间
     */
    @Column(name = "gmtModified")
    private Long gmtModified;

    /**
     * 应用名称
     */
    @Column(name = "app")
    private String app;

    /**
     * 资源名称
     */
    @Column(name = "resource")
    private String resource;

    /**
     * 通过qps
     */
    @Column(name = "passQps")
    private Long passQps;

    /**
     * 成功qps
     */
    @Column(name = "successQps")
    private Long successQps;

    /**
     * 限流qps
     */
    @Column(name = "blockQps")
    private Long blockQps;

    /**
     * 发送异常的次数
     */
    @Column(name = "exceptionQps")
    private Long exceptionQps;

    /**
     * 所有successQps的rt的和
     */
    @Column(name = "rt")
    private Double rt;

    /**
     * 本次聚合的总条数
     */
    @Column(name = "count")
    private Integer count;

    /**
     * 资源的hashCode
     */
    @Column(name = "resourceCode")
    private Integer resourceCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Long getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Long gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(Long successQps) {
        this.successQps = successQps;
    }

    public Long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(Long blockQps) {
        this.blockQps = blockQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public Double getRt() {
        return rt;
    }

    public void setRt(Double rt) {
        this.rt = rt;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(Integer resourceCode) {
        this.resourceCode = resourceCode;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }
}
