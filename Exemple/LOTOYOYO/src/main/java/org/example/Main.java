package org.example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Main extends JFrame {

    private final JPanel topPanel;
    private final JPanel gridsContainer;
    private final JTextField tfNbMatches;
    private final JButton btnAdd;
    private final JPanel leftPanel;
    private final JLabel lblNb;
    private final JScrollPane scroll;
    private int gridCount = 0;

    public Main() {
        super("LoToYoYo – By Val [Java Version]");

        Theme.applyCurrentTheme();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Theme.BACKGROUND_COLOR);
        setLayout(new BorderLayout(8, 8));

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBackground(Theme.BACKGROUND_COLOR);
        topPanel.setBorder(new EmptyBorder(5, 8, 5, 8));

        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        leftPanel.setBackground(Theme.BACKGROUND_COLOR);

        JLabel lblTitle = new JLabel("LotoYoYo Generator [V1.1]");
        lblTitle.setForeground(Theme.PRIMARY_COLOR);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        leftPanel.add(lblTitle);

        lblNb = new JLabel("Matches :");
        lblNb.setFont(new Font("Arial", Font.PLAIN, 14));
        lblNb.setForeground(Theme.TEXT_COLOR);
        leftPanel.add(lblNb);

        tfNbMatches = new JTextField("7", 3);
        tfNbMatches.setFont(new Font("Arial", Font.PLAIN, 14));
        tfNbMatches.setBackground(Theme.INPUT_BACKGROUND);
        tfNbMatches.setForeground(Theme.TEXT_COLOR);
        leftPanel.add(tfNbMatches);

        btnAdd = new JButton("Ajouter");
        btnAdd.setFont(new Font("Arial", Font.PLAIN, 13));
        btnAdd.setBackground(Theme.PRIMARY_COLOR);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> onAddGrid());
        leftPanel.add(btnAdd);

        JButton btnHelp = new JButton("Aide");
        btnHelp.setFont(new Font("Arial", Font.PLAIN, 13));
        btnHelp.setBackground(Theme.PRIMARY_COLOR);
        btnHelp.setForeground(Color.WHITE);
        btnHelp.setFocusPainted(false);
        btnHelp.addActionListener(e -> {
            Aide aideFrame = new Aide();
            aideFrame.setVisible(true);
        });
        leftPanel.add(btnHelp);


        topPanel.add(leftPanel);

        ImageIcon rawIcon = new ImageIcon(getClass().getResource("/org/example/img.png"));
        Image scaled = rawIcon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaled));
        topPanel.add(logoLabel);

        add(topPanel, BorderLayout.NORTH);

        gridsContainer = new JPanel();
        gridsContainer.setLayout(new BoxLayout(gridsContainer, BoxLayout.Y_AXIS));
        gridsContainer.setBackground(Theme.CARD_BACKGROUND);

        scroll = new JScrollPane(gridsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(Theme.CARD_BACKGROUND);
        add(scroll, BorderLayout.CENTER);

        applyTheme();
    }

    private void onAddGrid() {
        gridCount++;
        int nb = 7;
        try {
            nb = Integer.parseInt(tfNbMatches.getText().trim());
        } catch (NumberFormatException ignored) {
        }

        final GridPanel[] holder = new GridPanel[1];
        holder[0] = new GridPanel(gridCount, nb, () -> {
            gridsContainer.remove(holder[0]);
            gridsContainer.revalidate();
            gridsContainer.repaint();
        });

        gridsContainer.add(holder[0]);
        gridsContainer.revalidate();
        gridsContainer.repaint();
    }

    private void applyTheme() {
        getContentPane().setBackground(Theme.BACKGROUND_COLOR);
        topPanel.setBackground(Theme.BACKGROUND_COLOR);
        leftPanel.setBackground(Theme.BACKGROUND_COLOR);
        lblNb.setForeground(Theme.TEXT_COLOR);
        tfNbMatches.setBackground(Theme.INPUT_BACKGROUND);
        tfNbMatches.setForeground(Theme.TEXT_COLOR);
        gridsContainer.setBackground(Theme.CARD_BACKGROUND);
        scroll.getViewport().setBackground(Theme.CARD_BACKGROUND);
        for (Component c : gridsContainer.getComponents()) {
            if (c instanceof GridPanel gp) {
                gp.refreshTheme();
            }
        }
        Autogrille.refreshOpenPanels();
        Aide.refreshOpenWindows();
        SwingUtilities.updateComponentTreeUI(this);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.applyDarkTheme();
            Main app = new Main();
            app.setVisible(true);
        });
    }
}
