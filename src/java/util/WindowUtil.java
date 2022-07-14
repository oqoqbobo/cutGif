package util;

import java.awt.*;

public class WindowUtil {
    private static Dimension screenSize;
    static{
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    }
    public static Integer getWinWidth(){
        return (int)screenSize.getWidth();
    }
    public static Integer getWinHeight(){
        return (int)screenSize.getHeight();
    }
}
