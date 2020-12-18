import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Timer_Window extends JFrame implements ActionListener //ввод таймера и начало наблюдения
{
    private final JButton start_timer_button;
    private final JLabel label;
    private final JTextField enter_timer;

    public Timer_Window()
    {
        setTitle("News catcher");
        setSize(500,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();

        if (cmd.equals("Start")){   //ввод таймера и начало наблюдения
            String tmp;
            tmp = enter_timer.getText();
            if(Integer.parseInt(tmp) < 1 || Integer.parseInt(tmp) > 240){
                enter_timer.setText("");
            }else {
                int timer = Integer.parseInt(tmp);
                dispose();
                new Break_Window();  //создание окна "break_window"
                //тут вызов функции наблюдения с таймером
            }
        }

    }
}

