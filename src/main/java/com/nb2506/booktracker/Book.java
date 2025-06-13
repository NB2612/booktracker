package com.nb2506.booktracker;

import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;

/**
 * Модель книги с основными полями: название, автор, год издания, количество страниц, рейтинг,
 * жанры, текущая прочитанная страница и обложка в виде массива байт.
 */
public class Book {
    /** Уникальный идентификатор книги в базе данных. */
    private int id;

    /** Название книги. */
    private String title;

    /** Автор книги. */
    private String author;

    /** Год издания книги. */
    private int year;

    /** Общее количество страниц в книге. */
    private int pages;

    /** Рейтинг книги (обычно от 0 до 10). */
    private double rating;

    /** Жанры книги, сохранённые в виде строки, разделённой запятыми. */
    private String genres;

    /** Текущая страница, на которой находится читатель. */
    private int currentPage;

    /** Обложка книги в виде массива байт. Может быть null, если обложка отсутствует. */
    private byte[] cover;

    /**
     * Полный конструктор книги с указанием id.
     *
     * @param id уникальный идентификатор книги
     * @param title название книги
     * @param author автор книги
     * @param year год издания
     * @param pages общее количество страниц
     * @param rating рейтинг книги
     * @param genres жанры книги через запятую
     * @param currentPage текущая прочитанная страница
     * @param cover байтовый массив изображения обложки книги (может быть null)
     */
    public Book(int id, String title, String author, int year, int pages,
                double rating, String genres, int currentPage, byte[] cover) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.pages = pages;
        this.rating = rating;
        this.genres = genres;
        this.currentPage = currentPage;
        this.cover = cover;
    }

    /**
     * Конструктор книги без указания id (например, для новых книг перед сохранением).
     *
     * @param title название книги
     * @param author автор книги
     * @param year год издания
     * @param pages общее количество страниц
     * @param rating рейтинг книги
     * @param currentPage текущая прочитанная страница
     * @param genres жанры книги через запятую
     * @param cover байтовый массив изображения обложки книги (может быть null)
     */
    public Book(String title, String author, int year, int pages, double rating, int currentPage, String genres, byte[] cover) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.pages = pages;
        this.rating = rating;
        this.genres = genres;
        this.currentPage = currentPage;
        this.cover = cover;
    }

    /** Возвращает уникальный идентификатор книги. */
    public int getId() { return id; }

    /** Возвращает название книги. */
    public String getTitle() { return title; }

    /** Возвращает автора книги. */
    public String getAuthor() { return author; }

    /** Возвращает год издания книги. */
    public int getYear() { return year; }

    /** Возвращает количество страниц в книге. */
    public int getPages() { return pages; }

    /** Возвращает рейтинг книги. */
    public double getRating() { return rating; }

    /** Возвращает жанры книги как строку, разделённую запятыми. */
    public String getGenres() { return genres; }

    /** Возвращает текущую прочитанную страницу. */
    public int getCurrentPage() { return currentPage; }

    /** Возвращает байтовый массив с обложкой книги. Может быть null. */
    public byte[] getCover() { return cover; }

    /** Устанавливает уникальный идентификатор книги. */
    public void setId(int id) {
        this.id = id;
    }

    /** Устанавливает название книги. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Устанавливает автора книги. */
    public void setAuthor(String author) {
        this.author = author;
    }

    /** Устанавливает год издания книги. */
    public void setYear(int year) {
        this.year = year;
    }

    /** Устанавливает количество страниц в книге. */
    public void setPages(int pages) {
        this.pages = pages;
    }

    /** Устанавливает рейтинг книги. */
    public void setRating(double rating) {
        this.rating = rating;
    }

    /** Устанавливает жанры книги. */
    public void setGenres(String genres) {
        this.genres = genres;
    }

    /** Устанавливает текущую прочитанную страницу. */
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /** Устанавливает обложку книги. */
    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    /**
     * Возвращает обложку книги в виде объекта {@link Image} для отображения в JavaFX.
     * Если обложка отсутствует, возвращает null.
     *
     * @return объект Image с обложкой книги или null, если обложка отсутствует
     */
    public Image getCoverAsImage() {
        if (cover != null && cover.length > 0) {
            return new Image(new ByteArrayInputStream(cover));
        }
        return null;
    }
}
