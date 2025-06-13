package com.nb2506.booktracker;

import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * Класс-помощник для работы с базой данных SQLite, содержащей информацию о книгах.
 * Предоставляет методы для создания таблицы, добавления, обновления, удаления и выборки книг.
 */
public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:books.db";

    /**
     * Устанавливает и возвращает соединение с базой данных SQLite.
     *
     * @return объект {@link Connection} для работы с БД
     * @throws SQLException при ошибках подключения
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Возвращает список всех книг из базы данных.
     *
     * @return список объектов {@link Book} (пустой, если книг нет или ошибка)
     */
    public static List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("year"),
                        rs.getInt("pages"),
                        rs.getDouble("rating"),
                        rs.getString("genres"),
                        rs.getInt("current_page"),
                        rs.getBytes("cover") // может быть null
                );
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
    }

    /**
     * Создаёт таблицу "books" в базе данных, если она ещё не существует.
     * Таблица содержит поля для хранения информации о книгах.
     */
    public static void createTable() {
        String sql = """
        CREATE TABLE IF NOT EXISTS books (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            author TEXT NOT NULL,
            year INTEGER,
            pages INTEGER,
            rating REAL,
            genres TEXT,
            current_page INTEGER,
            cover BLOB
        );
    """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица создана или уже существует.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Добавляет новую книгу в базу данных.
     *
     * @param title       название книги
     * @param author      автор книги
     * @param year        год издания
     * @param pages       количество страниц
     * @param rating      рейтинг книги (от 0 до 10)
     * @param genres      список жанров через запятую
     * @param currentPage текущая страница
     * @param cover       изображение обложки в виде массива байт, может быть null
     */
    public static void insertBook(String title, String author, int year, int pages,
                                  double rating, String genres, int currentPage, byte[] cover) {
        String sql = """
        INSERT INTO books(title, author, year, pages, rating, genres, current_page, cover)
        VALUES(?, ?, ?, ?, ?, ?, ?, ?);
    """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setInt(3, year);
            pstmt.setInt(4, pages);
            pstmt.setDouble(5, rating);
            pstmt.setString(6, genres);
            pstmt.setInt(7, currentPage);
            if (cover != null) {
                pstmt.setBytes(8, cover);
            } else {
                pstmt.setNull(8, Types.BLOB);
            }

            pstmt.executeUpdate();
            System.out.println("Книга добавлена!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обновляет данные существующей книги в базе данных.
     *
     * @param book объект книги с обновлёнными данными, должен содержать корректный id
     */
    public static void updateBook(Book book) {
        String sql = "UPDATE books SET title=?, author=?, year=?, pages=?, rating=?, genres=?, current_page=?, cover=? WHERE id=?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getYear());
            pstmt.setInt(4, book.getPages());
            pstmt.setDouble(5, book.getRating());
            pstmt.setString(6, book.getGenres());
            pstmt.setInt(7, book.getCurrentPage());
            pstmt.setBytes(8, book.getCover());
            pstmt.setInt(9, book.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаляет книгу из базы данных по её идентификатору.
     *
     * @param id идентификатор книги для удаления
     */
    public static void deleteBookById(int id) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает список всех уникальных жанров, присутствующих в базе.
     *
     * @return отсортированный список жанров (может быть пустым)
     */
    public static List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT genres FROM books";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            Set<String> genreSet = new HashSet<>();
            while (rs.next()) {
                String[] parts = rs.getString("genres").split(",\\s*");
                genreSet.addAll(Arrays.asList(parts));
            }
            genres.addAll(genreSet);
            Collections.sort(genres);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }
}
