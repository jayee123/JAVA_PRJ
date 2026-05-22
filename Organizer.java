// Organizer.java
public class Organizer extends User {
    public Organizer(String id, String name) {
        super(id, name);
    }

    @Override
    public void displayMenu() {
        System.out.println("\n--- 主辦者選單 (" + name + ") ---");
        System.out.println("1. 建立新活動");
        System.out.println("2. 檢視所有活動與報名人數");
        System.out.println("3. 查詢活動詳細資訊");
        System.out.println("4. 編輯我的活動");
        System.out.println("0. 登出並存檔");
    }
}
