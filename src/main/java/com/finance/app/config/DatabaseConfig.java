package com.finance.app.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        // 1. Check for Railway's MYSQL_URL (mysql://...)
        String railwayUrl = System.getenv("MYSQL_URL");

        if (railwayUrl != null && !railwayUrl.isEmpty()) {
            // Replace "mysql://" with "jdbc:mariadb://" to use the MariaDB driver
            // Railway URL format: mysql://user:pass@host:port/db
            String jdbcUrl = railwayUrl.replace("mysql://", "jdbc:mariadb://");

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .driverClassName("org.mariadb.jdbc.Driver")
                    .build();
        }

        // 2. Fallback to standard Spring Boot "spring.datasource.*" properties
        // This ensures local development works using configuration from application.yml
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
