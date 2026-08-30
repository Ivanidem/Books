package books;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/**
 * Главное окно программы.
 *
 * ВАЖНО: этот класс больше не сгенерирован NetBeans GUI Builder-ом.
 * Файл NewJFrame.form из старой версии проекта для этого окна больше
 * не используется - разметка теперь написана вручную (BoxLayout/GridLayout),
 * чтобы было проще добавлять новые кнопки и не бояться сломать
 * автогенерируемый initComponents(). Если откроете этот класс в дизайнере
 * NetBeans - он не будет виден как форма, только как обычный код, и это
 * нормально: просто продолжайте редактировать его как код.
 */
public class NewJFrame extends JFrame {

    private final AppConfig config = new AppConfig();
    private final BookRenamer renamer = new BookRenamer();

    public NewJFrame() {
        super("Установка названий книг");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        buildUi();
        pack();
    }

    private void buildUi() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildFb2Panel());
        root.add(Box.createVerticalStrut(10));
        root.add(buildInfoLabel());
        root.add(Box.createVerticalStrut(10));
        root.add(buildTxtPanel());
        root.add(Box.createVerticalStrut(10));
        root.add(buildMovePanel());
        root.add(Box.createVerticalStrut(6));
        root.add(buildSettingsPanel());

        setContentPane(root);
    }

    private JPanel buildFb2Panel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(204, 255, 204));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Переименовать книги FB2", SwingConstants.CENTER);
        title.setFont(new Font("Times New Roman", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton onlyRussian = createActionButton("Только русские буквы", 20,
                e -> renameAndReport(BookRenamer.Mode.FB2_RUSSIAN_ONLY));
        JButton russianAndEnglish = createActionButton("Русские и английские буквы", 20,
                e -> renameAndReport(BookRenamer.Mode.FB2_RUSSIAN_AND_ENGLISH));
        JButton withNickname = createActionButton("+ Псевдоним", 20,
                e -> renameAndReport(BookRenamer.Mode.FB2_WITH_NICKNAME));

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(onlyRussian);
        panel.add(Box.createVerticalStrut(4));
        panel.add(russianAndEnglish);
        panel.add(Box.createVerticalStrut(4));
        panel.add(withNickname);
        return panel;
    }

    private JPanel buildTxtPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(204, 255, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createBevelBorder(BevelBorder.RAISED),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Переименовать книги TXT", SwingConstants.CENTER);
        title.setFont(new Font("Times New Roman", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton standard = createActionButton("Стандартное переименование", 16,
                e -> renameAndReport(BookRenamer.Mode.TXT_STANDARD));
        JButton alternative = createActionButton(
                "<html><p align=\"center\">Альтернативное<br>"
                        + "(если в названиях файлов кракозябры или чёрные ромбы с вопросами)</p></html>",
                14, e -> renameAndReport(BookRenamer.Mode.TXT_ALTERNATIVE));

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(standard);
        panel.add(Box.createVerticalStrut(4));
        panel.add(alternative);
        return panel;
    }

    private JLabel buildInfoLabel() {
        JLabel label = new JLabel("<html><p>Программа переименовывает ВСЕ файлы (FB2 или TXT) "
                + "в папке-источнике на основании данных в самом файле, которых может "
                + "быть излишне много или недостаточно.</p></html>");
        label.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        return label;
    }

    private JPanel buildMovePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 6));

        JButton moveAll = new JButton("<html><p align=\"center\">Переместить ВСЕ книги "
                + "(FB2 и TXT) в папку с книгами</p></html>");
        moveAll.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        moveAll.setBackground(new Color(249, 255, 0));
        moveAll.setFocusable(false);
        moveAll.addActionListener(e -> moveAllFiles());

        JButton moveSome = new JButton("Переместить некоторые книги...");
        moveSome.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        moveSome.setFocusable(false);
        moveSome.addActionListener(e -> new SelectFilesDialog(this, config).setVisible(true));

        panel.add(moveAll);
        panel.add(moveSome);
        return panel;
    }

    private JPanel buildSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton settings = new JButton("Настройки путей...");
        settings.setFocusable(false);
        settings.addActionListener(e -> new SettingsDialog(this, config).setVisible(true));
        panel.add(settings);
        return panel;
    }

    private JButton createActionButton(String text, int fontSize, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Times New Roman", Font.BOLD, fontSize));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusable(false);
        button.addActionListener(listener);
        return button;
    }

    private void renameAndReport(BookRenamer.Mode mode) {
        try {
            List<RenamedFile> renamed = renamer.renameAll(config.getSourceFolder(), mode);
            JOptionPane.showMessageDialog(this, "Обработано файлов: " + renamed.size());
        } catch (IOException ex) {
            Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moveAllFiles() {
        try {
            List<RenamedFile> fb2Files = renamer.listFiles(config.getSourceFolder(), ".fb2");
            List<RenamedFile> txtFiles = renamer.listFiles(config.getSourceFolder(), ".txt");
            renamer.moveFiles(fb2Files, config.getFb2DestFolder());
            renamer.moveFiles(txtFiles, config.getTxtDestFolder());
            JOptionPane.showMessageDialog(this,
                    "Перемещено файлов: " + (fb2Files.size() + txtFiles.size()));
        } catch (IOException ex) {
            Logger.getLogger(NewJFrame.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
