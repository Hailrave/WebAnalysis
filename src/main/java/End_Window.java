import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * класс - окно полного завершения работы первичного наполнения
 */
public class End_Window extends JFrame //окно полного завершения работы первичного наполнения

{
    /**
     * поле с текстом
     */
    private final JLabel label;

    /**
     * конструктор
     */
    public End_Window()
    {
        setTitle("News catcher");
        setSize(400,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        URL iconURL = getClass().getResource("/newsIcon.png");
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        label = new JLabel("The parser has finished working. Press ❌ to close the program");
        add(label);

        setVisible(true);
    }


}

