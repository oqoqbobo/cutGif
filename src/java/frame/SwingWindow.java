package frame;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import component.MyCloseBtn;
import component.MyPanel;
import javafx.scene.image.Image;
import lombok.Data;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import util.WindowUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
@Data
public class SwingWindow extends JFrame {
    private static final int frameRate = 10;// 录制的帧率
    private static Integer theTimeCount = 0;
    private static SwingWindow instance;

    private Integer clickX;
    private Integer clickY;
    private MyPanel panel;

    //用于录制视频
    private Integer offsetX;
    private Integer offsetY;
    private Integer recordWidth;
    private Integer recordHeight;

    public static SwingWindow getInstance(){
        if(instance == null){
            instance = new SwingWindow();
        }
        return instance;
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

    private void recordHere(Integer offsetX,Integer offsetY,Integer width,Integer height) throws FrameGrabber.Exception, FrameRecorder.Exception, FileNotFoundException {

        FrameGrabber grabber = new FFmpegFrameGrabber("desktop");
        grabber.setFormat("gdigrab");
        grabber.setFrameRate(frameRate);
        // 捕获指定区域，不设置则为全屏
        grabber.setImageHeight(height);
        grabber.setImageWidth(width);
        grabber.setOption("offset_x", offsetX.toString());
        grabber.setOption("offset_y", offsetY.toString());//必须设置了大小才能指定区域起点，参数可参考 FFmpeg 入参
        grabber.start();

        AnimatedGifEncoder e = new AnimatedGifEncoder();
        // 设置生成图片大小
        e.setSize(900, 1000);
        //生成的图片路径
        e.start(new FileOutputStream("D://testGif.gif"));
        //图片之间间隔时间
        e.setDelay(500);
        //重复次数 0表示无限重复 默认不重复
        e.setRepeat(0);
        Java2DFrameConverter converter = new Java2DFrameConverter();
        // 用于存储视频 , 先调用stop，在释放，就会在指定位置输出文件，，这里我保存到D盘
        /*FrameRecorder recorder = FrameRecorder.createDefault("D://output.avi", grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setFrameRate(frameRate);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);// 编码，使用编码能让视频占用内存更小，根据实际自行选择
        recorder.start();*/
        getInstance().getPanel().setContent("初始化完成。。。");
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    getInstance().getPanel().setContent(theTimeCount.toString());
                    if(theTimeCount == 20){
                        // 停止
                        grabber.stop();

                        // 释放
                        grabber.release();
                        e.finish();
                        theTimeCount = 0;
                        timer.cancel();
                        getInstance().getPanel().setContent("录制完成。。。");
                        return;
                    }
                    theTimeCount += 1;
                    // 获取屏幕捕捉的一帧
                    Frame frame = grabber.grabFrame();

                    //添加帧
                    e.addFrame(converter.getBufferedImage(frame));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000 / frameRate); //执行逻辑   延迟时间   每次执行时间
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

                try {
                    recordHere(offsetX,offsetY,recordWidth,recordHeight);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

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
                getInstance().setOffsetX(clickX);
                getInstance().setOffsetY(clickY);
                getInstance().setRecordWidth(width);
                getInstance().setRecordHeight(length);

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
