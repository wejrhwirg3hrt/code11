package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class DataBackupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.storage.backup.path:E:/code11/backups}")
    private String backupPath;

    @Value("${app.storage.backup.enabled:true}")
    private boolean backupEnabled;

    /**
     * 创建数据备份
     */
    public void createBackup() {
        if (!backupEnabled) {
            return;
        }

        try {
            // 创建备份目录
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupDir = backupPath + File.separator + "backup_" + timestamp;
            Files.createDirectories(Paths.get(backupDir));

            // 备份用户数据
            backupTableData("users", backupDir + File.separator + "users.csv");
            
            // 备份视频数据
            backupTableData("videos", backupDir + File.separator + "videos.csv");
            
            // 备份登录日志
            backupTableData("user_login_logs", backupDir + File.separator + "login_logs.csv");
            
            // 备份分类数据
            backupTableData("categories", backupDir + File.separator + "categories.csv");

            System.out.println("✅ 数据备份完成: " + backupDir);
        } catch (Exception e) {
            System.err.println("❌ 数据备份失败: " + e.getMessage());
        }
    }

    /**
     * 备份表数据到CSV文件
     */
    private void backupTableData(String tableName, String filePath) throws IOException {
        try {
            // 获取表结构
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                "ORDER BY ORDINAL_POSITION", tableName);

            // 获取表数据
            List<Map<String, Object>> data = jdbcTemplate.queryForList("SELECT * FROM " + tableName);

            // 写入CSV文件
            try (FileWriter writer = new FileWriter(filePath)) {
                // 写入表头
                if (!columns.isEmpty()) {
                    writer.write(String.join(",", columns.stream()
                        .map(col -> col.get("COLUMN_NAME").toString())
                        .toArray(String[]::new)));
                    writer.write("\n");
                }

                // 写入数据
                for (Map<String, Object> row : data) {
                    String[] values = columns.stream()
                        .map(col -> {
                            Object value = row.get(col.get("COLUMN_NAME"));
                            return value != null ? value.toString().replace(",", "\\,") : "";
                        })
                        .toArray(String[]::new);
                    writer.write(String.join(",", values));
                    writer.write("\n");
                }
            }

            System.out.println("✅ 表 " + tableName + " 备份完成: " + filePath);
        } catch (Exception e) {
            System.err.println("❌ 表 " + tableName + " 备份失败: " + e.getMessage());
        }
    }

    /**
     * 从备份恢复数据
     */
    public void restoreFromBackup(String backupDir) {
        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                throw new IOException("备份目录不存在: " + backupDir);
            }

            // 恢复用户数据
            restoreTableData("users", backupDir + File.separator + "users.csv");
            
            // 恢复视频数据
            restoreTableData("videos", backupDir + File.separator + "videos.csv");
            
            // 恢复登录日志
            restoreTableData("user_login_logs", backupDir + File.separator + "login_logs.csv");
            
            // 恢复分类数据
            restoreTableData("categories", backupDir + File.separator + "categories.csv");

            System.out.println("✅ 数据恢复完成: " + backupDir);
        } catch (Exception e) {
            System.err.println("❌ 数据恢复失败: " + e.getMessage());
        }
    }

    /**
     * 从CSV文件恢复表数据
     */
    private void restoreTableData(String tableName, String filePath) throws IOException {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                System.out.println("⚠️ 备份文件不存在: " + filePath);
                return;
            }

            List<String> lines = Files.readAllLines(path);
            if (lines.isEmpty()) {
                return;
            }

            // 解析表头
            String[] headers = lines.get(0).split(",");

            // 清空表数据
            jdbcTemplate.execute("DELETE FROM " + tableName);

            // 插入数据
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                if (values.length == headers.length) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("INSERT INTO ").append(tableName).append(" (");
                    sql.append(String.join(",", headers));
                    sql.append(") VALUES (");
                    
                    for (int j = 0; j < values.length; j++) {
                        if (j > 0) sql.append(",");
                        if (values[j].isEmpty() || "null".equalsIgnoreCase(values[j])) {
                            sql.append("NULL");
                        } else {
                            sql.append("'").append(values[j].replace("'", "''")).append("'");
                        }
                    }
                    sql.append(")");
                    
                    jdbcTemplate.execute(sql.toString());
                }
            }

            System.out.println("✅ 表 " + tableName + " 恢复完成");
        } catch (Exception e) {
            System.err.println("❌ 表 " + tableName + " 恢复失败: " + e.getMessage());
        }
    }

    /**
     * 获取备份列表
     */
    public List<String> getBackupList() {
        try {
            Path backupDir = Paths.get(backupPath);
            if (!Files.exists(backupDir)) {
                return List.of();
            }

            return Files.list(backupDir)
                .filter(Files::isDirectory)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.startsWith("backup_"))
                .sorted()
                .toList();
        } catch (Exception e) {
            System.err.println("❌ 获取备份列表失败: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 删除备份
     */
    public boolean deleteBackup(String backupName) {
        try {
            Path backupDir = Paths.get(backupPath, backupName);
            if (Files.exists(backupDir)) {
                Files.walk(backupDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("删除文件失败: " + path);
                        }
                    });
                System.out.println("✅ 备份删除成功: " + backupName);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ 删除备份失败: " + e.getMessage());
            return false;
        }
    }
} 