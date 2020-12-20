import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run()
            {
                try {
                    new StartupWindow().setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
