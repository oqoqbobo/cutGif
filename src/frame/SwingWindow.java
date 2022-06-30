package frame;

import component.MyCloseBtn;
import component.MyPanel;
import util.WindowUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class SwingWindow extends JFrame {

    public SwingWindow(){

/*        this.setUndecorated(true); //这点奇怪，需要的
        this.setBackground(new Color(238, 153, 154,30));
//        this.setOpacity(0.1f);  组件也透明，不可取*/
        this.setVisible(true); //可见

        this.setSize(WindowUtil.getWinWidth(),WindowUtil.getWinHeight());
        this.setResizable(false); //不可改变大小
        this.setLocationRelativeTo(null); //居中
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.init();

    }

    public void init(){
        //添加鼠标点击事件
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("press: "+e.getX());
                System.out.println("press: "+e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("release: "+e.getX());
                System.out.println("release: "+e.getY());
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println("move:"+e.getX());
                System.out.println("move:"+e.getY());

            }
        });

        this.setLayout(null);
        Container container = this.getContentPane();
        MyPanel panel = new MyPanel();
        container.add(panel);
    }


}
