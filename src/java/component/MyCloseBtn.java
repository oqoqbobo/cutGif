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
        this.setFont(new Font("微软雅黑",1,20));
        this.addActionListener(actionEvent -> {
            System.exit(0);
        });
    }

    @Override
    public void paint(Graphics g) {
        //每次绘制都要重新定位，因为调用了父类的绘制方法
        this.setBounds(WindowUtil.getWinWidth() - 30, 0, 30, 30);
        super.paint(g);
        //重新绘制
        g.drawString("X",9,22);
    }
}
