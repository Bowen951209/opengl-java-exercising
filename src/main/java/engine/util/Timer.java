package engine.util;

public class Timer {
    private long startTime;

    public void start() {
        startTime = System.nanoTime();
    }
    public void end(String frontWord) {
        long endTime = System.nanoTime();
        long passTime = endTime - startTime;
        System.out.println(frontWord + ": " + nanoToMillisecond(passTime) + " millisecond");
    }

    private static float nanoToMillisecond(long nanoSecond) {
        return nanoSecond * 0.000001f;
    }
}
