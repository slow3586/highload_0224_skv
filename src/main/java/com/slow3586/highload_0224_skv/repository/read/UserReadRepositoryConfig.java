package com.slow3586.highload_0224_skv.repository.read;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJdbcRepositories(
    basePackages = "com.slow3586.highload_0224_skv.repository.read",
    transactionManagerRef = "userReadTransactionManager",
    jdbcOperationsRef = "userReadJdbcOperations"
)
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    JdbcRepositoriesAutoConfiguration.class
})
public class UserReadRepositoryConfig {
    @Bean
    @ConfigurationProperties("spring.read-datasource")
    public DataSourceProperties userReadDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public HikariDataSource userReadDataSource(@Qualifier("userReadDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class)
            .build();
    }

    @Bean(name = "userReadTransactionManager")
    PlatformTransactionManager userReadTransactionManager(@Qualifier("userReadDataSource") DataSource employeesDataSource) {
        return new JdbcTransactionManager(employeesDataSource);
    }

    @Bean
    NamedParameterJdbcOperations userReadJdbcOperations(@Qualifier("userReadDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

}
