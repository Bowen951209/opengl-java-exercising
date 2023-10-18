import chapter10.Program10_1;
import chapter10.Program10_2;
import chapter10.Program10_3;
import chapter10.Program10_4;
import chapter12.*;
import chapter13.Program13_1;
import chapter13.Program13_2;
import chapter13.Program13_3;
import chapter13.Program13_4;
import chapter14.Program14_1;
import chapter15.Program15_1;
import chapter16.Program16_1;
import chapter16.Program16_2;
import chapter4.Program4_2;
import chapter4.Program4_3;
import chapter4.Program4_4;
import chapter5.Program5_1;
import chapter6.Program6_1;
import chapter6.Program6_2;
import chapter6.Program6_3;
import chapter7.Program7_1;
import chapter7.Program7_2;
import chapter8.Program8_1;
import chapter9.Program9_2;
import chapter9.Program9_3;

import javax.swing.*;
import java.awt.*;

public class ProgramSelector extends JFrame {
    private ProgramSelector(int initWidth, int initHeight) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Program Selector");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JLabel descriptionLabel = new JLabel("You can select program here.");
        descriptionLabel.setFont(new Font("Default", Font.BOLD, 15));
        panel.add(descriptionLabel);
        panel.add(new JLabel("Chapter4:"));
        panel.add(new LauncherButton(Program4_2.class.getSimpleName(), ()->Program4_2.main(null),
                this));
        panel.add(new LauncherButton(Program4_3.class.getSimpleName(), ()->Program4_3.main(null),
                this));
        panel.add(new LauncherButton(Program4_4.class.getSimpleName(), ()-> Program4_4.main(null)
                , this));
        panel.add(new JLabel("Chapter5:"));
        panel.add(new LauncherButton(Program5_1.class.getSimpleName(), ()->Program5_1.main(null),
                this));
        panel.add(new JLabel("Chapter6:"));
        panel.add(new LauncherButton(Program6_1.class.getSimpleName(), ()->Program6_1.main(null),
                this));
        panel.add(new LauncherButton(Program6_2.class.getSimpleName(), ()->Program6_2.main(null),
                this));
        panel.add(new LauncherButton(Program6_3.class.getSimpleName(), ()->Program6_3.main(null),
                this));
        panel.add(new JLabel("Chapter7:"));
        panel.add(new LauncherButton(Program7_1.class.getSimpleName(), ()->Program7_1.main(null),
                this));
        panel.add(new LauncherButton(Program7_2.class.getSimpleName(), ()->Program7_2.main(null),
                this));
        panel.add(new JLabel("Chapter8:"));
        panel.add(new LauncherButton(Program8_1.class.getSimpleName(), ()->Program8_1.main(null),
                this));
        panel.add(new JLabel("Chapter9:"));
        panel.add(new LauncherButton(Program9_2.class.getSimpleName(), ()->Program9_2.main(null),
                this));
        panel.add(new LauncherButton(Program9_3.class.getSimpleName(), ()->Program9_3.main(null),
                this));
        panel.add(new JLabel("Chapter10:"));
        panel.add(new LauncherButton(Program10_1.class.getSimpleName(),
                ()->Program10_1.main(null), this));
        panel.add(new LauncherButton(Program10_2.class.getSimpleName(),
                ()->Program10_2.main(null), this));
        panel.add(new LauncherButton(Program10_3.class.getSimpleName(),
                ()->Program10_3.main(null), this));
        panel.add(new LauncherButton(Program10_4.class.getSimpleName(),
                ()->Program10_4.main(null), this));
        panel.add(new JLabel("Chapter12:"));
        panel.add(new LauncherButton(Program12_1.class.getSimpleName(),
                ()->Program12_1.main(null), this));
        panel.add(new LauncherButton(Program12_2.class.getSimpleName(),
                ()->Program12_2.main(null), this));
        panel.add(new LauncherButton(Program12_3.class.getSimpleName(),
                ()->Program12_3.main(null), this));
        panel.add(new LauncherButton(Program12_4.class.getSimpleName(),
                ()->Program12_4.main(null), this));
        panel.add(new LauncherButton(Program12_5.class.getSimpleName(),
                ()->Program12_5.main(null), this));
        panel.add(new JLabel("Chapter13:"));
        panel.add(new LauncherButton(Program13_1.class.getSimpleName(),
                ()->Program13_1.main(null), this));
        panel.add(new LauncherButton(Program13_3.class.getSimpleName(),
                ()-> Program13_2.main(null), this));
        panel.add(new LauncherButton(Program13_3.class.getSimpleName(),
                ()->Program13_3.main(null), this));
        panel.add(new LauncherButton(Program13_4.class.getSimpleName(),
                ()->Program13_4.main(null), this));
        panel.add(new JLabel("Chapter14:"));
        panel.add(new LauncherButton(Program14_1.class.getSimpleName(),
                ()->Program14_1.main(null), this));
        panel.add(new JLabel("Chapter15:"));
        panel.add(new LauncherButton(Program15_1.class.getSimpleName(),
                ()->Program15_1.main(null), this));
        panel.add(new JLabel("Chapter16:"));
        panel.add(new LauncherButton(Program16_1.class.getSimpleName(),
                ()->Program16_1.main(null), this));
        panel.add(new LauncherButton(Program16_2.class.getSimpleName(),
                ()->Program16_2.main(null), this));

        // Make my panel scrollable
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(initWidth, initHeight));
        add(scrollPane);

        pack(); // let the frame fit to the size of the components inside
        setVisible(true);
    }

    public static void main(String[] args) {
        new ProgramSelector(Toolkit.getDefaultToolkit().getScreenSize().width / 5,
                (int) (Toolkit.getDefaultToolkit().getScreenSize().height / 1.5));
    }

    private static class LauncherButton extends JButton {
        public LauncherButton(String text, Runnable onClickAction, JFrame jFrame) {
            super(text);

            // Add button onclick callback.
            addActionListener(e -> {
                /*
                * I decide to end the whole program after the clicked program is finish, because I
                * found that my programs could not run multiply, possibly because of the OpenGL
                * context stuff.
                * */
                jFrame.dispose();
                System.out.println("Selector disposed.");

                System.out.println("Opening " + text + ".");
                onClickAction.run();

            });
        }
    }
}
