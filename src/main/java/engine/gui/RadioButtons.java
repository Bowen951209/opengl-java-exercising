package engine.gui;

import imgui.ImGui;
import imgui.type.ImInt;

import java.util.HashMap;

public class RadioButtons implements GuiComponents {
    private final boolean isInSameLine;
    private final HashMap<Integer, String> selections = new HashMap<>();
    protected final ImInt chose = new ImInt();

    public ImInt getChose() {
        return chose;
    }

    public RadioButtons addSelection(int order, String label) {
        selections.put(order, label);
        return this;
    }

    public RadioButtons(boolean isInSameLine) {
        this.isInSameLine = isInSameLine;
    }

    @Override
    public void render() {
//        ImGui.checkbox(label, isSelected);
        for (int i = 0; i < selections.size(); i++) {
            ImGui.radioButton(selections.get(i), chose, i);
            if (isInSameLine) {
                ImGui.sameLine();
            }
        }
        ImGui.newLine();
    }
}
