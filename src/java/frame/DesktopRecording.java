package frame;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author lingkang
 * @date 2021/12/28
 */
public class DesktopRecording extends Application {
    private static final int frameRate = 10;// 录制的帧率
    private static boolean isStop = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("lingkang-桌面录屏emm...");
        ImageView imageVideo = new ImageView();
        imageVideo.setFitWidth(800);
        imageVideo.setFitHeight(600);
        Button button = new Button("停止录制");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                isStop = true;
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("info");
                alert.setHeaderText("已经停止录制");
                alert.setOnCloseRequest(event1 -> alert.hide());
                alert.showAndWait();
            }
        });

        VBox box = new VBox();
        box.getChildren().addAll(button, imageVideo);
        primaryStage.setScene(new Scene(box));
        primaryStage.setHeight(600);
        primaryStage.setWidth(800);
        primaryStage.show();

        // window 建议使用 FFmpegFrameGrabber("desktop") 进行屏幕捕捉
        FrameGrabber grabber = new FFmpegFrameGrabber("desktop");
        grabber.setFormat("gdigrab");
        grabber.setFrameRate(frameRate);
        // 捕获指定区域，不设置则为全屏
        grabber.setImageHeight(600);
        grabber.setImageWidth(800);
        grabber.setOption("offset_x", "200");
        grabber.setOption("offset_y", "200");//必须设置了大小才能指定区域起点，参数可参考 FFmpeg 入参
        grabber.start();

        // 用于存储视频 , 先调用stop，在释放，就会在指定位置输出文件，，这里我保存到D盘
        FrameRecorder recorder = FrameRecorder.createDefault("D://output.avi", grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setFrameRate(frameRate);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);// 编码，使用编码能让视频占用内存更小，根据实际自行选择
        recorder.start();


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isStop) {
                    try {
                        // 停止
                        recorder.stop();
                        grabber.stop();

                        // 释放
                        recorder.release();
                        grabber.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    timer.cancel();
                    return;
                }
                try {
                    // 获取屏幕捕捉的一帧
                    Frame frame = grabber.grabFrame();
                    // 将这帧放到录制
                    recorder.record(frame);

                    Image convert = new JavaFXFrameConverter().convert(frame);
                    // 更新UI
                    imageVideo.setImage(convert);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2000, 1000 / frameRate);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                isStop = true;
            }
        });
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}



