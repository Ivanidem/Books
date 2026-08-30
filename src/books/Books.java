package books;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
//import java.util.ArrayList;

public class Books {

        public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
   {
       //JFrame qwer = new JFrame(){};
        //qwer.setVisible(true);
       //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // изменяем окно на стиль Windows
       //JFrame.setDefaultLookAndFeelDecorated(true);
       javax.swing.SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               new NewJFrame().setVisible(true);
           }
       });
   }    
}
