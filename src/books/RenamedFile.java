package books;

import java.io.File;

/**
 * Файл книги на диске (после переименования) + его расширение (".fb2" или ".txt").
 * Используется и при перемещении "всех" книг (BookRenamer.listFiles),
 * и при выборочном перемещении через SelectFilesDialog.
 */
public class RenamedFile {

    private final File file;
    private final String extension;

    public RenamedFile(File file, String extension) {
        this.file = file;
        this.extension = extension;
    }

    public File getFile() {
        return file;
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return file.getName();
    }
}
