package com.example.rag.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Configuration
public class DatabaseInitializer {

    private final DataSource vectorDataSource;

    public DatabaseInitializer(@Qualifier("vectorDataSource") DataSource vectorDataSource) {
        this.vectorDataSource = vectorDataSource;
        log.info("DatabaseInitializer 注入的数据源: {}", vectorDataSource.getClass().getName());
    }

    @Bean
    public ApplicationRunner initializeVectorDatabase() {
        return args -> {
            try (Connection connection = vectorDataSource.getConnection()) {
                // 检查连接信息
                String url = connection.getMetaData().getURL();
                String catalog = connection.getCatalog();
                log.info("连接到 PostgreSQL 数据库: url={}, catalog={}", url, catalog);
                
                // 先删除表，以便重新创建
                log.info("删除 vector_chunks 表");
                try (java.sql.Statement statement = connection.createStatement()) {
                    statement.execute("DROP TABLE IF EXISTS vector_chunks;");
                } catch (Exception e) {
                    log.warn("删除 vector_chunks 表失败，可能表不存在", e);
                }
                
                // 初始化表结构
                log.info("开始初始化 PostgreSQL 向量数据库表结构...");
                // 读取并执行 SQL 脚本
                executeSqlScript(connection, "init-vector-db.sql");
                log.info("PostgreSQL 向量数据库表结构初始化完成");
            } catch (Exception e) {
                log.error("初始化 PostgreSQL 向量数据库失败: {}", e.getMessage(), e);
            }
        };
    }

    private boolean checkTableExists(Connection connection, String tableName) throws SQLException {
        var metaData = connection.getMetaData();
        var resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
        boolean exists = resultSet.next();
        log.info("检查表 {} 是否存在: {}", tableName, exists);
        return exists;
    }

    private void executeSqlScript(Connection connection, String scriptPath) throws IOException, SQLException {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        if (resource.exists()) {
            ScriptUtils.executeSqlScript(connection, resource);
        } else {
            // 如果在 classpath 中找不到，尝试从项目根目录读取
            java.io.File file = new java.io.File(scriptPath);
            if (file.exists()) {
                ScriptUtils.executeSqlScript(connection, new org.springframework.core.io.FileSystemResource(file));
            } else {
                log.error("SQL 脚本文件不存在: {}", scriptPath);
                throw new IOException("SQL 脚本文件不存在: " + scriptPath);
            }
        }
    }
}