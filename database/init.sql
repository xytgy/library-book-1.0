CREATE DATABASE IF NOT EXISTS library_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_management;

DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    role ENUM('user', 'admin') DEFAULT 'user',
    status TINYINT DEFAULT 1 COMMENT '1:正常, 0:禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    location VARCHAR(50) COMMENT '书籍位置，如A-01-01',
    total_quantity INT DEFAULT 0,
    available_quantity INT DEFAULT 0,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE borrow_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    due_date DATETIME NOT NULL,
    return_date DATETIME,
    status ENUM('borrowing', 'returned') DEFAULT 'borrowing',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (username, password, name, email, role) VALUES
('admin', '$2a$10$nsBRs8RxEo260Aes4t833.NMlwc/ZVoMusr835aYJPJcFIpxVr0KK', '管理员', 'admin@library.com', 'admin'),
('user1', '$2a$10$nsBRs8RxEo260Aes4t833.NMlwc/ZVoMusr835aYJPJcFIpxVr0KK', '张三', 'zhangsan@example.com', 'user');

INSERT INTO books (isbn, title, author, category, location, total_quantity, available_quantity, description) VALUES
('9787111111111', 'JavaScript高级程序设计', 'Nicholas C. Zakas', '编程', 'A-01-01', 5, 5, '经典的JavaScript学习书籍'),
('9787111222222', '深入理解计算机系统', 'Bryant', '计算机', 'A-02-01', 3, 3, '计算机系统经典教材'),
('9787111333333', '代码整洁之道', 'Robert C. Martin', '编程', 'A-01-02', 4, 4, '如何编写整洁的代码'),
('9787111444444', '设计模式', 'Gamma', '编程', 'A-01-03', 2, 2, '经典设计模式书籍'),
('9787111555555', '算法导论', 'Cormen', '算法', 'B-01-01', 3, 3, '算法领域经典教材');
