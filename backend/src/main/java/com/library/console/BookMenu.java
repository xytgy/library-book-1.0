package com.library.console;

import com.library.exception.BusinessException;
import com.library.model.Book;
import com.library.service.BookService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookMenu {

    private final BookService bookService;

    public BookMenu(BookService bookService) {
        this.bookService = bookService;
    }

    public void showMenu(boolean isAdmin) {
        while (true) {
            ConsoleHelper.clearScreen();
            ConsoleHelper.printTitle("书籍管理");
            ConsoleHelper.printMenuLine("1. 查看书籍列表");
            ConsoleHelper.printMenuLine("2. 搜索书籍");
            ConsoleHelper.printMenuLine("3. 查看书籍详情");
            if (isAdmin) {
                ConsoleHelper.printMenuLine("4. 添加书籍");
                ConsoleHelper.printMenuLine("5. 编辑书籍");
                ConsoleHelper.printMenuLine("6. 删除书籍");
            }
            ConsoleHelper.printMenuLine("0. 返回主菜单");
            ConsoleHelper.printMenuEnd();

            int choice = ConsoleHelper.readInt("  请选择: ", 0, isAdmin ? 6 : 3);

            switch (choice) {
                case 1 -> listBooks(false);
                case 2 -> listBooks(true);
                case 3 -> showBookDetail();
                case 4 -> { if (isAdmin) createBook(); }
                case 5 -> { if (isAdmin) updateBook(); }
                case 6 -> { if (isAdmin) deleteBook(); }
                case 0 -> { return; }
            }
        }
    }

    private void listBooks(boolean isSearch) {
        String search = null;
        String category = null;

        if (isSearch) {
            ConsoleHelper.clearScreen();
            ConsoleHelper.printTitle("搜索书籍");
            search = ConsoleHelper.readString("  关键词(书名/作者/ISBN): ", false);
            category = ConsoleHelper.readString("  分类: ", false);
        }

        int page = 1;
        int pageSize = 10;

        while (true) {
            ConsoleHelper.clearScreen();
            Map<String, Object> result = bookService.getBooks(search, category, page, pageSize);
            @SuppressWarnings("unchecked")
            List<Book> records = (List<Book>) result.get("records");
            long total = (long) result.get("total");
            int totalPages = (int) Math.ceil((double) total / pageSize);

            ConsoleHelper.printTitle("书籍列表 (共" + total + "条, 第" + page + "/" + Math.max(totalPages, 1) + "页)");

            if (records.isEmpty()) {
                ConsoleHelper.printInfo("暂无数据");
            } else {
                String[] headers = {"ID", "书名", "作者", "分类", "库存", "可借"};
                int[] widths = {4, 16, 12, 8, 6, 6};
                List<String[]> rows = new ArrayList<>();
                for (Book book : records) {
                    rows.add(new String[]{
                        String.valueOf(book.getId()),
                        truncate(book.getTitle(), 16),
                        truncate(book.getAuthor(), 12),
                        truncate(book.getCategory(), 8),
                        String.valueOf(book.getTotalQuantity()),
                        String.valueOf(book.getAvailableQuantity())
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

    private void showBookDetail() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("查看书籍详情");
        Long id = (long) ConsoleHelper.readInt("  书籍ID: ");

        try {
            Book book = bookService.getBookById(id);
            System.out.println();
            System.out.println("  ID:       " + book.getId());
            System.out.println("  ISBN:     " + (book.getIsbn() != null ? book.getIsbn() : "无"));
            System.out.println("  书名:     " + book.getTitle());
            System.out.println("  作者:     " + book.getAuthor());
            System.out.println("  分类:     " + (book.getCategory() != null ? book.getCategory() : "无"));
            System.out.println("  位置:     " + (book.getLocation() != null ? book.getLocation() : "无"));
            System.out.println("  总库存:   " + book.getTotalQuantity());
            System.out.println("  可借数量: " + book.getAvailableQuantity());
            System.out.println("  简介:     " + (book.getDescription() != null ? book.getDescription() : "无"));
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }

    private void createBook() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("添加书籍");

        String isbn = ConsoleHelper.readString("  ISBN: ", false);
        String title = ConsoleHelper.readString("  书名: ", true);
        String author = ConsoleHelper.readString("  作者: ", true);
        String category = ConsoleHelper.readString("  分类: ", false);
        String location = ConsoleHelper.readString("  位置: ", false);
        int totalQuantity = ConsoleHelper.readInt("  总库存: ", 1, 99999);
        String description = ConsoleHelper.readString("  简介: ", false);

        try {
            bookService.createBook(isbn, title, author, category, location, totalQuantity, description);
            ConsoleHelper.printSuccess("添加成功！");
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }

    private void updateBook() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("编辑书籍");
        Long id = (long) ConsoleHelper.readInt("  书籍ID: ");

        try {
            Book book = bookService.getBookById(id);
            System.out.println("  当前信息:");
            System.out.println("  书名: " + book.getTitle() + "  作者: " + book.getAuthor());
            System.out.println("  库存: " + book.getTotalQuantity() + "  可借: " + book.getAvailableQuantity());
            System.out.println();

            String isbn = ConsoleHelper.readString("  ISBN[" + book.getIsbn() + "]: ", false);
            if (isbn.isEmpty()) isbn = book.getIsbn();
            String title = ConsoleHelper.readString("  书名[" + book.getTitle() + "]: ", false);
            if (title.isEmpty()) title = book.getTitle();
            String author = ConsoleHelper.readString("  作者[" + book.getAuthor() + "]: ", false);
            if (author.isEmpty()) author = book.getAuthor();
            String category = ConsoleHelper.readString("  分类[" + book.getCategory() + "]: ", false);
            if (category.isEmpty()) category = book.getCategory();
            String location = ConsoleHelper.readString("  位置[" + book.getLocation() + "]: ", false);
            if (location.isEmpty()) location = book.getLocation();
            System.out.println("  总库存[" + book.getTotalQuantity() + "]: ");
            String qtyStr = ConsoleHelper.readString("  输入新值或回车跳过: ", false);
            int totalQuantity = qtyStr.isEmpty() ? book.getTotalQuantity() : Integer.parseInt(qtyStr);
            String description = ConsoleHelper.readString("  简介[" + book.getDescription() + "]: ", false);
            if (description.isEmpty()) description = book.getDescription();

            bookService.updateBook(id, isbn, title, author, category, location, totalQuantity, description);
            ConsoleHelper.printSuccess("更新成功！");
        } catch (NumberFormatException e) {
            ConsoleHelper.printError("库存必须是数字");
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }

    private void deleteBook() {
        ConsoleHelper.clearScreen();
        ConsoleHelper.printTitle("删除书籍");
        Long id = (long) ConsoleHelper.readInt("  书籍ID: ");

        try {
            Book book = bookService.getBookById(id);
            System.out.println("  即将删除: " + book.getTitle() + " (ID: " + book.getId() + ")");
            String confirm = ConsoleHelper.readString("  确认删除? (y/n): ", true);
            if ("y".equalsIgnoreCase(confirm)) {
                bookService.deleteBook(id);
                ConsoleHelper.printSuccess("删除成功！");
            } else {
                ConsoleHelper.printInfo("已取消");
            }
        } catch (BusinessException e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleHelper.waitForKey();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 2) + ".." : s;
    }
}
