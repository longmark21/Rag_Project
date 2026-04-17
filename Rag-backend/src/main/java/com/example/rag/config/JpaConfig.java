package com.example.rag.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.context.annotation.FilterType;

import com.example.rag.repository.ConversationRepository;
import com.example.rag.repository.DocumentRepository;
import com.example.rag.repository.MessageRepository;
import com.example.rag.repository.VectorChunkRepository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
public class JpaConfig {

    // MySQL 数据源的 JPA 配置
    @Configuration
    @EnableJpaRepositories(
        basePackages = {
            "com.example.rag.repository",
            "com.example.rag.repository.impl"
        },
        includeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                ConversationRepository.class,
                DocumentRepository.class,
                MessageRepository.class
            }
        ),
        entityManagerFactoryRef = "primaryEntityManagerFactory",
        transactionManagerRef = "primaryTransactionManager"
    )
    public static class PrimaryJpaConfig {
        
        @Primary
        @Bean(name = "primaryEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource dataSource) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("hibernate.hbm2ddl.auto", "create");
            properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            properties.put("hibernate.show_sql", "true");
            properties.put("hibernate.format_sql", "true");
            
            return builder
                .dataSource(dataSource)
                .packages("com.example.rag.entity")
                .persistenceUnit("primaryPersistenceUnit")
                .properties(properties)
                .build();
        }

        @Primary
        @Bean(name = "primaryTransactionManager")
        public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
            return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
        }
    }

    // PostgreSQL 数据源的 JPA 配置
    @Configuration
    @EnableJpaRepositories(
        basePackages = {
            "com.example.rag.repository",
            "com.example.rag.repository.impl"
        },
        includeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {
                VectorChunkRepository.class
            }
        ),
        entityManagerFactoryRef = "vectorEntityManagerFactory",
        transactionManagerRef = "vectorTransactionManager"
    )
    public static class VectorJpaConfig {
        
        @Bean(name = "vectorEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean vectorEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("vectorDataSource") DataSource dataSource) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("hibernate.hbm2ddl.auto", "none");
            properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            properties.put("hibernate.show_sql", "true");
            properties.put("hibernate.format_sql", "true");
            
            return builder
                .dataSource(dataSource)
                .packages("com.example.rag.entity")
                .persistenceUnit("vectorPersistenceUnit")
                .properties(properties)
                .build();
        }

        @Bean(name = "vectorTransactionManager")
        public PlatformTransactionManager vectorTransactionManager(
            @Qualifier("vectorEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
            return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
        }
    }
}
