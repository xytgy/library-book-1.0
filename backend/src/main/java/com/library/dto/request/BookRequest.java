package com.library.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookRequest {

    @Size(max = 20, message = "ISBN长度不能超过20")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    @Size(min = 1, max = 200, message = "书名长度必须在1-200之间")
    private String title;

    @NotBlank(message = "作者不能为空")
    @Size(min = 1, max = 100, message = "作者长度必须在1-100之间")
    private String author;

    @Size(max = 50, message = "分类长度不能超过50")
    private String category;

    @Size(max = 50, message = "位置长度不能超过50")
    private String location;

    @Min(value = 0, message = "总库存不能小于0")
    private Integer totalQuantity;

    private String description;

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
