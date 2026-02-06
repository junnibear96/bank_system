package com.finance.app.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() throws URISyntaxException {
        // 1. Check for Railway's MYSQL_URL (mysql://...)
        String railwayUrl = System.getenv("MYSQL_URL");

        if (railwayUrl != null && !railwayUrl.isEmpty()) {
            // Replace "mysql://" with "jdbc:mariadb://"
            // Railway URL format: mysql://user:pass@host:port/db
            String jdbcUrl = railwayUrl.replace("mysql://", "jdbc:mariadb://");

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .driverClassName("org.mariadb.jdbc.Driver")
                    .build();
        }

        // 2. Fallback to standard Spring Boot "spring.datasource.*" properties
        // (This enables local development to still work via application.yml)
        return DataSourceBuilder.create().build();
    }
}
