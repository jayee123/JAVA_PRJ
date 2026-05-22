// User.java (抽象類別)
public abstract class User {
    protected String id;
    protected String name;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    
    // 抽象方法：顯示專屬選單 (展現多型)
    public abstract void displayMenu();
}