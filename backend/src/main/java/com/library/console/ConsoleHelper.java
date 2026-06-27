package com.library.console;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.util.List;
import java.util.Scanner;

public class ConsoleHelper {

    private static final Scanner scanner = new Scanner(System.in);

    static {
        AnsiConsole.systemInstall();
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printTitle(String title) {
        int width = 40;
        StringBuilder sb = new StringBuilder();
        sb.append(Ansi.ansi().fgCyan().a("╔"));
        for (int i = 0; i < width - 2; i++) sb.append("═");
        sb.append("╗\n");

        int padding = (width - 2 - title.length()) / 2;
        sb.append(Ansi.ansi().fgCyan().a("║"));
        for (int i = 0; i < padding; i++) sb.append(" ");
        sb.append(Ansi.ansi().bold().a(title));
        for (int i = 0; i < width - 2 - padding - title.length(); i++) sb.append(" ");
        sb.append(Ansi.ansi().fgCyan().a("║\n"));

        sb.append(Ansi.ansi().fgCyan().a("╠"));
        for (int i = 0; i < width - 2; i++) sb.append("═");
        sb.append("╣\n");

        System.out.println(sb.toString());
    }

    public static void printMenuLine(String text) {
        System.out.println(Ansi.ansi().fgCyan().a("║  ").reset().a(text));
    }

    public static void printMenuEnd() {
        int width = 40;
        StringBuilder sb = new StringBuilder();
        sb.append(Ansi.ansi().fgCyan().a("╚"));
        for (int i = 0; i < width - 2; i++) sb.append("═");
        sb.append("╝");
        System.out.println(sb.toString());
    }

    public static void printSuccess(String message) {
        System.out.println(Ansi.ansi().fgGreen().a("  ✓ ").a(message).reset());
    }

    public static void printError(String message) {
        System.out.println(Ansi.ansi().fgRed().a("  ✗ ").a(message).reset());
    }

    public static void printInfo(String message) {
        System.out.println(Ansi.ansi().fgYellow().a("  ℹ ").a(message).reset());
    }

    public static void printTable(String[] headers, List<String[]> rows, int[] widths) {
        StringBuilder sb = new StringBuilder();

        sb.append(Ansi.ansi().fgCyan().a("┌"));
        for (int i = 0; i < widths.length; i++) {
            for (int j = 0; j < widths[i] + 2; j++) sb.append("─");
            if (i < widths.length - 1) sb.append("┬");
        }
        sb.append("┐\n");

        sb.append(Ansi.ansi().fgCyan().a("│"));
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ").append(Ansi.ansi().bold().a(padRight(headers[i], widths[i]))).append(Ansi.ansi().fgCyan().a(" │"));
        }
        sb.append("\n");

        sb.append(Ansi.ansi().fgCyan().a("├"));
        for (int i = 0; i < widths.length; i++) {
            for (int j = 0; j < widths[i] + 2; j++) sb.append("─");
            if (i < widths.length - 1) sb.append("┼");
        }
        sb.append("┤\n");

        for (String[] row : rows) {
            sb.append(Ansi.ansi().fgCyan().a("│"));
            for (int i = 0; i < row.length; i++) {
                sb.append(" ").append(Ansi.ansi().reset().a(padRight(row[i], widths[i]))).append(Ansi.ansi().fgCyan().a(" │"));
            }
            sb.append("\n");
        }

        sb.append(Ansi.ansi().fgCyan().a("└"));
        for (int i = 0; i < widths.length; i++) {
            for (int j = 0; j < widths[i] + 2; j++) sb.append("─");
            if (i < widths.length - 1) sb.append("┴");
        }
        sb.append("┘\n");

        System.out.println(sb.toString());
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(Ansi.ansi().fgCyan().a(prompt).reset());
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printError("请输入数字");
            }
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) return value;
            printError("请输入 " + min + " 到 " + max + " 之间的数字");
        }
    }

    public static String readString(String prompt) {
        System.out.print(Ansi.ansi().fgCyan().a(prompt).reset());
        return scanner.nextLine().trim();
    }

    public static String readString(String prompt, boolean required) {
        while (true) {
            String value = readString(prompt);
            if (!required || (value != null && !value.isEmpty())) return value;
            printError("此项为必填");
        }
    }

    public static void waitForKey() {
        System.out.println(Ansi.ansi().a("\n  按回车键继续...").reset());
        try {
            scanner.nextLine();
        } catch (java.util.NoSuchElementException e) {
            System.exit(0);
        }
    }

    public static int validatePage(int page) {
        return Math.max(1, page);
    }

    public static int validatePageSize(int pageSize) {
        if (pageSize <= 0) return 10;
        return Math.min(pageSize, 100);
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);
        return s + " ".repeat(n - s.length());
    }
}
