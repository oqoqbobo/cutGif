package thread;

import com.madgag.gif.fmsware.AnimatedGifEncoder;
import frame.SwingWindow;
import org.bytedeco.javacv.*;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Recording implements Runnable {
    private Timer timer = new Timer();
    @Override
    public void run() {
        synchronized (SourceOutputLock.getLock()){
            timer = new Timer();
            try {
                SwingWindow.getInstance().recordHere(timer);
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
