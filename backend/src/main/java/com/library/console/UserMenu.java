package com.library.console;

import com.library.exception.BusinessException;
import com.library.model.User;
import com.library.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserMenu {

    private final UserService userService;

    public UserMenu(UserService userService) {
        this.userService = userService;
    }

    public void showMenu(Long currentUserId) {
        while (true) {
            ConsoleHelper.clearScreen();
            ConsoleHelper.printTitle("用户管理");
            ConsoleHelper.printMenuLine("1. 查看用户列表");
            ConsoleHelper.printMenuLine("2. 搜索用户");
            ConsoleHelper.printMenuLine("3. 查看用户详情");
            ConsoleHelper.printMenuLine("4. 编辑用户");
            ConsoleHelper.printMenuLine("5. 删除用户");
            ConsoleHelper.printMenuLine("0. 返回主菜单");
            ConsoleHelper.printMenuEnd();

            int choice = ConsoleHelper.readInt("  请选择: ", 0, 5);

            switch (choice) {
                case 1 -> listUsers(false);
                case 2 -> listUsers(true);
                case 3 -> showUserDetail();
                case 4 -> updateUser(currentUserId);
                case 5 -> deleteUser();
                case 0 -> { return; }
            }
        }
    }

    private void listUsers(boolean isSearch) {
        String search = null;
        if (isSearch) {
            ConsoleHelper.clearScreen();
            ConsoleHelper.printTitle("搜索用户");
            search = ConsoleHelper.readString("  关键词(用户名/姓名/邮箱): ", false);
        }

        int page = 1;
        int pageSize = 10;

        while (true) {
            ConsoleHelper.clearScreen();
            Map<String, Object> result = userService.getUsers(search, page, pageSize);
            @SuppressWarnings("unchecked")
            List<User> records = (List<User>) result.get("records");
            long total = (long) result.get("total");
            int totalPages = (int) Math.ceil((double) total / pageSize);

            ConsoleHelper.printTitle("用户列表 (共" + total + "条, 第" + page + "/" + Math.max(totalPages, 1) + "页)");

            if (records.isEmpty()) {
                ConsoleHelper.printInfo("暂无数据");
            } else {
                String[] headers = {"ID", "用户名", "姓名", "邮箱", "角色", "状态"};
                int[] widths = {4, 12, 8, 20, 8, 8};
                List<String[]> rows = new ArrayList<>();
                for (User user : records) {
                    rows.add(new String[]{
                        String.valueOf(user.getId()),
                        user.getUsername(),
                        user.getName(),
                        user.getEmail() != null ? user.getEmail() : "",
                        "admin".equals(user.getRole()) ? "管理员" : "普通用户",
                        user.getStatus() == 1 ? "正常" : "禁用"
                    });
                }
                ConsoleHelper.printTable(headers, rows, widths);
            }

            if (totalPages <= 1) {
                ConsoleHelper.printInfo("  按回车键返回");
                ConsoleHelper.readString("");
                return;
            }

            StringBuilder hint = new StringBuilder("  ");
            if (page > 1) hint.append("[P]上一页 ");
            if (page < totalPages) hint.append("[N]下一页 ");
            hint.append("[Q]返回");
            ConsoleHelper.printInfo(hint.toString());

            String input = ConsoleHelper.readString("  选择: ").toLowerCase();
            switch (input) {
                case "p" -> { if (page > 1) page--; }
                case "n" -> { if (page < totalPages) page++; }
                case "q" -> { return; }
                default -> ConsoleHelper.printError("无效输入");
            }
        }
    }

    private void showUserDetail() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("查看用户详情");
        Long id = (long) ConsoleHelper.readInt("  用户ID: ");

        try {
            User user = userService.getUserById(id);
            System.out.println();
            System.out.println("  ID:     " + user.getId());
            System.out.println("  用户名: " + user.getUsername());
            System.out.println("  姓名:   " + user.getName());
            System.out.println("  邮箱:   " + (user.getEmail() != null ? user.getEmail() : "无"));
            System.out.println("  角色:   " + ("admin".equals(user.getRole()) ? "管理员" : "普通用户"));
            System.out.println("  状态:   " + (user.getStatus() == 1 ? "正常" : "禁用"));
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }

    private void updateUser(Long currentUserId) {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("编辑用户");
        Long id = (long) ConsoleHelper.readInt("  用户ID: ");

        try {
            User user = userService.getUserById(id);
            System.out.println("  当前信息:");
            System.out.println("  用户名: " + user.getUsername());
            System.out.println("  姓名: " + user.getName() + "  邮箱: " + user.getEmail());
            System.out.println("  角色: " + user.getRole() + "  状态: " + user.getStatus());
            System.out.println();

            String name = ConsoleHelper.readString("  姓名[" + user.getName() + "]: ", false);
            if (name.isEmpty()) name = user.getName();
            String email = ConsoleHelper.readString("  邮箱[" + user.getEmail() + "]: ", false);
            if (email.isEmpty()) email = user.getEmail();
            String role = ConsoleHelper.readString("  角色[" + user.getRole() + "]: ", false);
            if (role.isEmpty()) role = user.getRole();
            String statusStr = ConsoleHelper.readString("  状态[" + user.getStatus() + "](1正常/0禁用): ", false);
            Integer status = statusStr.isEmpty() ? user.getStatus() : Integer.parseInt(statusStr);

            userService.updateUser(id, name, email, role, status, currentUserId, "admin");
            ConsoleHelper.printSuccess("更新成功！");
        } catch (NumberFormatException e) {
            ConsoleHelper.printError("状态必须是数字");
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }

    private void deleteUser() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("删除用户");
        Long id = (long) ConsoleHelper.readInt("  用户ID: ");

        try {
            User user = userService.getUserById(id);
            System.out.println("  即将删除: " + user.getUsername() + " (" + user.getName() + ")");
            String confirm = ConsoleHelper.readString("  确认删除? (y/n): ", true);
            if ("y".equalsIgnoreCase(confirm)) {
                userService.deleteUser(id);
                ConsoleHelper.printSuccess("删除成功！");
            } else {
                ConsoleHelper.printInfo("已取消");
            }
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }
}
