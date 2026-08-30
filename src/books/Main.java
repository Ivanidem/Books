
package books;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;


public class Main {
    String ext = ".fb2";
    Boolean fb2 = false;
    Boolean fb2_eng = false;
    Boolean fb2_nikname = false;
    Boolean txt = false;
    Boolean txt_win1251 = false;
    
    public void my_main() throws IOException {
        //ArrayList<String> file_names = new ArrayList<String>();
        //ArrayList<String> new_file_names = new ArrayList<String>();
        
        String my_folder = System.getProperty("user.dir") + "\\"; //"F:\\books\\test\\";       
        File folder = new File(my_folder); //
        File[] files = folder.listFiles();
        int i = 0;
        for (File file : files) {     
            if (file.isFile() && getFileExtension(file.getName()).equals(ext)) {
                //file_names.add(file.getName());
                File new_name = new File("");
                if (fb2){
                    Book book = new Book();
                    book.file_name = file.getName();
                    if (fb2_eng){
                        book.add_eng = true;
                    }
                    if (fb2_nikname){
                        book.add_eng = true;
                        book.nickname_bool = true;
                    }
                    book.take_book();
                    new_name = new File(my_folder + book.book_name + ext);
                }
                
                if (txt) {
                    Book book_txt = new Book();
                    book_txt.file_name = file.getName();
                    if (txt_win1251){
                        book_txt.sharset_win1251 = true;
                    }
                    book_txt.txt();
                    new_name = new File(my_folder + book_txt.book_name + ext);
                }
                //book.sharset_win1251 = false;
                //book.txt();
                i++;
                //rename
                //new_name = new File(my_folder + book.book_name + ext);
                if (file.renameTo(new_name)) {
                    System.out.println("Файл переименован успешно+++++++++++++++++");//book.file_name + 
                } else {
                    System.out.println("Файл не был переименован--------------");//book.file_name + 
                }
                //rename
            }
        }
        System.out.println(i);   
    }
    
    public void move_files(String extension, String file_info) throws FileNotFoundException{
        //File f_info_to = new File(System.getProperty("user.dir") + "\\" + file_info);
        
        Scanner input = new Scanner(new File(System.getProperty("user.dir") + "\\" + file_info));
        String path_to = input.nextLine();
        
        String my_folder = System.getProperty("user.dir") + "\\"; //"F:\\books\\test\\";       
        File folder = new File(my_folder); //
        File[] files = folder.listFiles();
        int i = 0;
        for (File file : files) {     
            if (file.isFile() && getFileExtension(file.getName()).equals(extension)) {
                File to = new File(path_to + file.getName());
                try {
                    moveFile(file, to);
                    System.out.println("File moved successfully.");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
     public static void moveFile(File src, File dest) throws IOException {
        Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
     
    private static String getFileExtension(String mystr) {
        int index = mystr.lastIndexOf(".");
        return index == -1? null : mystr.substring(index);
    }
}
