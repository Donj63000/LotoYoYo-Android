package org.example;
import javax.swing.*;
import java.awt.*;
public class Theme {
    public static final Color BACKGROUND_COLOR = new Color(13, 17, 23);   // #0D1117
    public static final Color CARD_BACKGROUND  = new Color(22, 27, 34);   // #161B22
    public static final Color TEXT_COLOR       = new Color(230, 237, 243);// #E6EDF3
    public static final Color PRIMARY_COLOR    = new Color( 59,130,246);  // #3B82F6
    public static final Color DISABLED_COLOR   = new Color( 76, 86,100);  // #4C5664
    public static final Color SCROLLBAR_COLOR  = new Color( 66, 74, 83);  // #424A53
    public static final Color SCROLLBAR_HOVER  = new Color( 96,106,117);  // #606A75
    public static final Color DANGER_COLOR     = new Color(239, 68, 68);  // #EF4444
    public static void applyDarkTheme() {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch(Exception e) {
            e.printStackTrace();
        }
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.background", CARD_BACKGROUND);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        UIManager.put("Label.foreground", TEXT_COLOR);
        UIManager.put("Label.background", BACKGROUND_COLOR);
        UIManager.put("Button.background", BACKGROUND_COLOR);
        UIManager.put("Button.foreground", TEXT_COLOR);
        UIManager.put("Button.select", new Color(31,31,31));
        UIManager.put("Button.disabledText", DISABLED_COLOR);
        UIManager.put("ScrollBar.thumb", SCROLLBAR_COLOR);
        UIManager.put("ScrollBar.thumbDarkShadow", SCROLLBAR_COLOR);
        UIManager.put("ScrollBar.thumbHighlight", SCROLLBAR_HOVER);
        UIManager.put("ScrollBar.thumbShadow", SCROLLBAR_COLOR);
        UIManager.put("ScrollBar.track", BACKGROUND_COLOR);
        UIManager.put("ComboBox.background", new Color(42,42,42));
        UIManager.put("ComboBox.foreground", TEXT_COLOR);
        UIManager.put("TextField.background", new Color(42,42,42));
        UIManager.put("TextField.foreground", TEXT_COLOR);
        UIManager.put("TextField.caretForeground", TEXT_COLOR);
        UIManager.put("TextField.inactiveForeground", DISABLED_COLOR);
        SwingUtilities.invokeLater(() -> {
            for(Frame f : Frame.getFrames()) {
                SwingUtilities.updateComponentTreeUI(f);
            }
        });
    }

}
