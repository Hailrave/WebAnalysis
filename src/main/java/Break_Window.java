import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Break_Window extends JFrame implements ActionListener //окно завершения работы программы
        //aka прервать программу
{
    private final JButton brake_button;

    public Break_Window()
    {

        setSize(300,200);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        brake_button = new JButton("Break");
        brake_button.addActionListener(this);
        brake_button.setActionCommand("Break");
        add(brake_button);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        String cmd = e.getActionCommand();

        if (cmd.equals("Break")){  //завершение работы программы
            System.exit(0);
        };
    }
}
