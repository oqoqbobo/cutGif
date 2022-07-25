import frame.SwingWindow;

public class Main {

    public static void main(String[] args) {
//        System.out.println("Hello World!");
//        SwingWindow.getInstance();
        System.out.println((byte) (0 & 0xFF));
        System.out.println((byte) ((0 << 8)));
        System.out.println("================--------===============");
        System.out.println((byte) (1 & 0xFF));
        System.out.println((byte) ((1 << 8)));
    }
}
