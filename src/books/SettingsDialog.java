package books;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Диалог настройки путей: откуда брать книги (source) и куда перемещать
 * переименованные FB2 и TXT (можно указать разные папки, а можно
 * одну и ту же). Значения сохраняются в AppConfig -> books.properties.
 */
public class SettingsDialog extends JDialog {

    private final AppConfig config;
    private JTextField sourceField;
    private JTextField fb2DestField;
    private JTextField txtDestField;

    public SettingsDialog(Frame owner, AppConfig config) {
        super(owner, "Настройки путей", true);
        this.config = config;
        buildUi();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        sourceField = new JTextField(config.getSourceFolder(), 28);
        fb2DestField = new JTextField(config.getFb2DestFolder(), 28);
        txtDestField = new JTextField(config.getTxtDestFolder(), 28);

        addRow(fields, gbc, 0, "Папка с исходными книгами:", sourceField);
        addRow(fields, gbc, 1, "Куда перемещать FB2:", fb2DestField);
        addRow(fields, gbc, 2, "Куда перемещать TXT:", txtDestField);

        JButton saveButton = new JButton("Сохранить");
        JButton cancelButton = new JButton("Отмена");
        saveButton.addActionListener(e -> save());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        setLayout(new BorderLayout());
        add(fields, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseButton = new JButton("Обзор...");
        browseButton.addActionListener(e -> browseFor(field));
        panel.add(browseButton, gbc);
    }

    private void browseFor(JTextField field) {
        JFileChooser chooser = new JFileChooser(field.getText());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void save() {
        config.setSourceFolder(sourceField.getText().trim());
        config.setFb2DestFolder(fb2DestField.getText().trim());
        config.setTxtDestFolder(txtDestField.getText().trim());
        config.save();
        dispose();
    }
}
