package frame;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import component.MyCloseBtn;
import component.MyPanel;
import lombok.Data;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.yaml.snakeyaml.Yaml;
import util.WindowUtil;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Timer;
import java.util.stream.Stream;

@Data
public class SwingWindow extends JFrame {
    private String gifPath = "123.gif";
    private String output = "/output";
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

    {
        InputStream in = null;
        Yaml yaml = new Yaml();
        try{
            in = SwingWindow.class.getClassLoader().getResourceAsStream("application.yaml");
            LinkedHashMap<String, Object> sourceMap = (LinkedHashMap) yaml.load(in);
            //setValue 是递归时传递的值，一开始为空
            gifPath = getKeyValue(null, sourceMap, "web.filePath");
            output = getKeyValue(null, sourceMap, "web.output");
            System.out.println(output);
            System.out.println(gifPath);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //获取yaml文件的对应key的值
    public static String getKeyValue(String setValue,LinkedHashMap<String, Object> sourceMap,String dicKey) throws Exception {
        String[] split = dicKey.split("\\.");
        if(split.length <= 0){
            return "";
        }
        for(String key:sourceMap.keySet()){
            Object value = sourceMap.get(key);
            if(value == null){
                return "";
            }else if(value.getClass().getTypeName().equals(LinkedHashMap.class.getTypeName())){
                //如果是需要找的key，继续查询下一个
                if(split[0].equals(key) && split.length >= 2){
                    LinkedHashMap<String,Object> other = (LinkedHashMap)value;
                    String otherKey = "";
                    for(int i=1;i<split.length;i++){
                        if(i == split.length-1){
                            otherKey+=split[i];
                        }else{
                            otherKey+=split[i]+".";
                        }
                    }
                    return getKeyValue(setValue,other,otherKey);
                }
            }else if(split[0].equals(key) && value.getClass().getTypeName().equals(String.class.getTypeName())){
                setValue = value.toString();
            }
        }
        return setValue;
    }

    public static SwingWindow getInstance(){
        if(instance == null){
            instance = new SwingWindow();
        }
        return instance;
    }

    public SwingWindow(){

        this.setUndecorated(true); //这点奇怪，需要的
        this.setBackground(new Color(238, 153, 154,10));
//        this.setOpacity(0.1f);  组件也透明，不可取
        this.setVisible(true); //可见

        this.setSize(WindowUtil.getWinWidth(),WindowUtil.getWinHeight()-60);
        /*****目前可有可无******/
        this.setResizable(false); //不可改变大小
        this.setLocationRelativeTo(null); //居中
        this.setDefaultCloseOperation(EXIT_ON_CLOSE); //默认关闭形式
        /*****目前可有可无******/
        this.init();

    }
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }

    private void configureRootMetadata(IIOMetadata metadata,int delay, boolean loop) throws IIOInvalidTreeException {
        String metaFormatName = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);
        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");
        IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");

        int loopContinuously = loop ? 0 : 1;
        child.setUserObject(new byte[]{0x1, (byte) (loopContinuously & 0xFF), (byte) ((loopContinuously >> 8) & 0xFF)});
        appExtensionsNode.appendChild(child);
        metadata.setFromTree(metaFormatName, root);
    }

    //导出实体的gif  缺点：提供png图片作为帧，但是导出效果是实体的，很烦
    public void animatedGif(BufferedImage[] images) throws Exception {
        if(images == null || images.length<=0){
            throw new Exception("请传递images参数");
        }
        AnimatedGifEncoder animated = new AnimatedGifEncoder();
        //生成的图片路径
        animated.start(new FileOutputStream(gifPath));
        // 设置生成图片大小
        animated.setSize(images[0].getWidth(null), images[0].getHeight(null));
        //图片之间间隔时间 单位毫秒
        animated.setDelay(10);
        //重复次数 0表示无限重复 默认不重复
        animated.setRepeat(0);
        for(BufferedImage img : images){
            animated.addFrame(img);
        }
        animated.finish();
    }

    //导出透明背景的gif  缺点，gif有残影，且每一帧图片会出现多余的镂空现象（和原图不一样）
    public void convert(BufferedImage[] images, ImageOutputStream outputStream) throws Exception {
        if(images == null || images.length<=0){
            throw new Exception("请传递images参数");
        }
        ImageWriter writer = ImageIO.getImageWritersBySuffix("gif").next();
        try {
            //图像类型
            writer.setOutput(outputStream); //设置输出流
            ImageWriteParam params = writer.getDefaultWriteParam();
            IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(images[0].getType()), params);
            //配置元数据  delay 单位毫秒  loop 是否重复（true表示重复）
            configureRootMetadata(metadata,100,true);
            writer.prepareWriteSequence(null); //初始化

            AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(1, 1), null);
            for (BufferedImage image : images) {
                writer.writeToSequence(new IIOImage(op.filter(image,null), null,metadata), params);
            }

        } catch (Exception e) {
            throw new RuntimeException("GIF convert error", e);
        }finally {
            try {
                if(writer != null && outputStream != null){
                    writer.endWriteSequence();
                    writer.reset();
                    writer.dispose();

                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //将output文件中每一帧按顺序生成一张gif
    public void frameToGif(){
        try {
            FileImageOutputStream fileImageOutputStream = new FileImageOutputStream(new File(gifPath));

            File folder = new File(output);
            File[] files = folder.listFiles();
            BufferedImage[] imgList = new BufferedImage[files.length];
            int index = 0;
            for (File file : files){
                System.out.println(file.getPath());
                BufferedImage image = ImageIO.read(new File(file.getPath()));
                imgList[index] = image;
                index++;
            }
            //制作gif
            animatedGif(imgList);
            //画出每一帧
            getInstance().getPanel().drawImage(imgList);
//            convert(imgList,fileImageOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将gif分解成每一帧，保存到output文件加中
    public void getFrameByGIF(){
        try {
            //用于获取图像的帧
            FFmpegFrameGrabber grabberGif = new FFmpegFrameGrabber(gifPath);
            grabberGif.start();

            String path = output;
            GifDecoder gd = new GifDecoder();
            //要处理的图片
            File gif = new File(gifPath);
            gd.read(new FileInputStream(gif)); //用于获取帧数（数量）
            String fileName = gif.getName().substring(0, gif.getName().lastIndexOf("."));
            String fileTail = gif.getName().substring(gif.getName().lastIndexOf("."));
            // 类型转换,Frame -> BufferedImage
            Java2DFrameConverter converter = new Java2DFrameConverter();

            for (int i = 0; i < gd.getFrameCount(); i++) {
                //取得gif的每一帧
                Frame frame = grabberGif.grabImage(); //会自动获取下一帧，特别注意
                String newName = fileName+ "("+i+")";
                File oneFrame = new File(path+newName+fileTail);
                if(!oneFrame.exists()){
                    oneFrame.mkdirs();
                    System.out.println("创建文件成功！");
                }
                ImageIO.write(converter.getBufferedImage(frame),"jpg",oneFrame);
                System.out.println("生成一帧："+oneFrame.getPath());
            }
            
        }catch (Exception e) {
            e.printStackTrace();
        }
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
        e.setSize(width, height);
        //生成的图片路径
        e.start(new FileOutputStream(gifPath));
        //图片之间间隔时间 单位毫秒
        e.setDelay(10);
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
                    if(theTimeCount == 100){
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
        }, 0, 100 / frameRate); //执行逻辑   延迟时间   每次执行时间
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
    public static void main(String [] args){
        SwingWindow.getInstance().frameToGif();
//        SwingWindow.getInstance().getFrameByGIF();
    }

}
