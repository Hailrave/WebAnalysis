import javax.swing.*;
import java.io.IOException;

/**
 * Главный класс программы
 */
public class Main {
    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run()
            {
                try {
                    /**
                     * создание главного окна программы
                     * @see StartupWindow#StartupWindow()
                     */
                    new StartupWindow().setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
