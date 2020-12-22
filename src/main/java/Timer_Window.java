import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * класс ввод таймера и начало наблюдения
 */
public class Timer_Window extends JFrame implements ActionListener //ввод таймера и начало наблюдения
{
    /**
     * start_timer_button - поле кнопка Start
     * label - поле с текстом
     * enter_timer - поле для ввода текста
     */
    private final JButton start_timer_button;
    private final JLabel label;
    private final JTextField enter_timer;

    /**
     * конструктор
     */
    public Timer_Window()
    {
        setTitle("News catcher");
        setSize(500,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        URL iconURL = getClass().getResource("/newsIcon.png");
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        label = new JLabel("Enter time from 1 to 240 minutes:  ");
        enter_timer = new JTextField(10);
        add(label);
        add(enter_timer);

        start_timer_button = new JButton("Start");
        start_timer_button.addActionListener(this);
        start_timer_button.setActionCommand("Start");
        add(start_timer_button);


        setVisible(true);
    }

    /**
     * функция что происходит после нажатия на кнопку
     * @param e - нажатие кнопки
     *
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();
        /**
         * начало наблюдения
         * @see Break_Window#Break_Window()
         * @see Parser#Parser()
         * @see Parser#observation(int)
         */
        if (cmd.equals("Start")){   //если нажата Start
            int tmp;
            tmp = Integer.parseInt(enter_timer.getText()); //получение введенного значения
            if(tmp < 1 || tmp > 240){
                enter_timer.setText("");
            }else {
                int timer = tmp; //записываем в переменную введенное пользователем время
                dispose();
                ExecutorService pool = Executors.newFixedThreadPool(2); //созд.потоки
                pool.execute(() -> {
                    new Break_Window().setVisible(true);  //создание окна "break_window"
                    try {
                        Parser parser = new Parser();
                        parser.observation(timer); //метод начало наблюдения
                    } catch (IOException | InterruptedException ioException) {
                        ioException.printStackTrace();
                    }
                });
                pool.shutdown();
            }
        }

    }
}

