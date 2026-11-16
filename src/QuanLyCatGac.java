
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class QuanLyCatGac extends JFrame {

    private static final int SHIFTS = 12;

    private static final String[] TIMES = {
            "06:00 - 08:00",
            "08:00 - 10:00",
            "10:00 - 12:00",
            "12:00 - 14:00",
            "14:00 - 16:00",
            "16:00 - 18:00",
            "18:00 - 20:00",
            "20:00 - 22:00",
            "22:00 - 00:00",
            "00:00 - 02:00",
            "02:00 - 04:00",
            "04:00 - 06:00"
    };

    private void setTableColor() {

        table.setDefaultRenderer(Object.class,
                new javax.swing.table.DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column) {

                        Component c = super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);

                        LocalDate today = LocalDate.now();
                        LocalTime now = LocalTime.now();

                        // Mac dinh
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);

                        // Xac dinh ngay
                        LocalDate date = today.plusDays(column - 1);

                        // Cot dau tien la ten ca
                        if (column == 0) {
                            c.setBackground(new Color(230, 235, 245));
                            c.setForeground(Color.BLACK);
                        }

                        // Ngay hom nay
                        else if (date.equals(today)) {

                            int currentShift = now.getHour() < 6
                                    ? 9 + now.getHour() / 2
                                    : (now.getHour() - 6) / 2;

                            // Ca dang dien ra
                            if (row == currentShift) {
                                c.setBackground(new Color(255, 230, 150));
                                c.setForeground(new Color(180, 60, 0));
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
                            }
                        }

                        // To mau khi dang chon
                        if (isSelected) {
                            c.setBackground(new Color(100, 160, 220));
                            c.setForeground(Color.WHITE);
                        }

                        return c;
                    }
                });
    }

    private static final Path DATA_FILE = Paths.get(System.getProperty("user.home"),
            "QuanLyCatGac.dat");

    private Store store;
    private JTable table;
    private DefaultTableModel model;
    private JLabel status;

    private static class Store implements Serializable {
        private static final long serialVersionUID = 1L;

        List<String> people = new ArrayList<>();

        Map<LocalDate, String[]> schedules = new TreeMap<>();

        Map<LocalDate, Set<String>> absences = new TreeMap<>();
    }

    public QuanLyCatGac() {
        super("QUAN LY VA CAT LICH GAC");

        store = loadStore();

        buildUI();
        refreshTable();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 650);
        setMinimumSize(new Dimension(850, 500));
        setLocationRelativeTo(null);
    }

    // DOC DU LIEU
    private Store loadStore() {
        if (Files.exists(DATA_FILE)) {
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(DATA_FILE))) {

                Object obj = in.readObject();

                if (obj instanceof Store) {
                    return (Store) obj;
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Khong doc duoc du lieu cu.");
            }
        }

        return new Store();
    }

    // LUU DU LIEU
    private void saveStore() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                Files.newOutputStream(DATA_FILE))) {

            out.writeObject(store);

            status.setText("Da luu du lieu.");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Loi luu du lieu: " + ex.getMessage());
        }
    }

    // GIAO DIEN
    private void buildUI() {
        setLayout(new BorderLayout(10, 10));

        Font vietnameseFont = new Font(
                "Dialog",
                Font.BOLD,
                22);

        JLabel title = new JLabel(
                "QUAN LY VA CAT LICH GAC");

        title.setFont(vietnameseFont);

        title.setFont(
                new Font("Arial", Font.BOLD, 22));

        UIManager.put("defaultFont",
                new Font("Dialog", Font.PLAIN, 14));

        UIManager.put("Label.font",
                new Font("Dialog", Font.PLAIN, 14));

        UIManager.put("Button.font",
                new Font("Dialog", Font.PLAIN, 14));

        UIManager.put("OptionPane.messageFont",
                new Font("Dialog", Font.PLAIN, 14));

        JPanel header = new JPanel(
                new BorderLayout());

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        12, 14, 4, 14));

        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return false;
            }
        };

        table = new JTable(model);

        table.setRowHeight(34);

        // To mau ca gac hien tai
        setTableColor();

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        "LICH GAC 7 NGAY"));

        add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        addButton(actions, "Quan ly quan so",
                this::managePeople);

        addButton(actions, "Xep tu dong",
                this::autoSevenDays);

        addButton(actions, "Xep thu cong",
                this::manualAssign);

        addButton(actions, "Lich su",
                this::showHistory);

        addButton(actions, "Xuat CSV",
                this::exportCsv);

        addButton(actions, "Luu",
                this::saveStore);

        JPanel bottom = new JPanel(
                new BorderLayout());

        bottom.add(actions, BorderLayout.CENTER);

        status = new JLabel(" San sang ");

        bottom.add(status, BorderLayout.SOUTH);

        add(bottom, BorderLayout.SOUTH);
    }

    private void addButton(
            JPanel panel,
            String label,
            Runnable action) {
        JButton button = new JButton(label);

        button.addActionListener(
                e -> action.run());

        panel.add(button);
    }

    // LAY NGAY
    private LocalDate dateForColumn(int column) {
        return LocalDate.now().plusDays(column - 1);
    }

    // LAY LICH NGAY
    private String[] getDay(LocalDate date) {
        return store.schedules.computeIfAbsent(
                date,
                d -> new String[SHIFTS]);
    }

    // CAP NHAT BANG LICH
    private void refreshTable() {

        LocalDate today = LocalDate.now();

        Vector<String> columns = new Vector<>();

        columns.add("CA / GIO");

        for (int d = 0; d < 7; d++) {
            columns.add(
                    today.plusDays(d).format(
                            DateTimeFormatter.ofPattern("dd/MM")));
        }

        Vector<Vector<Object>> rows = new Vector<>();

        for (int s = 0; s < SHIFTS; s++) {

            Vector<Object> row = new Vector<>();

            row.add(
                    String.format(
                            "%02d  %s",
                            s + 1,
                            TIMES[s]));

            for (int d = 0; d < 7; d++) {

                String[] day = store.schedules.get(
                        today.plusDays(d));

                row.add(
                        day == null || day[s] == null
                                ? "Chua xep"
                                : day[s]);
            }

            rows.add(row);
        }

        model.setDataVector(rows, columns);
        table.repaint();

        status.setText(
                "Quan so: " + store.people.size());
    }

    // QUAN LY QUAN SO
    private void managePeople() {

        DefaultListModel<String> listModel = new DefaultListModel<>();

        for (String person : store.people) {
            listModel.addElement(person);
        }

        JList<String> list = new JList<>(listModel);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(38);
        list.setFont(new Font("Arial", Font.PLAIN, 14));

        // CHON NGAY
        JComboBox<LocalDate> dateBox = new JComboBox<>();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            dateBox.addItem(today.plusDays(i));
        }

        dateBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focus) {

                super.getListCellRendererComponent(
                        list, value, index, selected, focus);

                if (value instanceof LocalDate) {
                    setText(((LocalDate) value).format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }

                return this;
            }
        });

        // HIEN THI TRANG THAI BANG MAU SAC
        list.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focus) {

                super.getListCellRendererComponent(
                        list, value, index, selected, focus);

                String person = value.toString();

                LocalDate date = (LocalDate) dateBox.getSelectedItem();

                boolean absent = date != null
                        && store.absences.getOrDefault(
                                date, Collections.emptySet())
                                .contains(person);

                if (absent) {
                    setText("  " + person + "     [VANG]");
                    setForeground(selected
                            ? Color.WHITE
                            : new Color(190, 45, 45));

                    setBackground(selected
                            ? new Color(190, 45, 45)
                            : new Color(255, 230, 230));

                } else {
                    setText("  " + person + "     [CO MAT]");
                    setForeground(selected
                            ? Color.WHITE
                            : new Color(30, 130, 65));

                    setBackground(selected
                            ? new Color(30, 130, 65)
                            : new Color(225, 245, 230));
                }

                return this;
            }
        });

        // CAC THANH PHAN
        JTextField name = new JTextField(15);

        JButton add = new JButton("Them quan so");
        JButton remove = new JButton("Xoa quan so");

        JButton absent = new JButton("Cho vang");
        JButton present = new JButton("Co mat tro lai");

        JLabel countLabel = new JLabel();

        Runnable updateCount = () -> {

            LocalDate date = (LocalDate) dateBox.getSelectedItem();

            int absentCount = date == null ? 0
                    : store.absences.getOrDefault(
                            date, Collections.emptySet()).size();

            countLabel.setText(
                    "Tong quan so: " + store.people.size()
                            + " | Vang: " + absentCount
                            + " | Co mat: " + (store.people.size() - absentCount));
        };

        // KHU VUC THEM QUAN SO
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addPanel.add(new JLabel("Ho ten:"));
        addPanel.add(name);
        addPanel.add(add);

        name.addActionListener(e -> {
            addQuanSo(listModel, list, name, updateCount);
        });

        // KHU VUC CHON NGAY
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        datePanel.add(new JLabel("Ngay quan ly:"));
        datePanel.add(dateBox);

        // KHU VUC TRANG THAI
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel presentLabel = new JLabel("  ■ CO MAT  ");
        presentLabel.setForeground(new Color(30, 130, 65));

        JLabel absentLabel = new JLabel("  ■ VANG  ");
        absentLabel.setForeground(new Color(190, 45, 45));

        statusPanel.add(presentLabel);
        statusPanel.add(absentLabel);
        statusPanel.add(countLabel);

        JPanel top = new JPanel(new GridLayout(3, 1));

        top.add(addPanel);
        top.add(datePanel);
        top.add(statusPanel);

        // CAC NUT CHUC NANG
        JPanel bottom = new JPanel(new GridLayout(1, 3, 8, 5));

        bottom.add(absent);
        bottom.add(present);
        bottom.add(remove);

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        panel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.setPreferredSize(new Dimension(650, 450));

        panel.add(top, BorderLayout.NORTH);

        panel.add(
                new JScrollPane(list),
                BorderLayout.CENTER);

        panel.add(bottom, BorderLayout.SOUTH);

        // DOI NGAY -> CAP NHAT TRANG THAI
        dateBox.addActionListener(e -> {
            list.repaint();
            updateCount.run();
        });

        // THEM QUAN SO
        add.addActionListener(e -> {
            addQuanSo(listModel, list, name, updateCount);
        });

        // XOA QUAN SO
        remove.addActionListener(e -> {

            String person = list.getSelectedValue();

            if (person == null) {
                JOptionPane.showMessageDialog(
                        this, "Hay chon quan so can xoa.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xoa " + person + " va xep lai lich 7 ngay?",
                    "XAC NHAN",
                    JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION)
                return;

            store.people.remove(person);
            listModel.removeElement(person);

            // Xoa khoi danh sach vang cua cac ngay
            for (Set<String> absences : store.absences.values()) {
                absences.remove(person);
            }

            rebuildUpcomingSchedule();

            list.repaint();
            updateCount.run();
        });

        // CHO VANG
        absent.addActionListener(e -> {

            String person = list.getSelectedValue();

            if (person == null) {
                JOptionPane.showMessageDialog(
                        this, "Hay chon quan so.");
                return;
            }

            LocalDate date = (LocalDate) dateBox.getSelectedItem();

            if (date == null)
                return;

            Set<String> absences = store.absences.computeIfAbsent(
                    date, d -> new HashSet<>());

            if (!absences.add(person)) {
                JOptionPane.showMessageDialog(
                        this, "Quan so nay da vang.");
                return;
            }

            String[] slots = getDay(date);

            int absentIndex = store.people.indexOf(person);

            for (int s = 0; s < SHIFTS; s++) {

                if (person.equals(slots[s])) {

                    String replacement = null;

                    for (int step = 1; step <= store.people.size(); step++) {

                        String candidate = store.people.get(
                                (absentIndex + step)
                                        % store.people.size());

                        if (!absences.contains(candidate)) {
                            replacement = candidate;
                            break;
                        }
                    }

                    slots[s] = replacement;
                }
            }

            saveStore();
            refreshTable();

            list.repaint();
            updateCount.run();
        });

        // CO MAT TRO LAI
        present.addActionListener(e -> {

            String person = list.getSelectedValue();

            if (person == null) {
                JOptionPane.showMessageDialog(
                        this, "Hay chon quan so.");
                return;
            }

            LocalDate date = (LocalDate) dateBox.getSelectedItem();

            if (date == null)
                return;

            Set<String> absences = store.absences.get(date);

            if (absences == null || !absences.contains(person)) {
                JOptionPane.showMessageDialog(
                        this, "Quan so nay dang co mat.");
                return;
            }

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    person + " da co mat tro lai.\n"
                            + "Ban co muon xep lai toan bo 12 ca ngay "
                            + date + " khong?",
                    "XAC NHAN",
                    JOptionPane.YES_NO_OPTION);

            absences.remove(person);

            if (absences.isEmpty()) {
                store.absences.remove(date);
            }

            if (answer == JOptionPane.YES_OPTION) {
                autoAssignDay(date);
            } else {
                saveStore();
                refreshTable();
            }

            list.repaint();
            updateCount.run();
        });

        updateCount.run();

        JOptionPane.showMessageDialog(
                this,
                panel,
                "QUAN LY QUAN SO",
                JOptionPane.PLAIN_MESSAGE);

        refreshTable();
    }

    private void addQuanSo(DefaultListModel<String> listModel, JList<String> list, JTextField name,
            Runnable updateCount) {
        String n = name.getText().trim();

        if (n.isEmpty())
            return;

        if (store.people.contains(n)) {
            JOptionPane.showMessageDialog(
                    this, "Quan so nay da ton tai.");
            return;
        }

        store.people.add(n);
        listModel.addElement(n);

        name.setText("");

        rebuildUpcomingSchedule();

        list.repaint();
        updateCount.run();
    }

    // XEP LAI LICH 7 NGAY KHI DANH SACH QUAN SO THAY DOI
    private void rebuildUpcomingSchedule() {

        LocalDate today = LocalDate.now();

        // Dem so ca truc trong lich su truoc hom nay
        Map<String, Integer> totals = new HashMap<>();

        for (String person : store.people) {
            totals.put(person, 0);
        }

        for (Map.Entry<LocalDate, String[]> entry : store.schedules.entrySet()) {

            if (entry.getKey().isBefore(today)) {

                for (String person : entry.getValue()) {

                    if (person != null && totals.containsKey(person)) {
                        totals.put(
                                person,
                                totals.get(person) + 1);
                    }
                }
            }
        }

        // Xep lai tu hom nay den 6 ngay tiep theo
        for (int d = 0; d < 7; d++) {

            LocalDate date = today.plusDays(d);

            String[] slots = getDay(date);

            Set<String> absent = store.absences.getOrDefault(
                    date,
                    Collections.emptySet());

            List<String> eligible = new ArrayList<>();

            for (String person : store.people) {
                if (!absent.contains(person)) {
                    eligible.add(person);
                }
            }

            // Khong co ai du dieu kien truc
            if (eligible.isEmpty()) {
                Arrays.fill(slots, null);
                continue;
            }

            // Xoa lich cu cua ngay nay
            Arrays.fill(slots, null);

            String previous = null;

            for (int s = 0; s < SHIFTS; s++) {

                String selected = null;
                int minCount = Integer.MAX_VALUE;

                // Chon nguoi co so ca truc it nhat
                // Va tranh truc hai ca lien tiep
                for (String person : eligible) {

                    if (person.equals(previous)) {
                        continue;
                    }

                    int count = totals.get(person);

                    if (count < minCount) {
                        minCount = count;
                        selected = person;
                    }
                }

                // Neu khong tim duoc nguoi khac
                // thi cho phep truc lien tiep
                if (selected == null) {

                    for (String person : eligible) {

                        int count = totals.get(person);

                        if (count < minCount) {
                            minCount = count;
                            selected = person;
                        }
                    }
                }

                slots[s] = selected;

                totals.put(
                        selected,
                        totals.get(selected) + 1);

                previous = selected;
            }
        }

        saveStore();
        refreshTable();

        JOptionPane.showMessageDialog(
                this,
                "Danh sach quan so da thay doi.\n"
                        + "Da xep lai lich 7 ngay thanh cong.");
    }

    // XEP TU DONG
    private void autoSevenDays() {

        if (store.people.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Hay them quan so truoc.");
            return;
        }

        Map<String, Integer> totals = new HashMap<>();

        for (String p : store.people) {
            totals.put(p, 0);
        }

        for (String[] slots : store.schedules.values()) {

            for (String p : slots) {

                if (p != null && totals.containsKey(p)) {

                    totals.put(
                            p,
                            totals.get(p) + 1);
                }
            }
        }

        String previous = null;

        for (int d = 0; d < 7; d++) {

            LocalDate date = LocalDate.now().plusDays(d);

            String[] slots = getDay(date);

            Set<String> absent = store.absences.getOrDefault(
                    date,
                    Collections.emptySet());

            for (int s = 0; s < SHIFTS; s++) {

                if (slots[s] != null) {
                    previous = slots[s];
                    continue;
                }

                String chosen = null;

                int best = Integer.MAX_VALUE;

                for (int offset = 0; offset < store.people.size(); offset++) {

                    String candidate = store.people.get(
                            (s + d + offset)
                                    % store.people.size());

                    if (absent.contains(candidate)
                            || candidate.equals(previous)) {
                        continue;
                    }

                    int count = totals.getOrDefault(candidate, 0);

                    if (count < best) {
                        best = count;
                        chosen = candidate;
                    }
                }

                if (chosen == null) {

                    for (String candidate : store.people) {

                        if (!absent.contains(candidate)) {
                            chosen = candidate;
                            break;
                        }
                    }
                }

                if (chosen != null) {

                    slots[s] = chosen;

                    totals.put(
                            chosen,
                            totals.getOrDefault(chosen, 0) + 1);

                    previous = chosen;
                }
            }
        }

        saveStore();

        refreshTable();

        JOptionPane.showMessageDialog(
                this,
                "Da xep lich tu dong.");
    }

    // XEP THU CONG
    private void manualAssign() {

        int row = table.getSelectedRow();

        int col = table.getSelectedColumn();

        if (row < 0 || col <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Hay chon mot o trong bang.");
            return;
        }

        if (store.people.isEmpty())
            return;

        LocalDate date = dateForColumn(col);

        String[] slots = getDay(date);

        Set<String> absent = store.absences.getOrDefault(
                date,
                Collections.emptySet());

        List<String> eligible = new ArrayList<>();

        for (String p : store.people) {
            if (!absent.contains(p)) {
                eligible.add(p);
            }
        }

        if (eligible.isEmpty())
            return;

        JComboBox<String> combo = new JComboBox<>(
                eligible.toArray(new String[0]));

        if (slots[row] != null) {
            combo.setSelectedItem(slots[row]);
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                combo,
                "Chon nguoi truc",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {

            slots[row] = (String) combo.getSelectedItem();

            saveStore();

            refreshTable();
        }
    }

    // CHO VANG VA DON LEN
    private void markAbsent() {

        int col = table.getSelectedColumn();

        if (col <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Hay chon ngay can cho vang.");
            return;
        }

        LocalDate date = dateForColumn(col);

        if (store.people.isEmpty())
            return;

        String person = (String) JOptionPane.showInputDialog(
                this,
                "Chon nguoi vang:",
                "CHO VANG",
                JOptionPane.QUESTION_MESSAGE,
                null,
                store.people.toArray(),
                store.people.get(0));

        if (person == null)
            return;

        store.absences.computeIfAbsent(
                date,
                d -> new HashSet<>()).add(person);

        String[] slots = getDay(date);

        int absentIndex = store.people.indexOf(person);

        for (int s = 0; s < SHIFTS; s++) {

            if (person.equals(slots[s])) {

                String replacement = null;

                for (int step = 1; step <= store.people.size(); step++) {

                    String candidate = store.people.get(
                            (absentIndex + step)
                                    % store.people.size());

                    if (!store.absences.get(date)
                            .contains(candidate)) {

                        replacement = candidate;
                        break;
                    }
                }

                slots[s] = replacement;
            }
        }

        saveStore();

        refreshTable();

        JOptionPane.showMessageDialog(
                this,
                "Da cho vang va don nguoi thay.");
    }

    // DANH DAU NGUOI DA VANG CO MAT TRO LAI
    private void markPresent() {

        int col = table.getSelectedColumn();

        if (col <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Hay chon mot ngay tren bang lich truoc.");
            return;
        }

        LocalDate date = dateForColumn(col);

        Set<String> absent = store.absences.get(date);

        if (absent == null || absent.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ngay nay khong co nguoi nao dang vang.");
            return;
        }

        String[] absentPeople = absent.toArray(new String[0]);

        String person = (String) JOptionPane.showInputDialog(
                this,
                "Chon nguoi da co mat tro lai:",
                "CO MAT TRO LAI",
                JOptionPane.QUESTION_MESSAGE,
                null,
                absentPeople,
                absentPeople[0]);

        if (person == null) {
            return;
        }

        // Xoa nguoi nay khoi danh sach vang cua ngay da chon
        absent.remove(person);

        if (absent.isEmpty()) {
            store.absences.remove(date);
        }

        saveStore();
        refreshTable();

        int answer = JOptionPane.showConfirmDialog(
                this,
                person + " da duoc danh dau CO MAT.\n"
                        + "Ban co muon xep lai toan bo 12 ca cua ngay "
                        + date + " khong?",
                "XAC NHAN XEP LAI",
                JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION) {
            autoAssignDay(date);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    person + " da tro lai danh sach du dieu kien.\n"
                            + "Lich hien tai duoc giu nguyen.");
        }
    }

    // XEP LAI TOAN BO 12 CA CUA MOT NGAY
    private void autoAssignDay(LocalDate date) {

        List<String> eligible = new ArrayList<>();

        Set<String> absent = store.absences.getOrDefault(
                date, Collections.emptySet());

        for (String person : store.people) {
            if (!absent.contains(person)) {
                eligible.add(person);
            }
        }

        if (eligible.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Khong co quan so du dieu kien truc trong ngay nay.");
            return;
        }

        // Dem so ca da truc trong cac ngay truoc ngay dang xep
        Map<String, Integer> totals = new HashMap<>();

        for (String person : eligible) {
            totals.put(person, 0);
        }

        for (Map.Entry<LocalDate, String[]> entry : store.schedules.entrySet()) {

            if (!entry.getKey().isBefore(date)) {
                continue;
            }

            for (String person : entry.getValue()) {
                if (person != null && totals.containsKey(person)) {
                    totals.put(person, totals.get(person) + 1);
                }
            }
        }

        // Xoa lich cu va xep lai tu dau
        String[] slots = getDay(date);
        Arrays.fill(slots, null);

        String previous = null;

        for (int shift = 0; shift < SHIFTS; shift++) {

            String selected = null;
            int minCount = Integer.MAX_VALUE;

            // Uu tien nguoi co tong so ca it hon,
            // dong thoi tranh hai ca lien tiep neu co the.
            for (String person : eligible) {

                if (person.equals(previous)) {
                    continue;
                }

                int count = totals.get(person);

                if (count < minCount) {
                    minCount = count;
                    selected = person;
                }
            }

            // Neu chi con mot nguoi du dieu kien,
            // van xep de khong bo trong ca.
            if (selected == null) {
                for (String person : eligible) {
                    int count = totals.get(person);
                    if (count < minCount) {
                        minCount = count;
                        selected = person;
                    }
                }
            }

            slots[shift] = selected;
            totals.put(selected, totals.get(selected) + 1);
            previous = selected;
        }

        saveStore();
        refreshTable();

        JOptionPane.showMessageDialog(
                this,
                "Da xep lai thanh cong 12 ca ngay "
                        + date + ".\nNguoi vang da duoc loai khoi lich; "
                        + "nguoi co mat da duoc dua vao danh sach xep.");
    }

    // XEM LICH SU
    private void showHistory() {

        StringBuilder text = new StringBuilder();

        for (Map.Entry<LocalDate, String[]> entry : store.schedules.entrySet()) {

            if (entry.getKey().isBefore(LocalDate.now())) {

                text.append(entry.getKey()).append("\n");

                for (int s = 0; s < SHIFTS; s++) {

                    text.append(
                            "Ca " + (s + 1)
                                    + " - " + TIMES[s]
                                    + " : " + entry.getValue()[s]
                                    + "\n");
                }

                text.append("\n");
            }
        }

        JTextArea area = new JTextArea(
                text.toString(),
                25,
                70);

        area.setEditable(false);

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "LICH SU CAT GAC",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // XUAT FILE CSV
    private void exportCsv() {

        JFileChooser chooser = new JFileChooser();

        chooser.setSelectedFile(
                new File("lich_gac_7_ngay.csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();

        if (!path.toString().endsWith(".csv")) {
            path = Paths.get(path + ".csv");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8)) {

            writer.write("Ca,Thoi gian");

            for (int d = 0; d < 7; d++) {
                writer.write(
                        "," + LocalDate.now().plusDays(d));
            }

            writer.newLine();

            for (int s = 0; s < SHIFTS; s++) {

                writer.write(
                        (s + 1) + "," + TIMES[s]);

                for (int d = 0; d < 7; d++) {

                    String[] slots = store.schedules.get(
                            LocalDate.now().plusDays(d));

                    String value = slots == null || slots[s] == null
                            ? ""
                            : slots[s];

                    writer.write(
                            ",\"" + value + "\"");
                }

                writer.newLine();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Xuat file thanh cong!");

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Loi xuat file: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        // try {
        // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        SwingUtilities.invokeLater(() -> {

            new QuanLyCatGac().setVisible(true);

        });
    }
}
