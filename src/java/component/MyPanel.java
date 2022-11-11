package component;

import lombok.Data;
import util.WindowUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    private Image tuCeng;


    public MyPanel(){
        try {
            tuCeng = ImageIO.read(new File("D://tuCeng.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        this.setBackground(new Color(238, 153, 154,100));
    }
    public void draw(Integer clickX,Integer clickY,Integer myWidth,Integer myLength){
        this.setClickX(clickX);
        this.setClickY(clickY);
        this.setMyLength(myLength);
        this.setMyWidth(myWidth);
        this.repaint();
    }
    public void draw(Integer clickX,Integer clickY){
        this.setClickX(clickX);
        this.setClickY(clickY);
        this.repaint();
    }
    public void drawClear(){
        this.setClickX(0);
        this.setClickY(0);
        this.setMyLength(0);
        this.setMyWidth(0);
        this.repaint();
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
        if(myWidth > 0 || myLength > 0){
            g.drawRect(clickX-1,clickY-1,myWidth+1,myLength+1);//绘制矩形
            g.setFont(new Font("微软雅黑",1,30));
            if(clickY - 8 >= 30){
                g.drawString(content, clickX+4, clickY-8);
            }else{
                g.drawString(content, clickX+4, clickY+myLength+30);
            }

            g.drawImage(tuCeng,clickX,clickY,80,40,this);
        }

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
