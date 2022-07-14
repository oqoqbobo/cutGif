package component;

import javax.swing.*;
import java.awt.*;

public class MyPanel extends JPanel {
    private Integer clickX = 0;
    private Integer clickY = 0;
    private Integer myWidth = 0;
    private Integer myLength = 0;

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

    public Integer getMyWidth() {
        return myWidth;
    }

    public void setMyWidth(Integer myWidth) {
        this.myWidth = myWidth;
    }

    public Integer getMyLength() {
        return myLength;
    }

    public void setMyLength(Integer myLength) {
        this.myLength = myLength;
    }

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

    @Override
    public void paint(Graphics g) {
        this.setOpaque(false); //将面板设置为透明的
        super.paint(g);//继承父类的绘制方式
        g.drawRect(clickX,clickY,myWidth,myLength);//绘制矩形
        this.repaint();//刷新绘制
    }
}
