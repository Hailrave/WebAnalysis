import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class Break_Window extends JFrame implements ActionListener //окно завершения работы программы
        //aka прервать программу
{
    private final JButton break_button;

    public Break_Window()
    {
        setSize(400,300);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        URL iconURL = getClass().getResource("/newsIcon.png");
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        break_button = new JButton("Break");
        break_button.addActionListener(this);
        break_button.setActionCommand("Break");
        add(break_button);

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
