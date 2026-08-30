package books;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Переименовывает книги (FB2/TXT) в папке-источнике на основании их
 * метаданных и перемещает переименованные файлы в папку назначения.
 *
 * Раньше эта логика была в Main.my_main() и делала переименование и
 * перемещение сразу для "всех" файлов без возможности выбрать часть.
 * Теперь переименование (renameAll) и перемещение (moveFiles) - два
 * независимых шага: сначала переименовали и посмотрели на результат,
 * потом отдельно решили, что из этого перемещать (см. SelectFilesDialog).
 */
public class BookRenamer {

    /** Режим переименования - соответствует кнопкам в главном окне. */
    public enum Mode {
        FB2_RUSSIAN_ONLY,
        FB2_RUSSIAN_AND_ENGLISH,
        FB2_WITH_NICKNAME,
        TXT_STANDARD,
        TXT_ALTERNATIVE
    }

    /**
     * Переименовывает все файлы нужного расширения в folder согласно mode.
     * Возвращает список файлов после переименования (для отображения
     * пользователю и последующего выборочного перемещения).
     */
    public List<RenamedFile> renameAll(String folder, Mode mode) throws IOException {
        String extension = isTxtMode(mode) ? ".txt" : ".fb2";
        List<RenamedFile> result = new ArrayList<>();

        for (File file : filesWithExtension(folder, extension)) {
            Book book = new Book();
            book.folder = folder;
            book.fileName = file.getName();
            applyMode(book, mode);

            File renamedFile = renameSingleFile(file, folder, book.bookName, extension);
            result.add(new RenamedFile(renamedFile, extension));
        }
        return result;
    }

    private void applyMode(Book book, Mode mode) {
        switch (mode) {
            case FB2_RUSSIAN_AND_ENGLISH:
                book.addEnglishNames = true;
                book.parseFb2();
                break;
            case FB2_WITH_NICKNAME:
                book.addEnglishNames = true;
                book.useNickname = true;
                book.parseFb2();
                break;
            case TXT_STANDARD:
                book.parseTxt();
                break;
            case TXT_ALTERNATIVE:
                book.useWindows1251 = true;
                book.parseTxt();
                break;
            case FB2_RUSSIAN_ONLY:
            default:
                book.parseFb2();
                break;
        }
    }

    private File renameSingleFile(File file, String folder, String proposedName, String extension) {
        String safeName = sanitizeFileName(proposedName);
        File newFile = new File(folder, safeName + extension);

        // если книга с таким именем уже есть - не перезаписываем её молча,
        // а добавляем номер, чтобы не потерять файл
        int counter = 1;
        while (newFile.exists() && !newFile.equals(file)) {
            newFile = new File(folder, safeName + " (" + counter + ")" + extension);
            counter++;
        }

        if (file.renameTo(newFile)) {
            System.out.println("Переименован: " + file.getName() + " -> " + newFile.getName());
            return newFile;
        } else {
            System.out.println("НЕ удалось переименовать: " + file.getName());
            return file;
        }
    }

    /** Перемещает уже переименованные файлы в папку назначения (создаёт её при необходимости). */
    public void moveFiles(List<RenamedFile> files, String destinationFolder) throws IOException {
        File destDir = new File(destinationFolder);
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new IOException("Не удалось создать папку назначения: " + destinationFolder);
        }
        for (RenamedFile renamedFile : files) {
            File src = renamedFile.getFile();
            File dest = new File(destDir, src.getName());
            Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Перемещён: " + src.getName());
        }
    }

    /** Список файлов заданного расширения в папке (без переименования) - для кнопки "Переместить ВСЕ". */
    public List<RenamedFile> listFiles(String folder, String extension) {
        List<RenamedFile> result = new ArrayList<>();
        for (File file : filesWithExtension(folder, extension)) {
            result.add(new RenamedFile(file, extension));
        }
        return result;
    }

    private List<File> filesWithExtension(String folder, String extension) {
        List<File> result = new ArrayList<>();
        File[] files = new File(folder).listFiles();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            if (file.isFile() && extension.equalsIgnoreCase(getExtension(file.getName()))) {
                result.add(file);
            }
        }
        return result;
    }

    private static boolean isTxtMode(Mode mode) {
        return mode == Mode.TXT_STANDARD || mode == Mode.TXT_ALTERNATIVE;
    }

    private static String getExtension(String name) {
        int index = name.lastIndexOf('.');
        return index == -1 ? "" : name.substring(index);
    }

    /** Убирает из предложенного имени файла символы, запрещённые в Windows: \ / : * ? " < > | */
    private static String sanitizeFileName(String name) {
        if (name == null || name.isEmpty()) {
            return "unknown";
        }
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", " ").trim();
        return cleaned.isEmpty() ? "unknown" : cleaned;
    }
}
