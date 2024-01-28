public class MyTimer {
    private long startTime;
    private long elapsedTime;
    private boolean isRunning;

    public MyTimer() {
        startTime = 0;
        elapsedTime = 0;
        isRunning = false;
    }

    public void start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime;
            isRunning = true;
        }
    }

    public long getElapsedTime() {
        if (isRunning) {
            return System.currentTimeMillis() - startTime;
        } else {
            return elapsedTime;
        }
    }
}
