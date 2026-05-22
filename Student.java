// Student.java
public class Student extends User implements Registerable {
    public Student(String id, String name) {
        super(id, name);
    }

    @Override
    public void displayMenu() {
        System.out.println("\n--- 學生選單 (" + name + ") ---");
        System.out.println("1. 瀏覽所有活動");
        System.out.println("2. 查詢活動詳細資訊");
        System.out.println("3. 報名活動");
        System.out.println("4. 取消報名");
        System.out.println("5. 查詢我的報名紀錄");
        System.out.println("0. 登出並存檔");
    }

    @Override
    public boolean registerEvent(Event e) {
        return e.addParticipant(this.id);
    }

    @Override
    public boolean cancelEvent(Event e) {
        return e.removeParticipant(this.id);
    }
}
