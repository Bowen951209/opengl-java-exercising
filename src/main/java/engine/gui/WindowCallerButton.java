package engine.gui;

public class WindowCallerButton extends Button implements Interactable{
    private final GuiWindow guiWindow;
    public WindowCallerButton(String content, GuiWindow guiWindow) {
        super(content);
        this.guiWindow = guiWindow;
    }

    @Override
    public void behave() {
        if (super.isClick.get()) {
            // show window
            System.out.println(guiWindow.isShow().get());
            guiWindow.show();
        }
    }

    @Override
    public void render() {
        super.render();
        behave();
    }
}
