package com.library.util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AuditLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + "/audit.log";
    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public enum Level {
        INFO, WARN, ERROR
    }

    public static void log(String username, String action, String detail) {
        log(Level.INFO, username, action, detail);
    }

    public static void log(Level level, String username, String action, String detail) {
        String timestamp = LocalDateTime.now().format(LOG_FORMATTER);
        String line = String.format("[%s] [%s] 用户: %s | 操作: %s | 详情: %s",
                timestamp, level, username, action, detail);
        writeToFile(line);
    }

    private static void writeToFile(String line) {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("审计日志写入失败: " + e.getMessage());
        }
    }

    public static List<String> readAllLogs() {
        List<String> logs = new ArrayList<>();
        File file = new File(LOG_FILE);
        if (!file.exists()) return logs;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    logs.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("读取审计日志失败: " + e.getMessage());
        }
        return logs;
    }

    public static List<String> searchLogs(String keyword) {
        return readAllLogs().stream()
                .filter(line -> line.contains(keyword))
                .collect(Collectors.toList());
    }

    public static List<String> searchLogsByUser(String username) {
        return readAllLogs().stream()
                .filter(line -> line.contains("用户: " + username))
                .collect(Collectors.toList());
    }

    public static List<String> searchLogsByDate(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        return readAllLogs().stream()
                .filter(line -> line.startsWith("[" + dateStr))
                .collect(Collectors.toList());
    }

    public static List<String> searchLogsByAction(String action) {
        return readAllLogs().stream()
                .filter(line -> line.contains("操作: " + action))
                .collect(Collectors.toList());
    }

    public static Map<String, Integer> getStats() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("总记录数", 0);
        stats.put("借书", 0);
        stats.put("还书", 0);
        stats.put("添加书籍", 0);
        stats.put("更新书籍", 0);
        stats.put("删除书籍", 0);
        stats.put("更新用户", 0);
        stats.put("删除用户", 0);

        for (String line : readAllLogs()) {
            stats.put("总记录数", stats.get("总记录数") + 1);
            if (line.contains("操作: 借书")) stats.put("借书", stats.get("借书") + 1);
            else if (line.contains("操作: 还书")) stats.put("还书", stats.get("还书") + 1);
            else if (line.contains("操作: 添加书籍")) stats.put("添加书籍", stats.get("添加书籍") + 1);
            else if (line.contains("操作: 更新书籍")) stats.put("更新书籍", stats.get("更新书籍") + 1);
            else if (line.contains("操作: 删除书籍")) stats.put("删除书籍", stats.get("删除书籍") + 1);
            else if (line.contains("操作: 更新用户")) stats.put("更新用户", stats.get("更新用户") + 1);
            else if (line.contains("操作: 删除用户")) stats.put("删除用户", stats.get("删除用户") + 1);
        }
        return stats;
    }

    public static void exportLogs(String exportPath) {
        List<String> logs = readAllLogs();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportPath))) {
            writer.write("图书馆管理系统 - 操作审计日志导出");
            writer.newLine();
            writer.write("导出时间: " + LocalDateTime.now().format(LOG_FORMATTER));
            writer.newLine();
            writer.write("共 " + logs.size() + " 条记录");
            writer.newLine();
            writer.write("=".repeat(60));
            writer.newLine();
            for (String log : logs) {
                writer.write(log);
                writer.newLine();
            }
            System.out.println("日志已导出到: " + exportPath);
        } catch (IOException e) {
            System.err.println("导出日志失败: " + e.getMessage());
        }
    }

    public static void cleanOldLogs(int days) {
        File file = new File(LOG_FILE);
        if (!file.exists()) return;
        List<String> keepLogs = readAllLogs().stream()
                .filter(line -> {
                    try {
                        String dateStr = line.substring(1, 11);
                        LocalDate logDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                        return logDate.isAfter(LocalDate.now().minusDays(days));
                    } catch (Exception e) {
                        return true;
                    }
                })
                .collect(Collectors.toList());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String log : keepLogs) {
                writer.write(log);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("清理日志失败: " + e.getMessage());
        }
        System.out.println("已清理 " + (readAllLogs().size() - keepLogs.size()) + " 条过期日志");
    }

    public static int getLogCount() {
        return readAllLogs().size();
    }
}
