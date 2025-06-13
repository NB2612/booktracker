package com.nb2506.booktracker;

import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер главного окна приложения "Дневник чтения".
 * Управляет отображением списка книг, фильтрацией, добавлением,
 * удалением, редактированием и отображением деталей выбранной книги.
 */
public class MainController {

    /** Список книг, отображаемый в ListView. */
    @FXML
    private ListView<Book> listViewBook;

    /** Метка для отображения названия книги. */
    @FXML
    private Label titleBook;

    /** Метка для отображения автора книги. */
    @FXML
    private Label bookAuthorLabel;

    /** Метка для отображения года издания книги. */
    @FXML
    private Label yearLabel;

    /** Метка для отображения количества страниц книги. */
    @FXML
    private Label pagesLabel;

    /** Метка для отображения рейтинга книги. */
    @FXML
    private Label ratingLabel;

    /** Контейнер для отображения жанров книги. */
    @FXML
    private VBox genresContainer;

    /** Прогресс-бар, показывающий прогресс чтения книги. */
    @FXML
    private ProgressBar readProgressBar;

    /** Метка для отображения процента прочитанной книги. */
    @FXML
    private Label progressLabel;

    /** Метка для отображения текущей страницы, на которой читатель находится. */
    @FXML
    private Label currentPageLabel;

    /** Элемент ImageView для отображения обложки книги. */
    @FXML
    private ImageView coverImageView;

    /** Текстовое поле для поиска книг по названию или автору. */
    @FXML
    private TextField searchField;

    /** Выпадающий список для выбора жанра для фильтрации. */
    @FXML
    private ComboBox<String> genreFilterComboBox;

    /** Все книги, загруженные из базы данных. */
    private final ObservableList<Book> allBooks = FXCollections.observableArrayList();

    /** Отфильтрованный список книг на основе поиска и выбранного жанра. */
    private FilteredList<Book> filteredBooks;

    /**
     * Инициализация контроллера.
     * Загружает книги из базы данных, настраивает фильтрацию и отображение списка книг.
     */
    @FXML
    public void initialize() {
        allBooks.setAll(DatabaseHelper.getAllBooks());

        filteredBooks = new FilteredList<>(allBooks, _ -> true);
        listViewBook.setItems(filteredBooks);

        listViewBook.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                setText((empty || book == null) ? null : book.getTitle());
            }
        });

        searchField.textProperty().addListener((_, _, _) -> applyFilters());

        genreFilterComboBox.getItems().clear();
        genreFilterComboBox.getItems().add("Все жанры");
        genreFilterComboBox.getItems().addAll(DatabaseHelper.getAllGenres());
        genreFilterComboBox.setValue("Все жанры");
        genreFilterComboBox.setOnAction(this::onGenreFilterChanged);
    }

    /**
     * Обработчик изменения выбранного жанра в ComboBox.
     *
     * @param event событие изменения значения ComboBox
     */
    @FXML
    private void onGenreFilterChanged(ActionEvent event) {
        applyFilters();
    }

    /**
     * Обработчик нажатия кнопки добавления книги.
     * Открывает окно добавления новой книги.
     *
     * @param event событие нажатия кнопки
     * @throws IOException в случае ошибки загрузки FXML
     */
    @FXML
    private void onAddBookItem(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("add-edit-book-view.fxml"));
        Parent root = fxmlLoader.load();

        AddEditBookController controller = fxmlLoader.getController();
        controller.setBook(null);

        Stage addBookStage = new Stage();
        addBookStage.setTitle("Добавить книгу");
        addBookStage.setScene(new Scene(root));
        addBookStage.setResizable(false);
        addBookStage.initModality(Modality.WINDOW_MODAL);
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        addBookStage.initOwner(primaryStage);
        addBookStage.showAndWait();

        updateListViewItems();
    }

    /**
     * Обработчик удаления выбранной книги из базы данных.
     * Показывает подтверждающее диалоговое окно.
     */
    @FXML
    private void onDeleteButton() {
        Book selectedBook = getSelectedBook();
        if (selectedBook == null) {
            showWarning("Удаление книги", "Пожалуйста, выберите книгу для удаления.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Вы действительно хотите удалить книгу \"" + selectedBook.getTitle() + "\"?");
        alert.setContentText("Это действие необратимо.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DatabaseHelper.deleteBookById(selectedBook.getId());
            updateListViewItems();
            clearBookDetails();
        }
    }

    /**
     * Обработчик нажатия кнопки редактирования выбранной книги.
     * Открывает окно редактирования.
     *
     * @param event событие нажатия кнопки
     * @throws IOException в случае ошибки загрузки FXML
     */
    @FXML
    private void onEditItem(ActionEvent event) throws IOException {
        Book selectedBook = getSelectedBook();
        if (selectedBook == null) return;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("add-edit-book-view.fxml"));
        Parent root = loader.load();

        AddEditBookController controller = loader.getController();
        controller.setBook(selectedBook);

        Stage stage = new Stage();
        stage.setTitle("Редактировать книгу");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(((Node) event.getSource()).getScene().getWindow());
        stage.setScene(new Scene(root));
        stage.showAndWait();

        updateListViewItems();
    }

    /**
     * Обработчик нажатия кнопки открытия книги.
     * Отображает детали выбранной книги.
     */
    @FXML
    private void onOpenButton() {
        Book selectedBook = getSelectedBook();
        if (selectedBook == null) {
            clearBookDetails();
            return;
        }

        showBookDetails(selectedBook);
    }

    /**
     * Получает выбранную книгу из списка.
     *
     * @return выбранная книга или null, если ничего не выбрано
     */
    private Book getSelectedBook() {
        return listViewBook.getSelectionModel().getSelectedItem();
    }

    /**
     * Обновляет список книг из базы данных.
     */
    private void updateListViewItems() {
        allBooks.setAll(DatabaseHelper.getAllBooks());
    }

    /**
     * Применяет фильтры поиска и жанра к списку книг.
     */
    private void applyFilters() {
        String selectedGenre = genreFilterComboBox.getValue();
        if (selectedGenre == null) selectedGenre = "Все жанры";

        String searchText = searchField.getText() != null
                ? searchField.getText().toLowerCase().trim()
                : "";

        String finalSelectedGenre = selectedGenre;
        filteredBooks.setPredicate(book -> {
            if (book == null) return false;

            List<String> genresList = book.getGenres() == null
                    ? List.of()
                    : Arrays.asList(book.getGenres().split(",\\s*"));

            boolean matchesGenre = finalSelectedGenre.equals("Все жанры") || genresList.contains(finalSelectedGenre);

            boolean matchesSearch = searchText.isEmpty()
                    || book.getTitle().toLowerCase().contains(searchText)
                    || book.getAuthor().toLowerCase().contains(searchText);

            return matchesGenre && matchesSearch;
        });
    }

    /**
     * Отображает детали выбранной книги в панели информации.
     *
     * @param book книга для отображения
     */
    private void showBookDetails(Book book) {
        titleBook.setText(book.getTitle());
        bookAuthorLabel.setText(book.getAuthor());
        yearLabel.setText(String.valueOf(book.getYear()));
        pagesLabel.setText(String.valueOf(book.getPages()));
        ratingLabel.setText(String.valueOf(book.getRating()));
        currentPageLabel.setText(String.valueOf(book.getCurrentPage()));

        genresContainer.getChildren().clear();
        if (book.getGenres() != null) {
            Arrays.stream(book.getGenres().split(","))
                    .map(String::trim)
                    .forEach(genre -> {
                        Label label = new Label(genre);
                        genresContainer.getChildren().add(label);
                    });
        }

        int currentPage = book.getCurrentPage();
        int totalPages = book.getPages();
        double progress = totalPages > 0 ? (double) currentPage / totalPages : 0.0;
        readProgressBar.setProgress(progress);
        progressLabel.setText((int)(progress * 100) + "%");

        Image coverImage = book.getCoverAsImage();
        coverImageView.setImage(coverImage);
    }

    /**
     * Очищает панель отображения информации о книге.
     */
    private void clearBookDetails() {
        titleBook.setText("");
        bookAuthorLabel.setText("");
        yearLabel.setText("");
        pagesLabel.setText("");
        ratingLabel.setText("");
        currentPageLabel.setText("");
        genresContainer.getChildren().clear();
        readProgressBar.setProgress(0);
        progressLabel.setText("");
        coverImageView.setImage(null);
    }

    /**
     * Показывает предупреждающее диалоговое окно с указанным заголовком и сообщением.
     *
     * @param title заголовок окна предупреждения
     * @param message сообщение для пользователя
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
