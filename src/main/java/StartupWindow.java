
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

/**
 * класс - главное окно программы
 */
public class StartupWindow extends JFrame implements ActionListener //главное окно программы
{
    /**
     * start_button - поле кнопка Start
     * timer_button - поле кнопка Set Timer
     */
    private final JButton start_button;
    private final JButton timer_button;

    /**
     * Конструктор - создание нового объекта
     * @throws IOException
     */
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

    /**
     * функция что происходит после нажатия одной из кнопок
     * @param e - нажатие кнопки
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        /**
         * первичное наполнение
         * @see Break_Window#Break_Window()
         * @see Parser#Parser()
         * @see Parser#primFill()
         * @see End_Window#End_Window()
         */
        if (cmd.equals("Start")){ //если нажата Start, начать первичное наполнение
            dispose();
            ExecutorService pool = Executors.newFixedThreadPool(2); //созд.потоки
            Break_Window breakW = new Break_Window(); //создание окна "break_window"
            pool.execute(() -> {
                breakW.setVisible(true);
                try {
                    Parser parser = new Parser();
                    parser.primFill(); //метод первичного наполнения
                    breakW.dispose();
                    new End_Window(); //окошко закрытия программы после завершения работы парсера
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            });
            pool.shutdown();

        }

        /**
         * создание окна "timer_window"
         * @see Timer_Window#Timer_Window()
         */
        if(cmd.equals("Set")) //если нажата Set timer, создание окна "timer_window"
        {
            dispose();
            new Timer_Window();
        }
    }

}

