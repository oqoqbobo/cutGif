package component;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {
    public MyPanel(){
        this.setBackground(new Color(238, 153, 154,100));
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(10,10,100,100);
    }
}
