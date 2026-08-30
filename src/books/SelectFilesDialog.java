package books;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Окно "Переместить некоторые книги": показывает все FB2 и TXT файлы
 * из папки-источника с чекбоксами, позволяет отметить нужные и переместить
 * только их - в отличие от кнопки "Переместить ВСЕ", которая перемещает
 * действительно все файлы подряд.
 *
 * Строки, в имени файла которых нет " - " (то есть не похоже на
 * "Автор - Название"), подсвечиваются - обычно это признак того, что
 * автора или название не удалось вытащить из файла, и книгу стоит
 * проверить перед перемещением.
 */
public class SelectFilesDialog extends JDialog {

    private final AppConfig config;
    private final BookRenamer renamer = new BookRenamer();
    private final List<Row> rows = new ArrayList<>();
    private RowTableModel tableModel;

    private static final class Row {
        final RenamedFile renamedFile;
        boolean selected;

        Row(RenamedFile renamedFile, boolean selected) {
            this.renamedFile = renamedFile;
            this.selected = selected;
        }
    }

    public SelectFilesDialog(Frame owner, AppConfig config) {
        super(owner, "Выбор книг для перемещения", true);
        this.config = config;
        loadFiles();
        buildUi();
        setSize(620, 480);
        setLocationRelativeTo(owner);
    }

    private void loadFiles() {
        for (RenamedFile f : renamer.listFiles(config.getSourceFolder(), ".fb2")) {
            rows.add(new Row(f, true));
        }
        for (RenamedFile f : renamer.listFiles(config.getSourceFolder(), ".txt")) {
            rows.add(new Row(f, true));
        }
    }

    private void buildUi() {
        setLayout(new BorderLayout());

        tableModel = new RowTableModel();
        JTable table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setMaxWidth(60);
        table.setDefaultRenderer(Object.class, new SuspiciousNameRenderer());
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton selectAll = new JButton("Выбрать все");
        JButton deselectAll = new JButton("Снять все");
        selectAll.addActionListener(e -> setAllSelected(true));
        deselectAll.addActionListener(e -> setAllSelected(false));
        top.add(selectAll);
        top.add(deselectAll);
        JLabel hint = new JLabel("Подсвеченные строки — вероятно, переименовались неудачно");
        top.add(hint);
        add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton moveButton = new JButton("Переместить выбранные");
        JButton cancelButton = new JButton("Отмена");
        moveButton.addActionListener(e -> moveSelected());
        cancelButton.addActionListener(e -> dispose());
        bottom.add(cancelButton);
        bottom.add(moveButton);
        add(bottom, BorderLayout.SOUTH);
    }

    private void setAllSelected(boolean value) {
        for (Row r : rows) {
            r.selected = value;
        }
        tableModel.fireTableDataChanged();
    }

    private void moveSelected() {
        List<RenamedFile> fb2ToMove = new ArrayList<>();
        List<RenamedFile> txtToMove = new ArrayList<>();
        for (Row r : rows) {
            if (!r.selected) {
                continue;
            }
            if (".fb2".equalsIgnoreCase(r.renamedFile.getExtension())) {
                fb2ToMove.add(r.renamedFile);
            } else {
                txtToMove.add(r.renamedFile);
            }
        }

        if (fb2ToMove.isEmpty() && txtToMove.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ничего не выбрано.");
            return;
        }

        try {
            if (!fb2ToMove.isEmpty()) {
                renamer.moveFiles(fb2ToMove, config.getFb2DestFolder());
            }
            if (!txtToMove.isEmpty()) {
                renamer.moveFiles(txtToMove, config.getTxtDestFolder());
            }
            JOptionPane.showMessageDialog(this,
                    "Перемещено файлов: " + (fb2ToMove.size() + txtToMove.size()));
            dispose();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при перемещении: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private final class RowTableModel extends AbstractTableModel {
        private final String[] columns = {"", "Файл", "Тип"};

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Row r = rows.get(row);
            switch (col) {
                case 0:
                    return r.selected;
                case 1:
                    return r.renamedFile.getName();
                default:
                    return r.renamedFile.getExtension();
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0) {
                rows.get(row).selected = (Boolean) value;
                fireTableCellUpdated(row, col);
            }
        }
    }

    private final class SuspiciousNameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            boolean suspicious = !rows.get(row).renamedFile.getName().contains(" - ");
            if (!isSelected) {
                c.setBackground(suspicious ? new Color(255, 224, 178) : Color.WHITE);
            }
            return c;
        }
    }
}
