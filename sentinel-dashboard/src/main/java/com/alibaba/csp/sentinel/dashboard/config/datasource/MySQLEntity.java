package com.alibaba.csp.sentinel.dashboard.config.datasource;

/**
 * @author hbj
 * @date 2021/1/13 5:32 下午
 */
public class MySQLEntity {
    private String url;
    private String username;
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MySQLEntity() {
    }

    public MySQLEntity(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }
}
