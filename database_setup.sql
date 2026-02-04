CREATE DATABASE IF NOT EXISTS finance_app CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER IF NOT EXISTS 'finance_user'@'localhost' IDENTIFIED BY 'password1234';
GRANT ALL PRIVILEGES ON finance_app.* TO 'finance_user'@'localhost';
FLUSH PRIVILEGES;
