package com.library.console;

import com.library.exception.BusinessException;
import com.library.service.AuthService;

import java.util.Map;

public class AuthMenu {

    private final AuthService authService;

    public AuthMenu(AuthService authService) {
        this.authService = authService;
    }

    public Map<String, Object> showLoginMenu() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("用户登录");

        String username = ConsoleHelper.readString("  用户名: ", true);
        String password = ConsoleHelper.readString("  密码: ", true);

        try {
            Map<String, Object> result = authService.login(username, password);
            ConsoleHelper.printSuccess("登录成功！");
            ConsoleHelper.waitForKey();
            return result;
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
            ConsoleHelper.waitForKey();
            return null;
        }
    }

    public Map<String, Object> showRegisterMenu() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("用户注册");

        String username = ConsoleHelper.readString("  用户名(3-50位): ", true);
        String password = ConsoleHelper.readString("  密码(6-50位): ", true);
        String name = ConsoleHelper.readString("  姓名(2-50位): ", true);
        String email = ConsoleHelper.readString("  邮箱: ", false);

        try {
            Map<String, Object> result = authService.register(username, password, name, email);
            ConsoleHelper.printSuccess("注册成功！");
            ConsoleHelper.waitForKey();
            return result;
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
            ConsoleHelper.waitForKey();
            return null;
        }
    }
}
