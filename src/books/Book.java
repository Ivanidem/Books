package books;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Book {

    String file_name;
    String book_name;
    Boolean nickname_bool = false;
    Boolean add_eng = false;
    Boolean sharset_win1251 = false;

    public void take_book() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(System.getProperty("user.dir") + "\\" + file_name));//"F:\\books\\test\\"
            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            ArrayList<String> full_names = new ArrayList<String>();
            String full_name = "";
            String[] first_name = author_name("first-name", doc);
            String[] nickname = author_name("nickname", doc);
            String[] last_name = author_name("last-name", doc);

            int num_names = 0;
            if (first_name.length >= last_name.length) {
                num_names = first_name.length;
            }
            if (first_name.length <= last_name.length) {
                num_names = last_name.length;
            }
            for (int i = 0; i < num_names; i++) { 

                if (first_name[i] == null) first_name[i] = "";
                if (last_name[i] == null) last_name[i] = "";
                
                //on/off eng
                if (!add_eng){
                    boolean onlyLatinAlphabet_first = first_name[i].matches("^.+[a-zA-Z0-9]+.+$");
                    boolean onlyLatinAlphabet_last = last_name[i].matches("^.+[a-zA-Z0-9]+.+$");
                    if (onlyLatinAlphabet_first) first_name[i] = "";
                    if (onlyLatinAlphabet_last) last_name[i] = "";
                }

                //nickname add
                if (nickname_bool) 
                    if (first_name[i] == "" && last_name[i] == "" && nickname[i]!="") 
                        full_names.add(" " + nickname[i] + " ");
                
                if (first_name[i] == "" && last_name[i] == "") {
                    break;
                }
                full_names.add(last_name[i] + " " + first_name[i]);
            }

            //удалить дубликаты
            if (full_names.size() > 1) {
                Set<String> set = new HashSet<>(full_names);
                full_names.clear();
                full_names.addAll(set);
                //Collections.sort(full_names);
            }
             
            for (int i = 0; i < full_names.size(); i++) {
                //if (full_names[i]=="" || full_names[i]==null) break;
                if (full_names.size() > 1 && full_names.size() > (i + 1)) {
                    full_name += full_names.get(i) + ", ";
                } else if (full_names.size() == (i + 1) || full_names.size() == 1) {
                    full_name += full_names.get(i);
                }
            }

            //System.out.println(authors.getLength());
            String booktitle = "";
            if (doc.getElementsByTagName("book-title").getLength() != 0) {
                booktitle = doc.getElementsByTagName("book-title").item(0).getTextContent();
                booktitle = booktitle.replaceAll("\"", "");
                booktitle = booktitle.replaceAll(":", " - ");
                booktitle = booktitle.replaceAll("\\?", "");
                //check "" and delete them or replace on ()
                //System.out.println(booktitle);
            }

            String full_title = full_name + " - " + booktitle;
            book_name = full_title;
            System.out.println(file_name);
            System.out.println(book_name);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            //delete_word();
            //System.out.println(file_name);
        }
    }

    public static String[] author_name(String name_tag, Document doc1) {
        String[] name = new String[20];
        NodeList tag_elements = doc1.getElementsByTagName(name_tag);

        if (tag_elements.getLength() > 1) {
            for (int temp = 0; temp < tag_elements.getLength(); temp++) {
                name[temp] = tag_elements.item(temp).getTextContent();
            }
        } else if (tag_elements.getLength() == 1) {
            name[0] = tag_elements.item(0).getTextContent();
        }
        return name;
    }

    public void txt() {

        try {

            //BufferedReader fileReader = new BufferedReader(new FileReader(new File("F:\\books\\test\\" + file_name), Charset.forName("Windows-1251")));
            String fileName = System.getProperty("user.dir") + "\\" + file_name;
            String fileNameOut = System.getProperty("user.dir") + "\\" + ".temp";

            FileInputStream is = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            if (sharset_win1251)
                isr = new InputStreamReader(is, "windows-1251"); //"windows-1251" Charset.defaultCharset()
            BufferedReader buffReader = new BufferedReader(isr);
            
            FileOutputStream fos = new FileOutputStream(fileNameOut);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            String booktitle = "";
            String name = "";
            int i = 0;
            while (buffReader.ready()) {
                String s = buffReader.readLine();
                if (i==0) booktitle = s;
                if (i==1) name = s;
                bufferedWriter.write(s + "\n");
                i++;
            }
            bufferedWriter.close();
            outputStreamWriter.close();
            is.close();
            fos.close();

            File file = new File(System.getProperty("user.dir") + "\\", ".temp");
            if (file.exists()) {
                file.delete();
            }
            
            /*
            //Path path = Paths.get("F:\\books\\txt\\" + file_name);
            // задаем стандартную кодировку UTF_8
            //String text = Files.readString(path, StandardCharsets.UTF_8);
            //Files.lines(Paths.get("F:\\books\\txt\\" + file_name), Charset.forName("windows-1251"));
            String booktitle = "";
            //File file1 = new File("F:\\books\\txt\\" + file_name);
            //BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file1), "UTF8"));
            File file = new File("F:\\books\\test\\" + file_name);
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);//new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));//
            String line = reader.readLine();
            booktitle = line;
            //booktitle = booktitle.replaceAll("\"", "");
            //booktitle = booktitle.replaceAll(":", " - ");
            //booktitle = booktitle.replaceAll("\\?", "");
            String name = reader.readLine();
            */
            booktitle = booktitle.replaceAll("\"", "");
            booktitle = booktitle.replaceAll(":", " - ");
            booktitle = booktitle.replaceAll("\\?", "");
            
            String full_title = name + " - " + booktitle;
            book_name = full_title;

            System.out.println(file_name);
            System.out.println(book_name);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete_word() {
        try {

            File file = new File("F:\\books\\" + file_name);
            File temp = File.createTempFile("file", ".fb2", file.getParentFile());
            String charset = "UTF-8";
            String delete = "<document-info>";
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(temp), charset));
            for (String line; (line = reader.readLine()) != null;) {
                line = line.replace(delete, "");
                writer.println(line);
            }
            reader.close();
            writer.close();
            file.delete();
            temp.renameTo(file);

            System.out.println("asd");

        } catch (IOException e) { //ParserConfigurationException | SAXException |
            e.printStackTrace();
        }
    }
}
