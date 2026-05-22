// Main.java
import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class Main {
    private static List<Event>    eventList = new ArrayList<>();
    private static List<String[]> userList  = new ArrayList<>();   // [id, name, role]
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        eventList = FileManager.loadEvents();
        userList  = FileManager.loadUsers();

        System.out.println("歡迎來到校園活動管理系統！");
        while (true) {
            System.out.print("\n請輸入您的登入 ID (輸入 exit 離開): ");
            String loginId = scanner.nextLine().trim();

            if (loginId.equalsIgnoreCase("exit")) break;
            if (loginId.isEmpty()) continue;

            User currentUser = login(loginId);
            if (currentUser != null) handleUserSession(currentUser);
        }

        System.out.println("感謝使用，系統關閉。");
    }

    // ── 登入 / 使用者檔 ───────────────────────────────────────────────────────

    private static User login(String loginId) {
        String[] record = FileManager.findUser(loginId, userList);

        if (record != null) {
            // 已有紀錄，直接依 role 建立物件
            String role = record[2].toUpperCase();
            if (role.equals("S")) {
                System.out.println("登入成功！歡迎回來，" + record[1]);
                return new Student(record[0], record[1]);
            } else if (role.equals("O")) {
                System.out.println("登入成功！歡迎回來，" + record[1]);
                return new Organizer(record[0], record[1]);
            }
        }

        // 第一次使用：以 ID 首字自動判斷角色，並詢問姓名後寫入檔
        String upperPrefix = loginId.substring(0, 1).toUpperCase();
        String role;
        if (upperPrefix.equals("S"))      role = "S";
        else if (upperPrefix.equals("O")) role = "O";
        else {
            System.out.println("無效的 ID 格式！（請使用 S 開頭的學生帳號或 O 開頭的主辦者帳號）");
            return null;
        }

        System.out.print("首次登入，請輸入您的姓名: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = (role.equals("S") ? "學生_" : "主辦_") + loginId;

        String[] newRecord = {loginId, name, role};
        userList.add(newRecord);
        FileManager.saveUsers(userList);
        System.out.println("帳號建立成功！歡迎，" + name);

        if (role.equals("S")) return new Student(loginId, name);
        else                   return new Organizer(loginId, name);
    }

    // ── 會話迴圈 ──────────────────────────────────────────────────────────────

    private static void handleUserSession(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            user.displayMenu();
            System.out.print("請選擇操作: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                FileManager.saveEvents(eventList);
                loggedIn = false;
            } else if (user instanceof Student) {
                handleStudentAction((Student) user, choice);
            } else if (user instanceof Organizer) {
                handleOrganizerAction((Organizer) user, choice);
            }
        }
    }

    // ── 學生功能 ──────────────────────────────────────────────────────────────

    private static void handleStudentAction(Student student, String choice) {
        switch (choice) {
            case "1": // 瀏覽所有活動
                printAllEvents(false);
                break;

            case "2": // 查詢活動詳細資訊
                viewEventDetail();
                break;

            case "3": // 報名活動
                System.out.print("請輸入想報名的活動 ID: ");
                String regId = scanner.nextLine().trim();
                Event regEvent = findEventById(regId);
                if (regEvent == null) {
                    System.out.println("找不到該活動！");
                } else if (student.registerEvent(regEvent)) {
                    System.out.println("報名成功！");
                } else {
                    System.out.println("報名失敗（可能已額滿或您已報名）！");
                }
                break;

            case "4": // 取消報名
                System.out.print("請輸入想取消報名的活動 ID: ");
                String cancelId = scanner.nextLine().trim();
                Event cancelEvent = findEventById(cancelId);
                if (cancelEvent != null && student.cancelEvent(cancelEvent)) {
                    System.out.println("取消報名成功！");
                } else {
                    System.out.println("取消失敗（您可能未報名此活動）！");
                }
                break;

            case "5": // 查詢個人報名紀錄
                viewMyRegistrations(student);
                break;

            default:
                System.out.println("無效的選項！");
        }
    }

    // ── 主辦者功能 ────────────────────────────────────────────────────────────

    private static void handleOrganizerAction(Organizer organizer, String choice) {
        switch (choice) {
            case "1": // 建立新活動
                createEvent(organizer);
                break;

            case "2": // 檢視所有活動
                printAllEvents(true);
                break;

            case "3": // 查詢活動詳細資訊
                viewEventDetail();
                break;

            case "4": // 編輯我的活動
                editEvent(organizer);
                break;

            default:
                System.out.println("無效的選項！");
        }
    }

    // ── 共用功能方法 ──────────────────────────────────────────────────────────

    /** 判斷活動是否還未結束（今天含今天都算即將舉行）。*/
    private static boolean isUpcoming(Event e) {
        try {
            LocalDate eventDate = LocalDate.parse(e.getEventDate());
            LocalDate today     = LocalDate.now();
            if (eventDate.isAfter(today)) return true;
            if (eventDate.isEqual(today)) {
                LocalTime eventTime = LocalTime.parse(e.getEventTime());
                return !eventTime.isBefore(LocalTime.now());
            }
            return false;
        } catch (DateTimeParseException ex) {
            return true; // 日期格式異常時仍顯示，不靜默過濾
        }
    }

    /**
     * 列出活動清單（按日期時間排序）。
     * showOrganizer=false（學生）：只顯示即將舉行的活動。
     * showOrganizer=true（主辦者）：顯示全部，已結束的標示 [已結束]。
     */
    private static void printAllEvents(boolean showOrganizer) {
        if (eventList.isEmpty()) {
            System.out.println("目前沒有任何活動。");
            return;
        }
        List<Event> sorted = new ArrayList<>(eventList);
        Collections.sort(sorted);

        if (!showOrganizer) {
            // 學生：只列即將舉行的活動
            List<Event> upcoming = new ArrayList<>();
            for (Event e : sorted) {
                if (isUpcoming(e)) upcoming.add(e);
            }
            if (upcoming.isEmpty()) {
                System.out.println("目前沒有即將舉行的活動。");
                return;
            }
            System.out.println("--- 即將舉行的活動清單（按日期時間排序）---");
            for (Event e : upcoming) {
                System.out.printf("[%s] %s｜%s %s｜地點: %s｜報名: %d/%d%n",
                    e.getId(), e.getTitle(), e.getEventDate(), e.getEventTime(),
                    e.getLocation(), e.getParticipantIds().size(), e.getCapacity());
            }
        } else {
            // 主辦者：顯示全部，已結束加標示
            System.out.println("--- 所有活動清單（按日期時間排序）---");
            for (Event e : sorted) {
                String status = isUpcoming(e) ? "" : "【已結束】";
                System.out.printf("%s[%s] %s｜%s %s｜地點: %s｜主辦: %s｜報名: %d/%d%n",
                    status, e.getId(), e.getTitle(), e.getEventDate(), e.getEventTime(),
                    e.getLocation(), e.getOrganizerId(),
                    e.getParticipantIds().size(), e.getCapacity());
            }
        }
    }

    /** 輸入活動 ID，顯示完整詳細資訊（含報名名單）。*/
    private static void viewEventDetail() {
        System.out.print("請輸入要查詢的活動 ID: ");
        String id = scanner.nextLine().trim();
        Event e = findEventById(id);
        if (e == null) {
            System.out.println("找不到該活動！");
            return;
        }
        System.out.println("══════════════════════════════");
        System.out.println("活動 ID  ：" + e.getId());
        System.out.println("標題     ：" + e.getTitle());
        System.out.println("地點     ：" + e.getLocation());
        System.out.println("日期時間  ：" + e.getEventDate() + " " + e.getEventTime());
        System.out.println("主辦單位  ：" + e.getOrganizerId());
        System.out.println("名額     ：" + e.getParticipantIds().size() + " / " + e.getCapacity());
        if (e.getParticipantIds().isEmpty()) {
            System.out.println("報名名單  ：（尚無人報名）");
        } else {
            System.out.println("報名名單  ：" + String.join(", ", e.getParticipantIds()));
        }
        System.out.println("══════════════════════════════");
    }

    /** 顯示學生自己報名過的所有活動。*/
    private static void viewMyRegistrations(Student student) {
        List<Event> myEvents = new ArrayList<>();
        for (Event e : eventList) {
            if (e.getParticipantIds().contains(student.getId())) myEvents.add(e);
        }
        if (myEvents.isEmpty()) {
            System.out.println("您目前尚未報名任何活動。");
            return;
        }
        Collections.sort(myEvents);
        System.out.println("--- 您的報名紀錄 ---");
        for (Event e : myEvents) {
            System.out.printf("[%s] %s｜%s %s｜地點: %s%n",
                e.getId(), e.getTitle(), e.getEventDate(), e.getEventTime(), e.getLocation());
        }
    }

    /** 建立新活動（主辦者）。*/
    private static void createEvent(Organizer organizer) {
        System.out.print("請輸入活動 ID (例: E01): ");
        String id = scanner.nextLine().trim();

        if (findEventById(id) != null) {
            System.out.println("錯誤：活動 ID「" + id + "」已存在，請使用不同的 ID！");
            return;
        }

        System.out.print("請輸入活動標題: ");
        String title = scanner.nextLine().trim();

        System.out.print("請輸入活動地點: ");
        String location = scanner.nextLine().trim();

        int capacity = readPositiveInt("請輸入活動名額 (必須為正整數): ");

        String eventDate = readDate("請輸入活動日期 (格式: YYYY-MM-DD): ");
        String eventTime = readTime("請輸入活動時間 (格式: HH:MM): ");

        eventList.add(new Event(id, title, location, capacity, organizer.getId(), eventDate, eventTime));
        System.out.println("活動建立成功！");
    }

    /** 編輯活動（主辦者）。*/
    private static void editEvent(Organizer organizer) {
        System.out.print("請輸入要編輯的活動 ID: ");
        String editId = scanner.nextLine().trim();
        Event e = findEventById(editId);
        if (e == null) { System.out.println("找不到該活動！"); return; }
        if (!e.getOrganizerId().equals(organizer.getId())) {
            System.out.println("錯誤：您只能編輯自己主辦的活動！"); return;
        }

        System.out.print("請輸入新標題 (空白保留原值 [" + e.getTitle() + "]): ");
        String v = scanner.nextLine().trim();
        if (!v.isEmpty()) e.setTitle(v);

        System.out.print("請輸入新地點 (空白保留原值 [" + e.getLocation() + "]): ");
        v = scanner.nextLine().trim();
        if (!v.isEmpty()) e.setLocation(v);

        int newCap = readCapacityEdit(
            "請輸入新名額 (直接 Enter 或輸入 -1 保留原值 [" + e.getCapacity() + "]): ",
            e.getParticipantIds().size()
        );
        if (newCap != -1) e.setCapacity(newCap);

        System.out.print("請輸入新日期 (空白保留原值 [" + e.getEventDate() + "]) (格式: YYYY-MM-DD): ");
        v = scanner.nextLine().trim();
        if (!v.isEmpty()) {
            if (v.matches("\\d{4}-\\d{2}-\\d{2}")) e.setEventDate(v);
            else System.out.println("日期格式不正確，日期未修改。");
        }

        System.out.print("請輸入新時間 (空白保留原值 [" + e.getEventTime() + "]) (格式: HH:MM): ");
        v = scanner.nextLine().trim();
        if (!v.isEmpty()) {
            if (v.matches("\\d{2}:\\d{2}")) e.setEventTime(v);
            else System.out.println("時間格式不正確，時間未修改。");
        }

        System.out.println("活動編輯完成！");
    }

    // ── 工具方法 ──────────────────────────────────────────────────────────────

    private static Event findEventById(String id) {
        for (Event e : eventList) {
            if (e.getId().equals(id)) return e;
        }
        return null;
    }

    /**
     * 編輯名額專用輸入：直接 Enter 或輸入 -1 代表保留原值，回傳 -1。
     * 輸入無效（非數字、<= 0、小於已報名人數）時反覆詢問。
     */
    private static int readCapacityEdit(String prompt, int registeredCount) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty() || input.equals("-1")) return -1;
            try {
                int v = Integer.parseInt(input);
                if (v <= 0) {
                    System.out.println("錯誤：名額必須大於 0，請重新輸入！");
                } else if (v < registeredCount) {
                    System.out.println("錯誤：新名額不能小於已報名人數（" + registeredCount + " 人），請重新輸入！");
                } else {
                    return v;
                }
            } catch (NumberFormatException e) {
                System.out.println("錯誤：請輸入有效的數字、直接 Enter 或 -1 保留原值！");
            }
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(scanner.nextLine().trim());
                if (v > 0) return v;
                System.out.println("錯誤：必須大於 0！");
            } catch (NumberFormatException e) {
                System.out.println("錯誤：請輸入有效的數字！");
            }
        }
    }

    private static String readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String v = scanner.nextLine().trim();
            if (v.matches("\\d{4}-\\d{2}-\\d{2}")) return v;
            System.out.println("錯誤：日期格式不正確，請使用 YYYY-MM-DD！");
        }
    }

    private static String readTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String v = scanner.nextLine().trim();
            if (v.matches("\\d{2}:\\d{2}")) return v;
            System.out.println("錯誤：時間格式不正確，請使用 HH:MM！");
        }
    }
}
