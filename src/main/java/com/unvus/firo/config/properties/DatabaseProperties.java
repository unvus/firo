package com.unvus.firo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component("firoDatabaseProperties")
public class DatabaseProperties {
    @Value("${unvus.firo.database.driver-class-name:com.zaxxer.hikari.HikariDataSource}")
    private String driverClassName;

    @Value("${unvus.firo.database.url:jdbc:h2:file:~/.firo/h2db/db/firo;DB_CLOSE_DELAY=-1}")
    private String url;

    @Value("${unvus.firo.database.username:firouser}")
    private String username;

    @Value("${unvus.firo.database.password:#{null}}")
    private String password;
}
