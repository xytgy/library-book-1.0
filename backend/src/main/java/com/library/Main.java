package com.library;

import com.library.console.*;
import com.library.dao.BookDao;
import com.library.dao.BorrowRecordDao;
import com.library.dao.UserDao;
import com.library.db.DatabaseUtil;
import com.library.service.AuthService;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.UserService;
import com.library.util.JwtUtil;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.util.Map;

public class Main {

    private static Long currentUserId = null;
    private static String currentUsername = null;
    private static String currentRole = null;

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        System.out.println(Ansi.ansi().fgCyan().bold().a("\n  正在初始化图书馆管理系统...\n").reset());

        try {
            DatabaseUtil.init();
            System.out.println(Ansi.ansi().fgGreen().a("  ✓ 数据库连接成功\n").reset());
        } catch (Exception e) {
            System.out.println(Ansi.ansi().fgRed().a("  ✗ 数据库连接失败: " + e.getMessage()).reset());
            System.out.println(Ansi.ansi().fgYellow().a("  请检查 application.properties 中的数据库配置\n").reset());
            return;
        }

        UserDao userDao = new UserDao();
        BookDao bookDao = new BookDao();
        BorrowRecordDao borrowRecordDao = new BorrowRecordDao();
        JwtUtil jwtUtil = new JwtUtil();

        AuthService authService = new AuthService(userDao, jwtUtil);
        BookService bookService = new BookService(bookDao, borrowRecordDao);
        BorrowService borrowService = new BorrowService(borrowRecordDao, bookDao);
        UserService userService = new UserService(userDao, borrowRecordDao);

        AuthMenu authMenu = new AuthMenu(authService);
        BookMenu bookMenu = new BookMenu(bookService);
        BorrowMenu borrowMenu = new BorrowMenu(borrowService, bookService);
        UserMenu userMenu = new UserMenu(userService);
        AuditMenu auditMenu = new AuditMenu();

        while (true) {
            ConsoleHelper.clearScreen();
            showMainMenu();

            int maxChoice = currentUserId == null ? 2 : ("admin".equals(currentRole) ? 5 : 2);
            int choice = ConsoleHelper.readInt("  请选择: ", 0, maxChoice);

            switch (choice) {
                case 1 -> {
                    if (currentUserId == null) {
                        Map<String, Object> result = authMenu.showLoginMenu();
                        if (result != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> user = (Map<String, Object>) result.get("user");
                            currentUserId = (Long) user.get("id");
                            currentUsername = (String) user.get("username");
                            currentRole = (String) user.get("role");
                        }
                    } else {
                        bookMenu.showMenu("admin".equals(currentRole));
                    }
                }
                case 2 -> {
                    if (currentUserId == null) {
                        Map<String, Object> result = authMenu.showRegisterMenu();
                        if (result != null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> user = (Map<String, Object>) result.get("user");
                            currentUserId = (Long) user.get("id");
                            currentUsername = (String) user.get("username");
                            currentRole = (String) user.get("role");
                        }
                    } else {
                        borrowMenu.showMenu(currentUserId, "admin".equals(currentRole));
                    }
                }
                case 3 -> {
                    if (currentUserId != null && "admin".equals(currentRole)) {
                        userMenu.showMenu(currentUserId);
                    }
                }
                case 4 -> auditMenu.showMenu();
                case 5 -> logout();
                case 0 -> {
                    if (currentUserId == null) {
                        System.out.println(Ansi.ansi().fgGreen().a("\n  再见！\n").reset());
                        DatabaseUtil.shutdown();
                        return;
                    } else {
                        logout();
                    }
                }
            }
        }
    }

    private static void logout() {
        System.out.println(Ansi.ansi().fgYellow().a("\n  已退出登录\n").reset());
        currentUserId = null;
        currentUsername = null;
        currentRole = null;
        ConsoleHelper.waitForKey();
    }

    private static void showMainMenu() {
        if (currentUserId == null) {
            ConsoleHelper.printTitle("图书馆管理系统 v1.0");
            ConsoleHelper.printMenuLine("1. 用户登录");
            ConsoleHelper.printMenuLine("2. 用户注册");
            ConsoleHelper.printMenuLine("0. 退出系统");
        } else {
            String roleLabel = "admin".equals(currentRole) ? "管理员" : "用户";
            ConsoleHelper.printTitle("欢迎, " + currentUsername + " (" + roleLabel + ")");
            if ("admin".equals(currentRole)) {
                ConsoleHelper.printMenuLine("1. 书籍管理");
                ConsoleHelper.printMenuLine("2. 借阅管理");
                ConsoleHelper.printMenuLine("3. 用户管理");
                ConsoleHelper.printMenuLine("4. 审计日志");
                ConsoleHelper.printMenuLine("5. 退出登录");
            } else {
                ConsoleHelper.printMenuLine("1. 书籍浏览");
                ConsoleHelper.printMenuLine("2. 借阅管理");
                ConsoleHelper.printMenuLine("0. 退出登录");
            }
        }
        ConsoleHelper.printMenuEnd();
    }
}
