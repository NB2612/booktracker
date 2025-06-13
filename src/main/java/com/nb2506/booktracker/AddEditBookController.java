package com.nb2506.booktracker;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для окна добавления и редактирования книги.
 * Управляет вводом данных, выбором жанров, загрузкой обложки и сохранением книги в репозиторий.
 */
public class AddEditBookController {

    /** Контейнер для чекбоксов жанров книги. */
    @FXML private VBox checkboxContainer;

    /** Текстовое поле для ввода названия книги. */
    @FXML private TextField titleField;

    /** Текстовое поле для ввода автора книги. */
    @FXML private TextField authorField;

    /** Текстовое поле для ввода года издания книги. */
    @FXML private TextField yearField;

    /** Текстовое поле для ввода общего количества страниц книги. */
    @FXML private TextField pagesField;

    /** Текстовое поле для ввода рейтинга книги (от 0 до 10). */
    @FXML private TextField ratingField;

    /** Текстовое поле для ввода текущей страницы, на которой читатель остановился. */
    @FXML private TextField currentPageField;

    /** Компонент для отображения обложки книги. */
    @FXML private ImageView coverImageView;

    /** Массив байтов с данными изображения обложки книги. */
    private byte[] coverImageBytes;

    /** Список чекбоксов для жанров, созданных динамически. */
    private final List<CheckBox> genreCheckboxes = new ArrayList<>();

    /** Текущая редактируемая книга, либо null при добавлении новой книги. */
    private Book currentBook;

    /** Репозиторий для сохранения и загрузки книг из базы данных. */
    private final BookRepository bookRepository = new DatabaseBookRepository();

    /** Все доступные жанры для выбора. */
    private static final String[] ALL_GENRES = {
            "Фантастика", "Детектив", "Роман", "Научная литература",
            "Биография", "Ужасы", "Приключения", "Фэнтези",
            "Детская литература", "Готика", "Философия"
    };

    /**
     * Инициализация контроллера.
     * Создаёт чекбоксы для всех жанров и добавляет их в контейнер.
     * Если книга не задана, инициализирует пустое состояние.
     */
    @FXML
    private void initialize() {
        for (String genre : ALL_GENRES) {
            CheckBox cb = new CheckBox(genre);
            genreCheckboxes.add(cb);
            checkboxContainer.getChildren().add(cb);
        }

        if (currentBook == null) {
            coverImageBytes = null;
        }
    }

    /**
     * Обработчик нажатия кнопки выбора обложки книги.
     * Открывает диалог выбора файла, загружает и отображает выбранное изображение.
     */
    @FXML
    private void onChooseCover() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение обложки");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(coverImageView.getScene().getWindow());
        if (file != null) {
            try {
                coverImageBytes = ImageHelper.readImageFile(file);
                Image img = ImageHelper.toImage(coverImageBytes);
                coverImageView.setImage(img);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить изображение.");
            }
        }
    }

    /**
     * Устанавливает книгу для редактирования.
     * Заполняет поля формы данными книги, если она не null.
     *
     * @param book объект книги для редактирования или null для добавления новой книги
     */
    public void setBook(Book book) {
        this.currentBook = book;
        if (book != null) {
            fillFormWithBook(book);
        }
    }

    /**
     * Заполняет форму данными из объекта книги.
     *
     * @param book книга, данные которой нужно отобразить
     */
    private void fillFormWithBook(Book book) {
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        yearField.setText(String.valueOf(book.getYear()));
        pagesField.setText(String.valueOf(book.getPages()));
        ratingField.setText(String.valueOf(book.getRating()));
        currentPageField.setText(String.valueOf(book.getCurrentPage()));
        coverImageBytes = book.getCover();

        Image coverImage = ImageHelper.toImage(coverImageBytes);
        if (coverImage != null) {
            coverImageView.setImage(coverImage);
        }

        markSelectedGenres(book.getGenres());
    }

    /**
     * Обработчик нажатия кнопки сохранения книги.
     * Валидирует введённые данные, создает или обновляет книгу в репозитории.
     */
    @FXML
    private void onSave() {
        if (!validateInput()) return;

        Book book = buildBookFromInput();
        try {
            if (currentBook == null) {
                bookRepository.insert(book);
                showAlert(Alert.AlertType.INFORMATION, "Книга добавлена", "Книга успешно добавлена!");
            } else {
                book.setId(currentBook.getId());
                bookRepository.update(book);
                showAlert(Alert.AlertType.INFORMATION, "Книга обновлена", "Книга успешно обновлена!");
            }
            onCancel();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось сохранить книгу.");
        }
    }

    /**
     * Проверяет корректность введённых пользователем данных.
     *
     * @return true, если данные корректны; false в противном случае
     */
    private boolean validateInput() {
        List<String> errors = new ArrayList<>();

        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String yearStr = yearField.getText().trim();
        String pagesStr = pagesField.getText().trim();
        String ratingStr = ratingField.getText().trim();
        String currentPageStr = currentPageField.getText().trim();

        if (title.isEmpty()) errors.add("Название книги обязательно.");
        if (author.isEmpty()) errors.add("Автор обязателен.");

        try {
            int year = Integer.parseInt(yearStr);
            if (year < 0) errors.add("Год должен быть положительным числом.");
        } catch (NumberFormatException e) {
            errors.add("Год должен быть числом.");
        }

        try {
            int pages = Integer.parseInt(pagesStr);
            if (pages <= 0) errors.add("Количество страниц должно быть положительным.");
        } catch (NumberFormatException e) {
            errors.add("Количество страниц должно быть числом.");
        }

        try {
            double rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 10) errors.add("Рейтинг должен быть от 0 до 10.");
        } catch (NumberFormatException e) {
            errors.add("Рейтинг должен быть числом.");
        }

        try {
            int currentPage = Integer.parseInt(currentPageStr);
            int pages = Integer.parseInt(pagesStr);
            if (currentPage < 0 || currentPage > pages) errors.add("Текущая страница должна быть в диапазоне от 0 " +
                    "до общего количества страниц.");
        } catch (NumberFormatException e) {
            errors.add("Текущая страница должна быть числом.");
        }

        List<String> selectedGenres = genreCheckboxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .toList();
        if (selectedGenres.isEmpty()) errors.add("Выберите хотя бы один жанр.");

        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка ввода", String.join("\n", errors));
            return false;
        }
        return true;
    }

    /**
     * Создаёт объект книги на основе данных, введённых в форму.
     *
     * @return объект Book с заполненными полями
     */
    private Book buildBookFromInput() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        int year = Integer.parseInt(yearField.getText().trim());
        int pages = Integer.parseInt(pagesField.getText().trim());
        double rating = Double.parseDouble(ratingField.getText().trim());
        int currentPage = Integer.parseInt(currentPageField.getText().trim());

        List<String> selectedGenres = genreCheckboxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());
        String genres = String.join(",", selectedGenres);

        Book book = new Book(title, author, year, pages, rating, currentPage, genres, coverImageBytes);
        book.setTitle(title);
        book.setAuthor(author);
        book.setYear(year);
        book.setPages(pages);
        book.setRating(rating);
        book.setCurrentPage(currentPage);
        book.setGenres(genres);
        book.setCover(coverImageBytes);
        return book;
    }

    /**
     * Отмечает в чекбоксах жанров те, что присутствуют в строке genres.
     *
     * @param genres строка с жанрами, разделёнными запятыми
     */
    private void markSelectedGenres(String genres) {
        if (genres == null || genres.trim().isEmpty()) {
            genreCheckboxes.forEach(cb -> cb.setSelected(false));
            return;
        }
        Set<String> selected = Arrays.stream(genres.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        genreCheckboxes.forEach(cb -> cb.setSelected(selected.contains(cb.getText())));
    }

    /**
     * Обработчик кнопки отмены.
     * Закрывает текущее окно.
     */
    @FXML
    private void onCancel() {
        Stage stage = (Stage) coverImageView.getScene().getWindow();
        stage.close();
    }

    /**
     * Показывает всплывающее окно с сообщением.
     *
     * @param type тип сообщения (информация, ошибка и т.п.)
     * @param title заголовок окна
     * @param content текст сообщения
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
