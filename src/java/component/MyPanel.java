package component;

import lombok.Data;

import javax.swing.*;
import java.awt.*;
@Data
public class MyPanel extends JPanel {
    private Integer clickX = 0;
    private Integer clickY = 0;
    private Integer myWidth = 0;
    private Integer myLength = 0;

    private String content = "";


    public MyPanel(){
//        this.setBackground(new Color(238, 153, 154,100));
    }
    public void draw(Integer clickX,Integer clickY,Integer myWidth,Integer myLength){
        this.setClickX(clickX);
        this.setClickY(clickY);
        this.setMyLength(myLength);
        this.setMyWidth(myWidth);
    }
    public void drawClear(){
        this.setClickX(0);
        this.setClickY(0);
        this.setMyLength(0);
        this.setMyWidth(0);
    }
    public void drawFont(String content){
        this.setContent(content);
    }

    @Override
    public void paint(Graphics g) {
        this.setOpaque(false); //将面板设置为透明的
        super.paint(g);//继承父类的绘制方式
        g.drawRect(clickX,clickY,myWidth,myLength);//绘制矩形
        g.setFont(new Font("华文行楷",1,30));
        g.drawString(content, clickX-10, clickY-10);
        this.repaint();//刷新绘制
    }
}
