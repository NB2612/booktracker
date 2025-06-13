# Дневник чтения — архитектура и паттерны

## SOLID

### Single Responsibility Principle (Принцип единственной ответственности)

Каждый класс должен иметь только одну ответственность и одну причину для изменения.

В проекте класс `BookRepository` отвечает только за операции с данными книг — добавление, удаление, поиск.  
```java
public interface BookRepository {
    void insert(Book book);
    void update(Book book);
    void delete(int id);
}
```

Если объединить логику UI и работу с данными, например в `MainController`, класс станет сложным и нарушит SRP.

```java
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
```


### Open/Closed Principle (Принцип открытости/закрытости)

Код открыт для расширения, но закрыт для модификации.

Фильтрация книг реализована с возможностью добавлять новые фильтры без изменения базового кода — через паттерн Strategy.
```java
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
```
Это позволяет расширять функциональность без риска поломать существующий код. Например: если бы фильтр включал только определенные типы жанров, то при добавлении новых пришлось бы их добавлять в ручную в коде.

### Liskov Substitution Principle (Принцип подстановки Лисков)

Наследники класса `Book` (например, `EBook`) могут использоваться вместо базового класса без нарушения логики приложения.

```java
public class EBook extends Book {
    private String eBookModel;
}
```
Если бы для каждого типа книги пришлось писать отдельные методы, это бы усложнило код и нарушило LSP. Так же в таком случае нарушается принцип DRY
```java
public class Book {
    private int id;
    private String title;
    private String author;
    private int year;
    //и так далее.
}
public class EBook {
    private int id;
    private String eBookModel;
    private String title;
    private String author;
    private int year;
    //и так далее.
}
```


### Interface Segregation Principle (Принцип разделения интерфейсов)

Интерфейсы должны быть узконаправленными.
```java
public interface BookRepository {
    void insert(Book book);
    void update(Book book);
    void delete(Book book);
}
```
Пока в проекте достаточно одного интерфейса `BookRepository` с базовыми методами.

### Dependency Inversion Principle (Принцип инверсии зависимостей)

Классы зависят от абстракций, а не от конкретных реализаций.

Вместо создания экземпляров репозитория внутри контроллеров зависимости передаются через конструктор.  
Это улучшает тестируемость и упрощает замену реализации.
```java
public class Book {
    private int id;
    private String title;
    private String author;
    private int year;
    private int pages;
    private double rating;
    private String genres;
    private int currentPage;
    private byte[] cover;

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
}
```
Пример нарушения принципа инверсии зависимостей:
```java
public class AddEditBookController {
    private int id;
    private String title;
    private String author;
    private int year;
    private int pages;
    private double rating;
    private String genres;
    private int currentPage;
    private byte[] cover;

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
}

```
---

## Принцип DRY (Don't Repeat Yourself)

Повторяющийся код, например проверка корректности UUID, вынесен в утилитный класс `UUIDUtils`.  
Это уменьшает дублирование и повышает надежность. 
В моем приложении функция проверки данных находится в контроллере `AddEditController`. Но по скольку метод создания новой книги и редактирование существующей
используют одну и туже форму, и проверять данные в приложении более ни где не требуется, было решено оставить их тут. В случае если нужно будет 
где-то еще проверять валидность данных, следует этот код переместить в отдельный утилитный класс.
```java
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
```
---

## Применение паттернов

### Strategy (Стратегия)

Используется стратегия базовой фильтрации. В дальнейшем можно будет добавить дополнительные поля и условия для фильтрации данных, не
ломая существующий код.

```java
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
```
