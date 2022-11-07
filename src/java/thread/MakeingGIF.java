package thread;

import frame.SwingWindow;

public class MakeingGIF implements Runnable{
    @Override
    public void run() {
        synchronized (SourceOutputLock.getLock()){
            SwingWindow.getInstance().frameToGif();
        }
    }
}
