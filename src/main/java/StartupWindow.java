
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class StartupWindow extends JFrame implements ActionListener //главное окно программы
{

    private final JButton start_button;
    private final JButton timer_button;

    public StartupWindow()
    {
        setTitle("News catcher");
        setSize(300,200);
        setLocationRelativeTo(null);
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
            new Break_Window();
            //функция начало наблюдения
        }
        if(cmd.equals("Set")) //создание окна "timer_window"
        {
            dispose();
            new Timer_Window();
        }
    }


    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run()
            {
                new StartupWindow().setVisible(true);
            }

        });
    }
}

