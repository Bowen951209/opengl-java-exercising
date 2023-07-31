package engine.gui;

import engine.App;
import engine.util.Timer;

public class FpsDisplay implements GuiComponents {
    private static final float DEFAULT_UPDATE_RATE = 0.1f;
    private static final int DEFAULT_PRECISION_AFTER_POINT = 2;
    private final Text text = new Text("");
    private final GuiWindow guiWindow = new GuiWindow("FPS", false).addChild(text);
    private final App app;
    private final Timer timer = new Timer();

    public FpsDisplay(App app) {
        this.app = app;
        // assume digits of 3 (4 = 3 + 1 for dot)
        guiWindow.setWidth(app.getGui().getFontScale() * (4 + DEFAULT_PRECISION_AFTER_POINT) * 10f);
        guiWindow.setHeight(app.getGui().getFontScale() * 30f);
    }

    private void updateFps() {
        // Assume fps will only go to 2 or 3 digits.
        if (app.getFps() < 100) {
            // 2 digits
            text.setContent(Float.toString(app.getFps()).substring(0, DEFAULT_PRECISION_AFTER_POINT + 3));
        } else {
            // 3 digits
            text.setContent(Float.toString(app.getFps()).substring(0, DEFAULT_PRECISION_AFTER_POINT + 4));
        }
    }

    @Override
    public void render() {
        timer.start();
        guiWindow.render();
        timer.end();
        timer.addTotalTime();
        if (Timer.nanoToMillisecond(timer.getTotalTime()) >= DEFAULT_UPDATE_RATE) {
            updateFps();
            timer.resetTotalTime();
        }
    }
}
