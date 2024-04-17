package com.slow3586.highload_0224_skv.repository.write;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jdbc.core.convert.BasicJdbcConverter;
import org.springframework.data.jdbc.core.convert.DefaultJdbcTypeFactory;
import org.springframework.data.jdbc.core.convert.JdbcConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.convert.RelationResolver;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.dialect.PostgresDialect;
import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableJdbcRepositories(
    basePackages = "com.slow3586.highload_0224_skv.repository.write",
    transactionManagerRef = "userWriteTransactionManager",
    jdbcOperationsRef = "userWriteJdbcOperations"
)
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    JdbcRepositoriesAutoConfiguration.class
})
public class UserWriteRepositoryConfig {
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties userWriteDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public HikariDataSource userWriteDataSource(@Qualifier("userWriteDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class)
            .build();
    }

    @Bean(name = "userWriteTransactionManager")
    PlatformTransactionManager userWriteTransactionManager(@Qualifier("userWriteDataSource") DataSource employeesDataSource) {
        return new JdbcTransactionManager(employeesDataSource);
    }

    @Bean
    NamedParameterJdbcOperations userWriteJdbcOperations(@Qualifier("userWriteDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Primary
    @Autowired
    @Bean(name = "userWriteJdbcTemplate")
    public JdbcTemplate userWriteJdbcTemplate(@Qualifier("userWriteDataSource") DataSource userWriteDataSource) {
        return new JdbcTemplate(userWriteDataSource);
    }

    @Bean
    Dialect jdbcDialect() {
        return PostgresDialect.INSTANCE;
    }

    @Bean
    JdbcCustomConversions customConversions() {
        return new JdbcCustomConversions();
    }

    @Bean
    JdbcMappingContext jdbcMappingContext(Optional<NamingStrategy> namingStrategy, JdbcCustomConversions customConversions) {
        JdbcMappingContext mappingContext = new JdbcMappingContext(namingStrategy.orElse(NamingStrategy.INSTANCE));
        mappingContext.setSimpleTypeHolder(customConversions.getSimpleTypeHolder());
        return mappingContext;
    }

    @Bean
    JdbcConverter jdbcConverter(JdbcMappingContext mappingContext,
                                @Qualifier("userWriteJdbcOperations") NamedParameterJdbcOperations jdbcOperationsDataBase1,
                                @Lazy RelationResolver relationResolver,
                                @Qualifier("customConversions") JdbcCustomConversions conversions,
                                Dialect dialect) {
        DefaultJdbcTypeFactory jdbcTypeFactory = new DefaultJdbcTypeFactory(jdbcOperationsDataBase1.getJdbcOperations());
        return new BasicJdbcConverter(mappingContext, relationResolver, conversions, jdbcTypeFactory,
            dialect.getIdentifierProcessing());
    }
}
