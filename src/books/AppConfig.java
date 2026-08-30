package books;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Настройки путей программы: откуда брать книги и куда их перемещать
 * после переименования (отдельно для FB2 и TXT, так как раньше это были
 * два разных файла).
 *
 * Раньше пути хранились в двух скрытых файлах рядом с программой:
 * ".fb2pathto" и ".txtpathto" (в каждом по одной строке с путём).
 * Найти и понять эти файлы со стороны было тяжело, а менять путь можно
 * было только вручную открыв файл в блокноте.
 *
 * Теперь всё хранится в одном человекочитаемом файле "books.properties"
 * рядом с программой и редактируется через SettingsDialog. Если файла нет
 * (первый запуск) - используются значения по умолчанию ниже, а сам файл
 * создастся при первом сохранении настроек.
 */
public class AppConfig {

    private static final String CONFIG_FILE_NAME = "books.properties";

    private static final String KEY_SOURCE_FOLDER = "source.folder";
    private static final String KEY_FB2_DEST_FOLDER = "fb2.dest.folder";
    private static final String KEY_TXT_DEST_FOLDER = "txt.dest.folder";

    private final Path configPath;

    private String sourceFolder;
    private String fb2DestFolder;
    private String txtDestFolder;

    public AppConfig() {
        this.configPath = Paths.get(System.getProperty("user.dir"), CONFIG_FILE_NAME);
        loadWithDefaults();
    }

    private void loadWithDefaults() {
        String currentDir = System.getProperty("user.dir");

        // Значения по умолчанию на случай первого запуска (файла настроек ещё нет):
        // книги по умолчанию лежат и перемещаются в подпапки текущей папки.
        // Замените их на что вам удобно через кнопку "Настройки путей..." в программе.
        sourceFolder = currentDir;
        fb2DestFolder = currentDir + File.separator + "organized";
        txtDestFolder = currentDir + File.separator + "organized";

        if (!Files.exists(configPath)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            properties.load(in);
            sourceFolder = properties.getProperty(KEY_SOURCE_FOLDER, sourceFolder);
            fb2DestFolder = properties.getProperty(KEY_FB2_DEST_FOLDER, fb2DestFolder);
            txtDestFolder = properties.getProperty(KEY_TXT_DEST_FOLDER, txtDestFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        Properties properties = new Properties();
        properties.setProperty(KEY_SOURCE_FOLDER, sourceFolder);
        properties.setProperty(KEY_FB2_DEST_FOLDER, fb2DestFolder);
        properties.setProperty(KEY_TXT_DEST_FOLDER, txtDestFolder);
        try (OutputStream out = Files.newOutputStream(configPath)) {
            properties.store(out, "Настройки путей программы Books");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public String getFb2DestFolder() {
        return fb2DestFolder;
    }

    public void setFb2DestFolder(String fb2DestFolder) {
        this.fb2DestFolder = fb2DestFolder;
    }

    public String getTxtDestFolder() {
        return txtDestFolder;
    }

    public void setTxtDestFolder(String txtDestFolder) {
        this.txtDestFolder = txtDestFolder;
    }
}
