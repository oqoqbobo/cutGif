package component;

import lombok.Data;
import util.WindowUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Data
public class MyPanel extends JPanel {
    //矩形
    private Integer clickX = 0;
    private Integer clickY = 0;
    private Integer myWidth = 0;
    private Integer myLength = 0;
    //帧
    private Image[] images;

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
    public void drawImage(Image[] images){
        setImages(images);
    }

    @Override
    public void paint(Graphics g) {
        this.setOpaque(false); //将面板设置为透明的
        super.paint(g);//继承父类的绘制方式
        if(WindowUtil.getWinWidth() - 30 <= clickX && clickY <= 30){
            System.out.println("这里是关闭按钮");
            return;
        }
        g.drawRect(clickX,clickY,myWidth,myLength);//绘制矩形
        g.setFont(new Font("微软雅黑",1,30));
        g.drawString(content, clickX+10, clickY+40);
        if(this.getImages() != null && getImages().length > 0){
            Integer index = 1;
            Integer flag = 0;
            for(Image img : getImages()){
                if(img.getWidth(this)*(index+2) > WindowUtil.getWinWidth()){
                    //换行
                    flag++;
                    index = 1;
                }
                g.drawImage(img,img.getWidth(null)*index,100 + img.getHeight(null) * flag,this);
                index++;
            }
        }
        this.repaint();//刷新绘制
    }
}
