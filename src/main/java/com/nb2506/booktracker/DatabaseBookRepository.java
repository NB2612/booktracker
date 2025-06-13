package com.nb2506.booktracker;

/**
 * Реализация интерфейса {@link BookRepository} для работы с базой данных.
 * Использует вспомогательный класс {@link DatabaseHelper} для выполнения операций вставки и обновления.
 */
public class DatabaseBookRepository implements BookRepository {

    /**
     * Вставляет новую книгу в базу данных.
     *
     * @param book объект книги для добавления (обязательные поля должны быть заполнены)
     */
    @Override
    public void insert(Book book) {
        DatabaseHelper.insertBook(
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                book.getPages(),
                book.getRating(),
                book.getGenres(),
                book.getCurrentPage(),
                book.getCover()
        );
    }

    /**
     * Обновляет существующую книгу в базе данных.
     *
     * @param book объект книги с обновлёнными данными. Должен содержать корректный идентификатор.
     */
    @Override
    public void update(Book book) {
        DatabaseHelper.updateBook(book);
    }

    /**
     * Обновляет существующую книгу в базе данных.
     *
     * @param book объект книги с обновлёнными данными. Должен содержать корректный идентификатор.
     */
    @Override
    public void deleteBookById(int id) {
        DatabaseHelper.deleteBookById(book);
    }
}
