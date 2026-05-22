// Event.java
import java.util.ArrayList;
import java.util.List;

public class Event implements Comparable<Event> {
    private String id;
    private String title;
    private String location;
    private int capacity;
    private String organizerId;
    private List<String> participantIds; // 儲存報名學生的 ID
    private String eventDate; // 活動日期 (格式: YYYY-MM-DD)
    private String eventTime;  // 活動時間 (格式: HH:MM)

    public Event(String id, String title, String location, int capacity, String organizerId, String eventDate, String eventTime) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.capacity = capacity;
        this.organizerId = organizerId;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.participantIds = new ArrayList<>();
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public String getOrganizerId() { return organizerId; }
    public List<String> getParticipantIds() { return participantIds; }
    public String getEventDate() { return eventDate; }
    public String getEventTime() { return eventTime; }

    public void setTitle(String title) { this.title = title; }
    public void setLocation(String location) { this.location = location; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }

    public boolean addParticipant(String studentId) {
        if (participantIds.size() < capacity && !participantIds.contains(studentId)) {
            participantIds.add(studentId);
            return true;
        }
        return false;
    }

    public boolean removeParticipant(String studentId) {
        return participantIds.remove(studentId);
    }

    // 實現 Comparable 接口以支持按日期時間排序
    @Override
    public int compareTo(Event other) {
        int dateCompare = this.eventDate.compareTo(other.eventDate);
        if (dateCompare != 0) return dateCompare;
        return this.eventTime.compareTo(other.eventTime);
    }

    // 轉換成 CSV 格式存檔用
    // 欄位順序: id,title,location,capacity,organizerId,eventDate,eventTime,participantIds
    public String toCSV() {
        String pIds = participantIds.isEmpty() ? "NONE" : String.join(";", participantIds);
        return id + "," + title + "," + location + "," + capacity + "," + organizerId + "," + eventDate + "," + eventTime + "," + pIds;
    }
}
