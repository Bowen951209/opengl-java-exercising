package engine.util;

public class Timer {
    private long startTime;
    private long passTime;
    private long totalTime;

    public long getTotalTime() {
        return totalTime;
    }

    public void addTotalTime() {
        totalTime += passTime;
    }
    public void resetTotalTime() {
        totalTime = 0;
    }

    public float getFps() {
        return 1000f / nanoToMillisecond(passTime);
    }

    public void start() {
        startTime = System.nanoTime();
    }
    public void end(String frontWord) {
        long endTime = System.nanoTime();
        passTime = endTime - startTime;
        System.out.println(frontWord + nanoToMillisecond(passTime) + " millisecond");
    }

    public void end() {
        long endTime = System.nanoTime();
        passTime = endTime - startTime;
    }

    public static float nanoToMillisecond(long nanoSecond) {
        return nanoSecond * 0.000001f;
    }
}
