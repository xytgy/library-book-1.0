package com.library.console;

import com.library.exception.BusinessException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.service.BookService;
import com.library.service.BorrowService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BorrowMenu {

    private final BorrowService borrowService;
    private final BookService bookService;

    public BorrowMenu(BorrowService borrowService, BookService bookService) {
        this.borrowService = borrowService;
        this.bookService = bookService;
    }

    public void showMenu(Long userId, boolean isAdmin) {
        while (true) {
            ConsoleHelper.clearScreen();
            ConsoleHelper.printTitle("借阅管理");
            ConsoleHelper.printMenuLine("1. 借书");
            ConsoleHelper.printMenuLine("2. 还书");
            ConsoleHelper.printMenuLine("3. 我的借阅记录");
            if (isAdmin) {
                ConsoleHelper.printMenuLine("4. 全部借阅记录");
            }
            ConsoleHelper.printMenuLine("0. 返回主菜单");
            ConsoleHelper.printMenuEnd();

            int choice = ConsoleHelper.readInt("  请选择: ", 0, isAdmin ? 4 : 3);

            switch (choice) {
                case 1 -> borrowBook(userId);
                case 2 -> returnBook(userId, isAdmin);
                case 3 -> listMyRecords(userId);
                case 4 -> { if (isAdmin) listAllRecords(); }
                case 0 -> { return; }
            }
        }
    }

    private void borrowBook(Long userId) {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("借书");
        Long bookId = (long) ConsoleHelper.readInt("  书籍ID: ");

        try {
            Book book = bookService.getBookById(bookId);
            System.out.println("  书籍: " + book.getTitle());
            System.out.println("  作者: " + book.getAuthor());
            System.out.println("  可借: " + book.getAvailableQuantity());
            System.out.println();
            String confirm = ConsoleHelper.readString("  确认借阅? (y/n): ", true);
            if (!"y".equalsIgnoreCase(confirm)) {
                ConsoleHelper.printInfo("已取消");
                ConsoleHelper.waitForKey();
                return;
            }

            borrowService.borrowBook(userId, bookId);
            ConsoleHelper.printSuccess("借阅成功！请在30天内归还。");
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }

    private void returnBook(Long userId, boolean isAdmin) {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("还书");
        Long recordId = (long) ConsoleHelper.readInt("  借阅记录ID: ");

        try {
            // 获取记录信息
            Map<String, Object> result = borrowService.getMyBorrowRecords(userId, "borrowing", 1, 100);
            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) result.get("records");
            BorrowRecord target = null;
            for (BorrowRecord r : records) {
                if (r.getId().equals(recordId)) {
                    target = r;
                    break;
                }
            }
            if (target == null && isAdmin) {
                result = borrowService.getAllBorrowRecords("borrowing", null, 1, 100);
                @SuppressWarnings("unchecked")
                List<BorrowRecord> allRecords = (List<BorrowRecord>) result.get("records");
                for (BorrowRecord r : allRecords) {
                    if (r.getId().equals(recordId)) {
                        target = r;
                        break;
                    }
                }
            }

            if (target != null) {
                System.out.println("  书籍: " + target.getBookTitle());
                System.out.println("  借阅人: " + target.getUserName());
                System.out.println("  借阅日期: " + target.getBorrowDate());
                System.out.println("  应还日期: " + target.getDueDate());
                System.out.println();
            }

            String confirm = ConsoleHelper.readString("  确认归还? (y/n): ", true);
            if (!"y".equalsIgnoreCase(confirm)) {
                ConsoleHelper.printInfo("已取消");
                ConsoleHelper.waitForKey();
                return;
            }

            String role = isAdmin ? "admin" : "user";
            borrowService.returnBook(userId, recordId, role);
            ConsoleHelper.printSuccess("归还成功！");
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }

    private void listMyRecords(Long userId) {
        listRecords(userId, false);
    }

    private void listAllRecords() {
        listRecords(null, true);
    }

    private void listRecords(Long userId, boolean isAdmin) {
        int page = 1;
        int pageSize = 10;

        while (true) {
            ConsoleHelper.clearScreen();
            String title = isAdmin ? "全部借阅记录" : "我的借阅记录";
            ConsoleHelper.printTitle(title);

            String status = ConsoleHelper.readString("  状态筛选(borrowing/returned, 回车全部): ", false);

            Map<String, Object> result;
            if (isAdmin) {
                result = borrowService.getAllBorrowRecords(status, userId, page, pageSize);
            } else {
                result = borrowService.getMyBorrowRecords(userId, status, page, pageSize);
            }

            @SuppressWarnings("unchecked")
            List<BorrowRecord> records = (List<BorrowRecord>) result.get("records");
            long total = (long) result.get("total");
            int totalPages = (int) Math.ceil((double) total / pageSize);

            System.out.println("  共" + total + "条, 第" + page + "/" + Math.max(totalPages, 1) + "页");
            System.out.println();

            if (records.isEmpty()) {
                ConsoleHelper.printInfo("暂无记录");
            } else {
                String[] headers = {"ID", "书籍", "借阅人", "借阅日期", "应还日期", "状态"};
                int[] widths = {4, 16, 8, 12, 12, 10};
                List<String[]> rows = new ArrayList<>();
                for (BorrowRecord record : records) {
                    rows.add(new String[]{
                        String.valueOf(record.getId()),
                        truncate(record.getBookTitle(), 16),
                        truncate(record.getUserName(), 8),
                        record.getBorrowDate() != null ? record.getBorrowDate().toLocalDate().toString() : "",
                        record.getDueDate() != null ? record.getDueDate().toLocalDate().toString() : "",
                        "borrowing".equals(record.getStatus()) ? "借阅中" : "已归还"
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
                default -> {
                    if (!input.isEmpty()) ConsoleHelper.printError("无效输入");
                    page = 1;
                }
            }
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 2) + ".." : s;
    }
}
