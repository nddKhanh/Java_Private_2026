public class Book extends Product {
    private String author;

    public Book(String id, String name, double price, int stock, String author) {
        super(id, name, price, stock);
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public boolean isExpensive() {
        return getPrice() > 20000;
    }
}
