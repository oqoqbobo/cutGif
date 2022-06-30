package component;

import util.WindowUtil;

import javax.swing.*;
import java.awt.*;

public class MyCloseBtn extends JButton {
    public MyCloseBtn(){

        this.setBounds(WindowUtil.getWinWidth() - 30, 0, 30, 30);
        this.setBorderPainted(false);
        this.setBackground(Color.RED);
        this.setForeground(Color.WHITE);
        this.setFont(new Font("华文行楷",1,20));
        this.addActionListener(actionEvent -> {
            System.exit(0);
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //重新绘制
        g.drawString("X",9,22);
    }
}
