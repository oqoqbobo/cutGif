package thread;

import frame.SwingWindow;

public class DisassembleGIF implements Runnable{
    @Override
    public void run() {
        synchronized (SourceOutputLock.getLock()){
            SwingWindow.getInstance().getFrameByGIF();
        }
    }
}
