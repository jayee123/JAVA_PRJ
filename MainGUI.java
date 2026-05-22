import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainGUI extends JFrame {

    private static List<Event>    eventList = new ArrayList<>();
    private static List<String[]> userList  = new ArrayList<>();
    private User currentUser;

    private static final Color C_PRIMARY = new Color(25, 118, 210);
    private static final Color C_LIGHT   = new Color(245, 247, 250);
    private static final Color C_SUCCESS = new Color(46, 125, 50);
    private static final Color C_DANGER  = new Color(198, 40, 40);
    private static final Color C_GREY    = new Color(97, 97, 97);
    private static final Color C_TEAL    = new Color(0, 131, 143);
    private static final Color C_TEXT    = new Color(33, 33, 33);

    private static final Font F_TITLE  = new Font("Microsoft JhengHei", Font.BOLD, 22);
    private static final Font F_LABEL  = new Font("Microsoft JhengHei", Font.PLAIN, 14);
    private static final Font F_BTN    = new Font("Microsoft JhengHei", Font.BOLD, 13);
    private static final Font F_TABLE  = new Font("Microsoft JhengHei", Font.PLAIN, 13);
    private static final Font F_HEADER = new Font("Microsoft JhengHei", Font.BOLD, 13);

    private CardLayout cardLayout;
    private JPanel     cardPanel;

    // Login
    private JTextField loginIdField;

    // Student
    private JLabel            studentWelcomeLabel;
    private DefaultTableModel studentModel;
    private JTable            studentTable;
    private JLabel            studentCountLabel;
    private DefaultTableModel myRegModel;
    private JTable            myRegTable;

    // Organizer
    private JLabel            orgWelcomeLabel;
    private DefaultTableModel orgModel;
    private JTable            orgTable;

    // ─────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        eventList = FileManager.loadEvents();
        userList  = FileManager.loadUsers();
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new MainGUI().setVisible(true);
        });
    }

    public MainGUI() {
        super("校園活動管理系統");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 660);
        setMinimumSize(new Dimension(780, 520));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                FileManager.saveEvents(eventList);
                FileManager.saveUsers(userList);
            }
        });

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.add(buildLoginPanel(),     "login");
        cardPanel.add(buildStudentPanel(),   "student");
        cardPanel.add(buildOrganizerPanel(), "organizer");
        setContentPane(cardPanel);
    }

    // ═════════════════ LOGIN ═════════════════════════════════════════════
    private JPanel buildLoginPanel() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(C_LIGHT);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(44, 56, 44, 56)));

        JLabel title = new JLabel("校園活動管理系統", SwingConstants.CENTER);
        title.setFont(F_TITLE); title.setForeground(C_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Campus Event Management System", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 12)); sub.setForeground(Color.GRAY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel idLabel = new JLabel("登入 ID");
        idLabel.setFont(F_LABEL); idLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginIdField = new JTextField(18);
        loginIdField.setFont(F_LABEL);
        loginIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        loginIdField.addActionListener(e -> doLogin());

        JButton loginBtn = styledBtn("登 入", C_PRIMARY);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());

        JButton firstLoginBtn = new JButton("首次登入（建立新帳號）");
        firstLoginBtn.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        firstLoginBtn.setForeground(C_PRIMARY);
        firstLoginBtn.setBackground(Color.WHITE);
        firstLoginBtn.setBorder(BorderFactory.createLineBorder(C_PRIMARY, 1));
        firstLoginBtn.setFocusPainted(false);
        firstLoginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        firstLoginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        firstLoginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        firstLoginBtn.addActionListener(e -> doFirstLogin());

        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(30));
        card.add(idLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(loginIdField);
        card.add(Box.createVerticalStrut(18));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(firstLoginBtn);

        root.add(card);
        return root;
    }

    private void doLogin() {
        String id = loginIdField.getText().trim();
        if (id.isEmpty()) { err("請輸入登入 ID！"); return; }

        String[] rec = FileManager.findUser(id, userList);
        if (rec == null) {
            err("帳號不存在！\n若您是第一次使用，請點選「首次登入（建立新帳號）」按鈕。");
            return;
        }

        currentUser = rec[2].equalsIgnoreCase("S")
            ? new Student(rec[0], rec[1])
            : new Organizer(rec[0], rec[1]);
        loginIdField.setText("");

        if (currentUser instanceof Student) {
            studentWelcomeLabel.setText("歡迎，" + currentUser.getName() + "（" + currentUser.getId() + "）");
            refreshStudentTable();
            refreshMyReg();
            cardLayout.show(cardPanel, "student");
        } else {
            orgWelcomeLabel.setText("歡迎，" + currentUser.getName() + "（" + currentUser.getId() + "）");
            refreshOrgTable();
            cardLayout.show(cardPanel, "organizer");
        }
    }

    private void doFirstLogin() {
        JDialog dlg = new JDialog(this, "首次登入 － 建立新帳號", true);
        dlg.setSize(400, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(28, 36, 24, 36));

        JLabel hint = new JLabel("S 開頭 = 學生帳號  ·  O 開頭 = 主辦者帳號", SwingConstants.CENTER);
        hint.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 11));
        hint.setForeground(Color.GRAY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel idLabel = new JLabel("登入 ID");
        idLabel.setFont(F_LABEL);
        JTextField idField = new JTextField(18);
        idField.setFont(F_LABEL);
        idField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel nameLabel = new JLabel("姓名");
        nameLabel.setFont(F_LABEL);
        JTextField nameField = new JTextField(18);
        nameField.setFont(F_LABEL);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton confirmBtn = styledBtn("建立帳號並登入", C_PRIMARY);
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirmBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        Runnable doCreate = () -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            if (id.isEmpty()) { err("請輸入登入 ID！"); return; }
            String p = id.substring(0, 1).toUpperCase();
            if (!p.equals("S") && !p.equals("O")) {
                err("無效的 ID！S 開頭 = 學生，O 開頭 = 主辦者"); return;
            }
            if (FileManager.findUser(id, userList) != null) {
                err("此帳號已存在！請關閉此視窗直接使用「登 入」按鈕。"); return;
            }
            if (name.isEmpty()) name = (p.equals("S") ? "學生_" : "主辦_") + id;
            userList.add(new String[]{id, name, p});
            FileManager.saveUsers(userList);
            currentUser = p.equals("S") ? new Student(id, name) : new Organizer(id, name);
            dlg.dispose();
            if (currentUser instanceof Student) {
                studentWelcomeLabel.setText("歡迎，" + currentUser.getName() + "（" + currentUser.getId() + "）");
                refreshStudentTable();
                refreshMyReg();
                cardLayout.show(cardPanel, "student");
            } else {
                orgWelcomeLabel.setText("歡迎，" + currentUser.getName() + "（" + currentUser.getId() + "）");
                refreshOrgTable();
                cardLayout.show(cardPanel, "organizer");
            }
        };

        confirmBtn.addActionListener(e -> doCreate.run());
        idField.addActionListener(e -> nameField.requestFocus());
        nameField.addActionListener(e -> doCreate.run());

        card.add(hint);
        card.add(Box.createVerticalStrut(20));
        card.add(idLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(idField);
        card.add(Box.createVerticalStrut(12));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(nameField);
        card.add(Box.createVerticalStrut(20));
        card.add(confirmBtn);

        dlg.setContentPane(card);
        dlg.setVisible(true);
    }

    // ═════════════════ STUDENT ═══════════════════════════════════════════
    private JPanel buildStudentPanel() {
        studentWelcomeLabel = new JLabel(" ");
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LIGHT);
        root.add(topBar("學生介面", studentWelcomeLabel, this::logout), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(F_LABEL);
        tabs.addTab("所有活動", buildAllEventsTab());
        tabs.addTab("我的報名紀錄", buildMyRegTab());
        root.add(tabs, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildAllEventsTab() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(C_LIGHT);

        studentModel = tableModel("活動ID","標題","日期","時間","地點","報名/名額","狀態");
        studentTable = buildTable(studentModel);
        p.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        studentCountLabel = new JLabel(" ");
        studentCountLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        studentCountLabel.setBorder(new EmptyBorder(2, 2, 4, 0));
        p.add(studentCountLabel, BorderLayout.NORTH);

        JPanel btns = btnBar();
        JButton viewBtn = styledBtn("查看詳情", C_GREY);
        JButton regBtn  = styledBtn("報名活動",  C_SUCCESS);
        JButton canBtn  = styledBtn("取消報名",  C_DANGER);
        viewBtn.addActionListener(e -> { Event ev = selected(studentTable, studentModel); if (ev!=null) showDetail(ev); });
        regBtn.addActionListener(e -> doRegister());
        canBtn.addActionListener(e -> doCancel(studentTable, studentModel));
        btns.add(viewBtn); btns.add(regBtn); btns.add(canBtn);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildMyRegTab() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(C_LIGHT);

        myRegModel = tableModel("活動ID","標題","日期","時間","地點");
        myRegTable = buildTable(myRegModel);
        p.add(new JScrollPane(myRegTable), BorderLayout.CENTER);

        JPanel btns = btnBar();
        JButton viewBtn = styledBtn("查看詳情", C_GREY);
        JButton canBtn  = styledBtn("取消報名",  C_DANGER);
        JButton refBtn  = styledBtn("重新整理",  new Color(80,80,80));
        viewBtn.addActionListener(e -> { Event ev = selected(myRegTable, myRegModel); if (ev!=null) showDetail(ev); });
        canBtn.addActionListener(e -> doCancel(myRegTable, myRegModel));
        refBtn.addActionListener(e -> { refreshStudentTable(); refreshMyReg(); });
        btns.add(viewBtn); btns.add(canBtn); btns.add(refBtn);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private void refreshStudentTable() {
        studentModel.setRowCount(0);
        List<Event> sorted = new ArrayList<>(eventList);
        Collections.sort(sorted);
        int cnt = 0;
        for (Event e : sorted) {
            if (!isUpcoming(e)) continue;
            String st = e.getParticipantIds().size() >= e.getCapacity() ? "額滿" : "開放報名";
            studentModel.addRow(new Object[]{
                e.getId(), e.getTitle(), e.getEventDate(), e.getEventTime(),
                e.getLocation(), e.getParticipantIds().size()+"/"+e.getCapacity(), st});
            cnt++;
        }
        studentCountLabel.setText("共 " + cnt + " 個即將舉行的活動");
    }

    private void refreshMyReg() {
        myRegModel.setRowCount(0);
        if (currentUser == null) return;
        List<Event> mine = new ArrayList<>();
        for (Event e : eventList)
            if (e.getParticipantIds().contains(currentUser.getId())) mine.add(e);
        Collections.sort(mine);
        for (Event e : mine)
            myRegModel.addRow(new Object[]{e.getId(),e.getTitle(),e.getEventDate(),e.getEventTime(),e.getLocation()});
    }

    private void doRegister() {
        Event e = selected(studentTable, studentModel);
        if (e == null) return;
        if (((Student) currentUser).registerEvent(e)) {
            FileManager.saveEvents(eventList);
            refreshStudentTable(); refreshMyReg();
            info("報名成功！活動：" + e.getTitle());
        } else {
            err("報名失敗！（活動已額滿，或您已報名此活動）");
        }
    }

    private void doCancel(JTable table, DefaultTableModel model) {
        Event e = selected(table, model);
        if (e == null) return;
        if (((Student) currentUser).cancelEvent(e)) {
            FileManager.saveEvents(eventList);
            refreshStudentTable(); refreshMyReg();
            info("已取消報名：" + e.getTitle());
        } else {
            err("取消失敗！您可能尚未報名此活動。");
        }
    }

    // ═════════════════ ORGANIZER ═════════════════════════════════════════
    private JPanel buildOrganizerPanel() {
        orgWelcomeLabel = new JLabel(" ");
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_LIGHT);
        root.add(topBar("主辦者介面", orgWelcomeLabel, this::logout), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.setBorder(new EmptyBorder(10, 10, 4, 10));
        center.setBackground(C_LIGHT);

        orgModel = tableModel("活動ID","標題","日期","時間","地點","主辦者","報名/名額","狀態");
        orgTable = buildTable(orgModel);
        center.add(new JScrollPane(orgTable), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel btns = btnBar();
        btns.setBorder(new EmptyBorder(0, 10, 8, 10));
        JButton createBtn = styledBtn("建立活動", C_SUCCESS);
        JButton editBtn   = styledBtn("編輯活動", C_PRIMARY);
        JButton deleteBtn = styledBtn("刪除活動", C_DANGER);
        JButton detailBtn = styledBtn("查看詳情", C_GREY);
        JButton exportBtn = styledBtn("匯出名單 CSV", C_TEAL);
        createBtn.addActionListener(e -> dlgCreate());
        editBtn.addActionListener(e -> dlgEdit());
        deleteBtn.addActionListener(e -> doDelete());
        detailBtn.addActionListener(e -> { Event ev = selected(orgTable, orgModel); if (ev!=null) showDetail(ev); });
        exportBtn.addActionListener(e -> doExport());
        btns.add(createBtn); btns.add(editBtn); btns.add(deleteBtn);
        btns.add(detailBtn); btns.add(exportBtn);
        root.add(btns, BorderLayout.SOUTH);
        return root;
    }

    private void refreshOrgTable() {
        orgModel.setRowCount(0);
        List<Event> sorted = new ArrayList<>(eventList);
        Collections.sort(sorted);
        for (Event e : sorted) {
            String st = isUpcoming(e)
                ? (e.getParticipantIds().size() >= e.getCapacity() ? "額滿" : "開放中")
                : "已結束";
            orgModel.addRow(new Object[]{
                e.getId(), e.getTitle(), e.getEventDate(), e.getEventTime(),
                e.getLocation(), e.getOrganizerId(),
                e.getParticipantIds().size()+"/"+e.getCapacity(), st});
        }
    }

    private void dlgCreate() {
        JDialog dlg = new JDialog(this, "建立新活動", true);
        dlg.setSize(440, 410);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints g = newGbc();

        JTextField fId  = tf(""); JTextField fTi  = tf("");
        JTextField fLoc = tf(""); JTextField fCap = tf("50");
        JTextField fDt  = tf("2026-MM-DD"); JTextField fTm = tf("09:00");

        addRow(form, g, "活動 ID：",           fId);
        addRow(form, g, "標題：",              fTi);
        addRow(form, g, "地點：",              fLoc);
        addRow(form, g, "名額：",              fCap);
        addRow(form, g, "日期 (YYYY-MM-DD)：", fDt);
        addRow(form, g, "時間 (HH:MM)：",      fTm);

        JButton ok = styledBtn("建立", C_SUCCESS);
        JButton cl = styledBtn("取消", C_GREY);
        ok.addActionListener(e -> {
            String id=fId.getText().trim(), ti=fTi.getText().trim();
            String loc=fLoc.getText().trim(), dt=fDt.getText().trim(), tm=fTm.getText().trim();
            if (id.isEmpty()||ti.isEmpty()||loc.isEmpty()) { err("ID、標題、地點不能為空！"); return; }
            if (findById(id) != null) { err("活動 ID「"+id+"」已存在！"); return; }
            int cap; try { cap=Integer.parseInt(fCap.getText().trim()); if(cap<=0) throw new NumberFormatException(); }
            catch(NumberFormatException ex) { err("名額必須為正整數！"); return; }
            if (!dt.matches("\\d{4}-\\d{2}-\\d{2}")) { err("日期格式錯誤（YYYY-MM-DD）！"); return; }
            if (!tm.matches("\\d{2}:\\d{2}"))         { err("時間格式錯誤（HH:MM）！"); return; }
            eventList.add(new Event(id, ti, loc, cap, currentUser.getId(), dt, tm));
            FileManager.saveEvents(eventList);
            refreshOrgTable();
            dlg.dispose();
            info("活動「"+ti+"」建立成功！");
        });
        cl.addActionListener(e -> dlg.dispose());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        row.add(cl); row.add(ok);
        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(row, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void dlgEdit() {
        Event e = selected(orgTable, orgModel);
        if (e == null) return;
        if (!e.getOrganizerId().equals(currentUser.getId())) { err("您只能編輯自己主辦的活動！"); return; }

        JDialog dlg = new JDialog(this, "編輯活動 - " + e.getId(), true);
        dlg.setSize(440, 370);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 24, 10, 24));
        GridBagConstraints g = newGbc();

        JTextField fTi  = tf(e.getTitle());
        JTextField fLoc = tf(e.getLocation());
        JTextField fCap = tf(String.valueOf(e.getCapacity()));
        JTextField fDt  = tf(e.getEventDate());
        JTextField fTm  = tf(e.getEventTime());

        addRow(form, g, "標題：",              fTi);
        addRow(form, g, "地點：",              fLoc);
        addRow(form, g, "名額：",              fCap);
        addRow(form, g, "日期 (YYYY-MM-DD)：", fDt);
        addRow(form, g, "時間 (HH:MM)：",      fTm);

        JButton ok = styledBtn("儲存", C_SUCCESS);
        JButton cl = styledBtn("取消", C_GREY);
        ok.addActionListener(ev -> {
            String ti=fTi.getText().trim(), loc=fLoc.getText().trim();
            String dt=fDt.getText().trim(), tm=fTm.getText().trim();
            if (!ti.isEmpty())  e.setTitle(ti);
            if (!loc.isEmpty()) e.setLocation(loc);
            if (!fCap.getText().trim().isEmpty()) {
                try {
                    int cap = Integer.parseInt(fCap.getText().trim());
                    if (cap < e.getParticipantIds().size()) {
                        err("名額不能小於已報名人數（"+e.getParticipantIds().size()+"）！"); return;
                    }
                    if (cap > 0) e.setCapacity(cap);
                } catch(NumberFormatException ex) { err("名額必須為整數！"); return; }
            }
            if (!dt.isEmpty()) {
                if (!dt.matches("\\d{4}-\\d{2}-\\d{2}")) { err("日期格式錯誤！"); return; }
                e.setEventDate(dt);
            }
            if (!tm.isEmpty()) {
                if (!tm.matches("\\d{2}:\\d{2}")) { err("時間格式錯誤！"); return; }
                e.setEventTime(tm);
            }
            FileManager.saveEvents(eventList);
            refreshOrgTable();
            dlg.dispose();
            info("活動已更新！");
        });
        cl.addActionListener(ev -> dlg.dispose());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        row.add(cl); row.add(ok);
        dlg.setLayout(new BorderLayout());
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(row, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void doDelete() {
        Event e = selected(orgTable, orgModel);
        if (e == null) return;
        if (!e.getOrganizerId().equals(currentUser.getId())) { err("您只能刪除自己主辦的活動！"); return; }
        int r = JOptionPane.showConfirmDialog(this,
            "確定刪除「" + e.getTitle() + "」？此操作無法復原。",
            "確認刪除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            eventList.remove(e);
            FileManager.saveEvents(eventList);
            refreshOrgTable();
            info("活動已刪除！");
        }
    }

    private void doExport() {
        Event e = selected(orgTable, orgModel);
        if (e == null) return;
        String fname = "participants_" + e.getId() + ".csv";
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(fname))) {
            pw.println("StudentID");
            for (String sid : e.getParticipantIds()) pw.println(sid);
            info("報名名單已匯出至：" + fname);
        } catch (Exception ex) { err("匯出失敗：" + ex.getMessage()); }
    }

    // ═════════════════ SHARED ════════════════════════════════════════════
    private JPanel topBar(String role, JLabel welcomeLabel, Runnable onLogout) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(C_PRIMARY);
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("校園活動管理系統  |  " + role);
        titleLbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);

        welcomeLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
        welcomeLabel.setForeground(new Color(200, 220, 255));

        left.add(titleLbl);
        left.add(welcomeLabel);
        bar.add(left, BorderLayout.WEST);

        JButton logoutBtn = new JButton("登出");
        logoutBtn.setFont(new Font("SimHei", Font.BOLD, 14));
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setBackground(new Color(21, 80, 160));
        logoutBtn.setOpaque(true);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setBorder(new EmptyBorder(7, 18, 7, 18));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> onLogout.run());
        bar.add(logoutBtn, BorderLayout.EAST);
        return bar;
    }

    private void logout() {
        FileManager.saveEvents(eventList);
        currentUser = null;
        cardLayout.show(cardPanel, "login");
    }

    private void showDetail(Event e) {
        String parts = e.getParticipantIds().isEmpty()
            ? "（尚無人報名）" : String.join(", ", e.getParticipantIds());
        String st = isUpcoming(e)
            ? (e.getParticipantIds().size() >= e.getCapacity() ? "額滿" : "開放報名")
            : "已結束";
        String msg = String.format(
            "活動 ID：%s%n標題：%s%n地點：%s%n日期時間：%s %s%n主辦者：%s%n名額：%d / %d%n狀態：%s%n%n報名名單：%s",
            e.getId(), e.getTitle(), e.getLocation(), e.getEventDate(), e.getEventTime(),
            e.getOrganizerId(), e.getParticipantIds().size(), e.getCapacity(), st, parts);
        JTextArea ta = new JTextArea(msg);
        ta.setEditable(false);
        ta.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        ta.setBackground(new Color(248, 248, 250));
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));
        JOptionPane.showMessageDialog(this, ta, "活動詳情 - " + e.getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }

    private Event selected(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row < 0) { info("請先選擇一個活動！"); return null; }
        String id = (String) model.getValueAt(table.convertRowIndexToModel(row), 0);
        Event e = findById(id);
        if (e == null) info("找不到該活動！");
        return e;
    }

    private Event findById(String id) {
        for (Event e : eventList) if (e.getId().equals(id)) return e;
        return null;
    }

    private boolean isUpcoming(Event e) {
        try {
            LocalDate ed = LocalDate.parse(e.getEventDate());
            LocalDate td = LocalDate.now();
            if (ed.isAfter(td)) return true;
            if (ed.isEqual(td)) return !LocalTime.parse(e.getEventTime()).isBefore(LocalTime.now());
            return false;
        } catch (DateTimeParseException ex) { return true; }
    }

    // ═════════════════ UI HELPERS ════════════════════════════════════════
    private DefaultTableModel tableModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(26);
        t.setFont(F_TABLE);
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                lbl.setFont(F_HEADER);
                lbl.setBackground(C_PRIMARY);
                lbl.setForeground(Color.BLACK);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(4, 8, 4, 8));
                return lbl;
            }
        });
        t.setSelectionBackground(new Color(187, 222, 251));
        t.setSelectionForeground(C_TEXT);
        t.setGridColor(new Color(220, 220, 220));
        t.setIntercellSpacing(new Dimension(8, 4));
        t.setFillsViewportHeight(true);
        t.setAutoCreateRowSorter(true);
        return t;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(F_BTN); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }

    private JPanel btnBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBackground(C_LIGHT);
        return p;
    }

    private JTextField tf(String text) {
        JTextField f = new JTextField(text, 18);
        f.setFont(F_LABEL);
        return f;
    }

    private GridBagConstraints newGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 4, 6, 4);
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.gridy  = 0;
        return g;
    }

    private void addRow(JPanel p, GridBagConstraints g, String labelText, JTextField field) {
        g.gridx = 0; g.weightx = 0;
        JLabel lbl = new JLabel(labelText); lbl.setFont(F_LABEL);
        p.add(lbl, g);
        g.gridx = 1; g.weightx = 1.0;
        p.add(field, g);
        g.gridy++;
    }

    private void err(String msg)  { JOptionPane.showMessageDialog(this, msg, "錯誤", JOptionPane.ERROR_MESSAGE); }
    private void info(String msg) { JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.INFORMATION_MESSAGE); }
}
