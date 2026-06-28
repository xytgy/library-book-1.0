package com.library.console;

import com.library.util.AuditLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AuditMenu {

    public void showMenu() {
        while (true) {
            ConsoleHelper.clearScreen();
            ConsoleHelper.printTitle("审计日志");
            ConsoleHelper.printMenuLine("1. 查看全部日志");
            ConsoleHelper.printMenuLine("2. 按用户搜索");
            ConsoleHelper.printMenuLine("3. 按日期搜索");
            ConsoleHelper.printMenuLine("4. 按操作类型搜索");
            ConsoleHelper.printMenuLine("5. 统计分析");
            ConsoleHelper.printMenuLine("6. 导出日志");
            ConsoleHelper.printMenuLine("0. 返回主菜单");
            ConsoleHelper.printMenuEnd();

            int choice = ConsoleHelper.readInt("  请选择: ", 0, 6);

            switch (choice) {
                case 1 -> viewAllLogs();
                case 2 -> searchByUser();
                case 3 -> searchByDate();
                case 4 -> searchByAction();
                case 5 -> showStats();
                case 6 -> exportLogs();
                case 0 -> { return; }
            }
        }
    }

    private void viewAllLogs() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("全部审计日志");
        List<String> logs = AuditLogger.readAllLogs();
        displayLogs(logs);
        ConsoleHelper.waitForKey();
    }

    private void searchByUser() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("按用户搜索");
        String username = ConsoleHelper.readString("  用户名: ", true);
        List<String> logs = AuditLogger.searchLogsByUser(username);
        System.out.println("  搜索结果（" + logs.size() + " 条）：");
        displayLogs(logs);
        ConsoleHelper.waitForKey();
    }

    private void searchByDate() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("按日期搜索");
        String dateStr = ConsoleHelper.readString("  日期(yyyy-MM-dd): ", true);
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> logs = AuditLogger.searchLogsByDate(date);
            System.out.println("  搜索结果（" + logs.size() + " 条）：");
            displayLogs(logs);
        } catch (Exception e) {
            ConsoleHelper.printError("日期格式错误");
        }
        ConsoleHelper.waitForKey();
    }

    private void searchByAction() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("按操作类型搜索");
        System.out.println("  可选操作: 借书, 还书, 添加书籍, 更新书籍, 删除书籍, 更新用户, 删除用户");
        String action = ConsoleHelper.readString("  操作类型: ", true);
        List<String> logs = AuditLogger.searchLogsByAction(action);
        System.out.println("  搜索结果（" + logs.size() + " 条）：");
        displayLogs(logs);
        ConsoleHelper.waitForKey();
    }

    private void showStats() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("审计日志统计");
        Map<String, Integer> stats = AuditLogger.getStats();
        System.out.println();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            System.out.printf("  %-12s: %d 条%n", entry.getKey(), entry.getValue());
        }
        System.out.println();
        ConsoleHelper.waitForKey();
    }

    private void exportLogs() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("导出日志");
        String path = ConsoleHelper.readString("  导出路径(如 export.log): ", true);
        AuditLogger.exportLogs(path);
        ConsoleHelper.waitForKey();
    }

    private void displayLogs(List<String> logs) {
        if (logs.isEmpty()) {
            ConsoleHelper.printInfo("暂无日志记录");
            return;
        }
        int start = 0;
        int pageSize = 15;
        while (true) {
            ConsoleHelper.clearScreen();
            int end = Math.min(start + pageSize, logs.size());
            int totalPages = (int) Math.ceil((double) logs.size() / pageSize);
            int currentPage = (start / pageSize) + 1;
            System.out.println("  共" + logs.size() + "条, 第" + currentPage + "/" + totalPages + "页");
            System.out.println();
            for (int i = start; i < end; i++) {
                System.out.println("  " + logs.get(i));
            }
            if (totalPages <= 1) {
                ConsoleHelper.printInfo("  按回车键返回");
                ConsoleHelper.readString("");
                return;
            }
            StringBuilder hint = new StringBuilder("  ");
            if (currentPage > 1) hint.append("[P]上一页 ");
            if (currentPage < totalPages) hint.append("[N]下一页 ");
            hint.append("[Q]返回");
            ConsoleHelper.printInfo(hint.toString());
            String input = ConsoleHelper.readString("  选择: ").toLowerCase();
            switch (input) {
                case "p" -> { if (start >= pageSize) start -= pageSize; }
                case "n" -> { if (end < logs.size()) start += pageSize; }
                case "q" -> { return; }
            }
        }
    }
}
