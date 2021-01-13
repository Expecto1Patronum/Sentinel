package com.alibaba.csp.sentinel.dashboard.config.datasource;

import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.util.JSONUtils;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author hbj
 * @date 2021/1/13 5:40 下午
 */
@Configuration
public class MySQLDatasourceConfig {
    @Autowired
    private ConfigService configService;

    @Bean
    public DataSource dataSource() throws NacosException {
        MySQLEntity config = NacosConfigUtil.getDatasourceConfigFromNacos(configService, "dev");
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(config.getUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        return dataSource;
    }
}
