// FileManager.java
import java.io.*;
import java.util.*;

public class FileManager {
    private static final String EVENT_FILE = "events.csv";
    private static final String USER_FILE  = "users.csv";

    // ── 活動資料 ──────────────────────────────────────────────────────────────

    // CSV 欄位: id,title,location,capacity,organizerId,eventDate,eventTime,participantIds
    public static List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        File file = new File(EVENT_FILE);
        if (!file.exists()) return events;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                try {
                    if (data.length < 7) {
                        System.out.println("警告：events.csv 第 " + lineNumber + " 行欄位不足（需要 7 欄，實際 " + data.length + " 欄），已略過。");
                        continue;
                    }
                    Event e = new Event(
                        data[0],                       // id
                        data[1],                       // title
                        data[2],                       // location
                        Integer.parseInt(data[3]),     // capacity
                        data[4],                       // organizerId
                        data[5],                       // eventDate
                        data[6]                        // eventTime
                    );
                    if (data.length == 8 && !data[7].equals("NONE")) {
                        for (String pid : data[7].split(";"))
                            e.getParticipantIds().add(pid);
                    }
                    events.add(e);
                } catch (NumberFormatException ex) {
                    System.out.println("警告：events.csv 第 " + lineNumber + " 行名額格式錯誤（值為「" + data[3] + "」），已略過。");
                }
            }
        } catch (IOException e) {
            System.out.println("讀取活動檔發生錯誤：" + e.getMessage());
        }
        return events;
    }

    public static void saveEvents(List<Event> events) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EVENT_FILE))) {
            for (Event e : events) pw.println(e.toCSV());
            System.out.println("活動資料已成功儲存至 " + EVENT_FILE);
        } catch (IOException e) {
            System.out.println("寫入活動檔發生錯誤：" + e.getMessage());
        }
    }

    // ── 使用者資料 ────────────────────────────────────────────────────────────

    // CSV 欄位: id,name,role   (role: S = 學生, O = 主辦者)
    public static List<String[]> loadUsers() {
        List<String[]> users = new ArrayList<>();
        File file = new File(USER_FILE);
        if (!file.exists()) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                if (data.length == 3) users.add(data);
            }
        } catch (IOException e) {
            System.out.println("讀取使用者檔發生錯誤：" + e.getMessage());
        }
        return users;
    }

    public static void saveUsers(List<String[]> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE))) {
            for (String[] u : users) pw.println(u[0] + "," + u[1] + "," + u[2]);
        } catch (IOException e) {
            System.out.println("寫入使用者檔發生錯誤：" + e.getMessage());
        }
    }

    /** 查詢單一使用者；找不到回傳 null。*/
    public static String[] findUser(String id, List<String[]> users) {
        for (String[] u : users) {
            if (u[0].equalsIgnoreCase(id)) return u;
        }
        return null;
    }
}
