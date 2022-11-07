package thread;

public class SourceOutputLock {
    private static SourceOutputLock lock;

    private SourceOutputLock(){}

    public static SourceOutputLock getLock(){
        if(lock == null){
            lock = new SourceOutputLock();
        }
        return lock;
    }
}
