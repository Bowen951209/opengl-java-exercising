package chapter7;

import org.lwjgl.glfw.GLFWKeyCallbackI;

import static org.lwjgl.glfw.GLFW.*;

public class KeyPressCallBack implements GLFWKeyCallbackI {




    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            if (key == GLFW_KEY_1) {
                Program7_1.currentProgram = Program7_1.gouraudProgram;
                System.out.println("Using ProgramID: " + Program7_1.currentProgram + "(Gouraud)");
            }
            if (key == GLFW_KEY_2) {
                Program7_1.currentProgram = Program7_1.phongProgram;
                System.out.println("Using ProgramID: " + Program7_1.currentProgram + "(Phong)");
            }
            if (key == GLFW_KEY_3) {
                Program7_1.currentProgram = Program7_1.blinnPhongProgram;
                System.out.println("Using ProgramID: " + Program7_1.currentProgram + "(Blinn-Phong)");
            }
            if (key == GLFW_KEY_TAB) {
                switch (Program7_1.usingModel) {
                    case "stanford-dragon" -> Program7_1.usingModel = "dolphin";
                    case "dolphin" -> Program7_1.usingModel = "stanford-bunny";
                    case "stanford-bunny" -> Program7_1.usingModel = "torus";
                    case "torus" -> Program7_1.usingModel = "stanford-dragon";
                }
                Program7_1.storeInBuffer(Program7_1.vbo);
            }
        }
    }
}
