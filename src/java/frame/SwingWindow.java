package frame;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import component.MyCloseBtn;
import component.MyPanel;
import lombok.Data;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.yaml.snakeyaml.Yaml;
import thread.DisassembleGIF;
import thread.MakeingGIF;
import thread.Recording;
import util.WindowUtil;
import utils.StringBuilderQueue;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

@Data
public class SwingWindow extends JFrame {
    private String gifPath = "123.gif";
    private String output = "/output";
    private static final int frameRate = 100;// 录制的帧率
    private static Integer theTimeCount = 0;
    private static SwingWindow instance;

    private Integer clickX;
    private Integer clickY;
    private MyPanel panel;

    private Recording recordRunnable = new Recording();
    private MakeingGIF gifRunnable = new MakeingGIF();
    private DisassembleGIF disassembleRunnable = new DisassembleGIF();

    //用于录制视频
    private Integer offsetX;
    private Integer offsetY;
    private Integer recordWidth;
    private Integer recordHeight;

    private static Integer currentKeyCode = null;
    private final static Integer CTRL = 17;

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
        this.setBackground(new Color(238, 153, 154,1));
//        this.setOpacity(0.1f);  组件也透明，不可取
        this.setVisible(true); //可见

        this.setSize(WindowUtil.getWinWidth(),WindowUtil.getWinHeight()-60);
        /*****目前可有可无******/
/*        this.setResizable(false); //不可改变大小
        this.setLocationRelativeTo(null); //居中
        this.setDefaultCloseOperation(EXIT_ON_CLOSE); //默认关闭形式*/
        /*****目前可有可无******/
        this.init();

        this.getPanel().revalidate();
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
        animated.setDelay(frameRate);
        //重复次数 0表示无限重复 默认不重复
        animated.setRepeat(0);
        int step = 4;
        int index = 0;

        for(BufferedImage img : images){
            if(index%step == 0){
                animated.addFrame(img);
            }
            index++;
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
                writer.writeToSequence(new IIOImage(image, null,metadata), params);
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


    private String outContain(String str){
        if(str.startsWith("(")){
            str = str.substring(1);
        }
        if(str.endsWith(")")){
            str = str.substring(0,str.length()-1);
        }
        return str;
    }


    //将output文件中每一帧按顺序生成一张gif
    public void frameToGif(){
        try {
//            FileImageOutputStream fileImageOutputStream = new FileImageOutputStream(new File(gifPath));

            File folder = new File(output);
            File[] files = folder.listFiles();
            Map<Integer,File> result = new TreeMap<>();
            for (File file : files) {
                String yStr = file.getName().substring(file.getName().indexOf("("), file.getName().indexOf(")"));
                int key = Integer.parseInt(outContain(yStr));
                result.put(key,file);
            }


            BufferedImage[] imgList = new BufferedImage[files.length];
            for (Integer index : result.keySet()){
                BufferedImage image = ImageIO.read(result.get(index));
                imgList[index] = image;
            }
            //制作gif
            animatedGif(imgList);
            //画出每一帧
//            getInstance().getPanel().drawImage(imgList);
//            convert(imgList,fileImageOutputStream);
            System.out.println("制作完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将gif分解成每一帧，保存到output文件加中
    public void getFrameByGIF(){
        try {

            //删除源文件夹里的内容
            File originFile = new File(output);
            for (File file : originFile.listFiles()) {
                file.delete();
            }

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
                setOneFrame(converter.getBufferedImage(frame),path+"/"+newName+fileTail);
            }
            System.out.println("拆解完成！");
            
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setOneFrame(BufferedImage capture,String path) throws IOException {
        File oneFrame = new File(path);
        if(!oneFrame.exists()){
            oneFrame.mkdirs();
//            System.out.println("创建文件成功！");
        }
        ImageIO.write(capture,"jpg",oneFrame);
//        System.out.println("生成一帧："+oneFrame.getPath());
    }

    public void recordHere(Timer timer) throws AWTException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        if(recordWidth == null || recordHeight == null || (recordWidth <= 100 && recordHeight <= 100)){
            System.out.println("录制框框太小");
            return;
        }

        //删除源文件夹里的内容
        File originFile = new File(output);
        for (File file : originFile.listFiles()) {
            file.delete();
        }

        getInstance().getPanel().setContent("初始化完成。。。");

        Robot robot= new Robot();
        String fileName = sdf.format(new Date());
        String fileTail = "jpg";
        StringBuilder sb = new StringBuilder();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sb.setLength(0);
                    getInstance().getPanel().setContent(theTimeCount.toString());
                    BufferedImage capture = robot.createScreenCapture(new Rectangle(clickX, clickY, recordWidth, recordHeight));
                    String newName = fileName+ "("+theTimeCount+")";
                    sb.append(output).append("/").append(newName).append(".").append(fileTail);
                    setOneFrame(capture,sb.toString());
                    if(theTimeCount == 368){
                        theTimeCount = 0;
                        timer.cancel();
                        getInstance().getPanel().setContent("录制完成。。。");
                        return;
                    }
                    theTimeCount += 1;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, frameRate); //执行逻辑   延迟时间   每次执行时间
    }

    public void init(){
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                currentKeyCode = null;
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(theTimeCount != 0){
                    System.out.println("正在录制，输入指令无效");
                    return;
                }

                currentKeyCode = e.getKeyCode();
                StringBuilderQueue queue = StringBuilderQueue.getInstance();
                if(e.getKeyCode() == 10){
                    queue.append('~',e.getKeyCode());
                }else if(e.getKeyCode() == 8){
                    queue.back();
                }else{
                    queue.append(e.getKeyChar(),e.getKeyCode());
                }

                String str = queue.getLastIndexOfString("gif~".length());
                if(str.equals("gif~")){
                    System.out.println("开始制作gif！");
                    new Thread(gifRunnable).start();
                }
                str = queue.getLastIndexOfString("start~".length());
                if(str.equals("start~")){
                    System.out.println("开始录制gif帧！");
                    new Thread(recordRunnable).start();
                }
                str = queue.getLastIndexOfString("disassemble~".length());
                if(str.equals("disassemble~")){
                    System.out.println("开始拆解gif！");
                    new Thread(disassembleRunnable).start();
                }
            }

        });
        //添加鼠标点击事件
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(theTimeCount != 0){
                    System.out.println("正在录屏中");
                    return;
                }
                if(CTRL == currentKeyCode){
                    getInstance().getPanel().draw(e.getX(),e.getY());
                    return;
                }else{
                    //满足需要拖拽的情况下，不清空！！！！！
                    //点击前初始化整个展示画面（相当于还原）
                    getInstance().getPanel().drawClear();
                }

                System.out.println("pressX: "+e.getX());
                System.out.println("pressY: "+e.getY());
                //初始化点击位置
                getInstance().setClickX(e.getX());
                getInstance().setClickY(e.getY());
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if(theTimeCount != 0){
                    System.out.println("正在录屏中");
                    return;
                }
                System.out.println("releaseX: "+e.getX());
                System.out.println("releaseY: "+e.getY());

                SwingWindow.getInstance().getPanel().setContent("ceshi");

            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if(theTimeCount != 0){
                    System.out.println("正在录屏中");
                    return;
                }
                Integer clickX = getInstance().getClickX();
                Integer clickY = getInstance().getClickY();
                getInstance().setOffsetX(clickX);
                getInstance().setOffsetY(clickY);

                if(CTRL == currentKeyCode){
                    getInstance().getPanel().draw(e.getX(),e.getY());
                    return;
                }

                Integer width = 0;
                Integer length = 0;
//                System.out.println("currentKeyCode   "+currentKeyCode+" ,CTRL   "+CTRL);
//                System.out.println(CTRL == currentKeyCode);
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

                System.out.println("moveX:"+clickX + "\tmoveY:"+clickY + "\twidth:"+width+"\tlength:"+length);
                getInstance().getPanel().draw(clickX,clickY,width,length);
                getInstance().setRecordWidth(width);
                getInstance().setRecordHeight(length);

            }
        });

        Container container = this.getContentPane();

        MyPanel panel = new MyPanel();
        this.setPanel(panel); // 初始化单例的面板
        container.add(panel);

        MyCloseBtn myClose = new MyCloseBtn();
        panel.add(myClose);
    }
    public static void main(String [] args){
//        SwingWindow.getInstance().frameToGif();
        SwingWindow.getInstance();
//        SwingWindow.getInstance().getFrameByGIF();
    }

}
