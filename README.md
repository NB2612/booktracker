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
Это позволяет расширять функциональность без риска поломать существующий код.

### Liskov Substitution Principle (Принцип подстановки Лисков)

Наследники класса `Book` (например, `EBook`) могут использоваться вместо базового класса без нарушения логики приложения.

Если бы для каждого типа книги пришлось писать отдельные методы, это бы усложнило код и нарушило LSP.

### Interface Segregation Principle (Принцип разделения интерфейсов)

Интерфейсы должны быть узконаправленными.

Пока в проекте достаточно одного интерфейса `BookRepository` с базовыми методами.  
В будущем при усложнении бизнес-логики интерфейс можно разделить на несколько более специализированных.

### Dependency Inversion Principle (Принцип инверсии зависимостей)

Классы зависят от абстракций, а не от конкретных реализаций.

Вместо создания экземпляров репозитория внутри контроллеров зависимости передаются через конструктор.  
Это улучшает тестируемость и упрощает замену реализации.
```java
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
```
---

## Принцип DRY (Don't Repeat Yourself)

Повторяющийся код, например проверка корректности UUID, вынесен в утилитный класс `UUIDUtils`.  
Это уменьшает дублирование и повышает надежность.

---

## Применение паттернов

### Strategy (Стратегия)

Используется для фильтрации списка книг. Фильтры оформлены как отдельные классы, реализующие общий интерфейс:

```java
public interface BookFilterStrategy {
    boolean test(Book book);
}
