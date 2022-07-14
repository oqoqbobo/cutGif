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
    private static SwingWindow instance;

    public static SwingWindow getInstance(){
        if(instance == null){
            instance = new SwingWindow();
        }
        return instance;
    }

    private Integer clickX;
    private Integer clickY;
    private MyPanel panel;

    public Integer getClickX() {
        return clickX;
    }

    public void setClickX(Integer clickX) {
        this.clickX = clickX;
    }

    public Integer getClickY() {
        return clickY;
    }

    public void setClickY(Integer clickY) {
        this.clickY = clickY;
    }

    public MyPanel getPanel() {
        return panel;
    }

    public void setPanel(MyPanel panel) {
        this.panel = panel;
    }

    public SwingWindow(){

        this.setUndecorated(true); //这点奇怪，需要的
        this.setBackground(new Color(238, 153, 154,30));
//        this.setOpacity(0.1f);  组件也透明，不可取
        this.setVisible(true); //可见

        this.setSize(WindowUtil.getWinWidth(),WindowUtil.getWinHeight());
        /*****目前可有可无******/
        this.setResizable(false); //不可改变大小
        this.setLocationRelativeTo(null); //居中
        this.setDefaultCloseOperation(EXIT_ON_CLOSE); //默认关闭形式
        /*****目前可有可无******/
        this.init();

    }

    public void init(){
        //添加鼠标点击事件
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //点击前初始化整个展示画面（相当于还原）
                getInstance().getPanel().drawClear();
                System.out.println("press: "+e.getX());
                System.out.println("press: "+e.getY());
                //初始化点击位置
                getInstance().setClickX(e.getX());
                getInstance().setClickY(e.getY());
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
                Integer clickX = getInstance().getClickX();
                Integer clickY = getInstance().getClickY();
                Integer width = 0;
                Integer length = 0;

                if(e.getX() < clickX){
                    width = clickX - e.getX();
                    clickX = e.getX();
                }else{
                    width = e.getX() - clickX;
                }

                if(e.getY() < clickY){
                    length = clickY - e.getY();
                    clickY = e.getY();
                }else{
                    length = e.getY() - clickY;
                }

                System.out.println("x:"+clickX + "   y:"+clickY + "   width:"+width+"    length:"+length);
                getInstance().getPanel().draw(clickX,clickY,width,length);

            }
        });

        Container container = this.getContentPane();

        MyPanel panel = new MyPanel();
        this.setPanel(panel); // 初始化单例的面板
        container.add(panel);

        MyCloseBtn myClose = new MyCloseBtn();
        container.add(myClose);
    }


}
