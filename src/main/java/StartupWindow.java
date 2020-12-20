
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import javax.swing.*;


public class StartupWindow extends JFrame implements ActionListener //главное окно программы
{

    private final JButton start_button;
    private final JButton timer_button;

    public StartupWindow() throws IOException {
        setTitle("News catcher");
        setSize(400,300);
        setResizable(false);
        setLocationRelativeTo(null);
        URL iconURL = getClass().getResource("/newsIcon.png");
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        start_button = new JButton("Start");
        start_button.addActionListener(this);
        start_button.setActionCommand("Start");

        timer_button = new JButton("Set timer");
        timer_button.addActionListener(this);
        timer_button.setActionCommand("Set");

        add(start_button);
        add(timer_button);

    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();

        if (cmd.equals("Start")){ //создание окна "break_window"
            dispose();
            ExecutorService pool = Executors.newFixedThreadPool(2);
            pool.execute(() -> {
                new Break_Window();
                try {
                    Parser parser = new Parser();
                    parser.primFill();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            });
            pool.shutdown();
        }
        if(cmd.equals("Set")) //создание окна "timer_window"
        {
            dispose();
            new Timer_Window();
        }
    }

}

