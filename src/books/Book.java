package books;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Вытаскивает из одного файла книги (FB2 или TXT) данные, из которых
 * строится новое имя файла в формате "Автор - Название".
 *
 * FB2 - это XML, поэтому имя/фамилия/псевдоним авторов и название книги
 * ищутся по тегам "first-name", "last-name", "nickname" и "book-title".
 * У TXT такой структуры нет, поэтому для него используется отдельное
 * эмпирическое правило: считается, что название книги - это первая строка
 * файла, а имя автора - вторая (так были устроены файлы, с которыми
 * изначально работала программа; если у вас другой формат TXT - этот
 * метод придётся подстроить под него).
 */
public class Book {

    /** Папка, в которой лежит файл (по умолчанию - папка запуска программы). */
    public String folder = System.getProperty("user.dir");

    /** Имя файла книги внутри folder, например "12345.fb2". */
    public String fileName;

    /** Итоговое предложенное имя файла (без расширения), например "Толстой Лев - Война и мир". */
    public String bookName;

    /** Добавлять псевдоним автора, если имя/фамилия не найдены или не на русском. */
    public boolean useNickname = false;

    /** Оставлять имя/фамилию, даже если они на латинице (не только русские буквы). */
    public boolean addEnglishNames = false;

    /** TXT-файл в кодировке windows-1251 вместо UTF-8. */
    public boolean useWindows1251 = false;

    /**
     * Разбирает FB2 (XML) файл и заполняет {@link #bookName} строкой вида
     * "Автор1, Автор2 - Название книги".
     */
    public void parseFb2() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // Защита от XXE-атак при разборе непроверенного XML.
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(folder, fileName));
            doc.getDocumentElement().normalize();

            bookName = buildAuthorsAndTitle(doc);

            System.out.println(fileName);
            System.out.println(bookName);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private String buildAuthorsAndTitle(Document doc) {
        List<String> firstNames = extractTagValues("first-name", doc);
        List<String> lastNames = extractTagValues("last-name", doc);
        List<String> nicknames = extractTagValues("nickname", doc);

        String authors = buildAuthorsList(firstNames, lastNames, nicknames);
        String title = extractBookTitle(doc);

        if (authors.isEmpty()) {
            return title;
        }
        if (title.isEmpty()) {
            return authors;
        }
        return authors + " - " + title;
    }

    /**
     * Собирает список авторов в виде "Фамилия Имя, Фамилия Имя, ...".
     * Если у автора нет ни имени, ни фамилии (или они отфильтрованы, так как
     * не на русском, а addEnglishNames = false), но есть псевдоним и
     * useNickname = true - используется псевдоним. Если и псевдонима нет -
     * автор просто пропускается (а не обрывает весь список, как было раньше).
     */
    private String buildAuthorsList(List<String> firstNames, List<String> lastNames, List<String> nicknames) {
        int authorCount = Math.max(firstNames.size(), lastNames.size());
        List<String> fullNames = new ArrayList<>();

        for (int i = 0; i < authorCount; i++) {
            String first = valueAt(firstNames, i);
            String last = valueAt(lastNames, i);
            String nickname = valueAt(nicknames, i);

            if (!addEnglishNames) {
                if (containsLatinLetters(first)) first = "";
                if (containsLatinLetters(last)) last = "";
            }

            boolean noNameInfo = first.isEmpty() && last.isEmpty();

            if (noNameInfo && useNickname && !nickname.isEmpty()) {
                fullNames.add(nickname.trim());
                continue;
            }
            if (noNameInfo) {
                continue;
            }
            fullNames.add((last + " " + first).trim());
        }

        // убрать дубликаты (например, один и тот же автор указан в нескольких <author>),
        // сохраняя порядок появления
        Set<String> unique = new LinkedHashSet<>(fullNames);
        return String.join(", ", unique);
    }

    private String extractBookTitle(Document doc) {
        NodeList titles = doc.getElementsByTagName("book-title");
        if (titles.getLength() == 0) {
            return "";
        }
        return cleanTitle(titles.item(0).getTextContent());
    }

    private static String valueAt(List<String> values, int index) {
        if (index >= values.size() || values.get(index) == null) {
            return "";
        }
        return values.get(index);
    }

    private static List<String> extractTagValues(String tagName, Document doc) {
        List<String> values = new ArrayList<>();
        NodeList nodes = doc.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            String text = nodes.item(i).getTextContent();
            values.add(text == null ? "" : text);
        }
        return values;
    }

    private static boolean containsLatinLetters(String value) {
        return value.matches(".*[a-zA-Z].*");
    }

    private static String cleanTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.replace("\"", "")
                .replace(":", " - ")
                .replace("?", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * "Разбирает" TXT файл: первая строка считается названием книги,
     * вторая - именем автора. Заполняет {@link #bookName}.
     */
    public void parseTxt() {
        File source = new File(folder, fileName);
        Charset charset = useWindows1251 ? Charset.forName("windows-1251") : StandardCharsets.UTF_8;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(source), charset))) {

            String title = reader.readLine();
            String author = reader.readLine();

            title = cleanTitle(title == null ? "" : title);
            author = author == null ? "" : author.trim();

            bookName = author.isEmpty() ? title : (author + " - " + title);

            System.out.println(fileName);
            System.out.println(bookName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
