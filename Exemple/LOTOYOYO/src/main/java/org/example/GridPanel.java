package org.example;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class GridPanel extends JPanel {
    private final int gridIndex;
    private final int nMatches;
    private final Runnable onRemove; // callback pour supprimer ce panel

    private final double[][] matchOdds;
    private final double[][] matchProb;
    private final List<JCheckBox[]> checkRows = new ArrayList<>();
    private final List<JTextField[]> oddsRows  = new ArrayList<>();

    private JLabel lblTickets, lblBudget, lblCover, lblAll;
    private JTextArea txtDist;
    private JLabel lblAtLeast1, lblAtLeastHalf, lblAtLeastAll;
    private JLabel lblScenMiss, lblWorst, lblEff, lblMean, lblStd, lblConfigs, lblForces;

    public GridPanel(int index, int nMatches, Runnable onRemove) {
        this.gridIndex = index;
        this.nMatches  = nMatches;
        this.onRemove  = onRemove;

        setBackground(Theme.CARD_BACKGROUND);
        setLayout(new BorderLayout(5,5));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100,100,100)),
                new EmptyBorder(10,10,10,10)
        ));

        matchOdds = new double[nMatches][3];
        matchProb = new double[nMatches][3];
        for(int i=0; i<nMatches; i++){
            matchOdds[i][0] = 2.5;
            matchOdds[i][1] = 3.0;
            matchOdds[i][2] = 2.7;
            matchProb[i]    = Calcul.oddsToProb(2.5,3.0,2.7);
        }

        JPanel leftPanel   = buildLeftPanel();
        JPanel cotesPanel  = buildCotesPanel();
        JPanel statsPanel  = buildStatsPanel();
        JPanel bottomPanel = buildBottomPanel();

        JPanel leftWrapper = new JPanel(new BorderLayout(0,8));
        leftWrapper.setBackground(Theme.CARD_BACKGROUND);
        leftWrapper.add(leftPanel, BorderLayout.NORTH);
        leftWrapper.add(cotesPanel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftWrapper,
                statsPanel
        );
        split.setResizeWeight(0.35);
        split.setOneTouchExpandable(true);
        split.setDividerSize(6);
        split.setBackground(Theme.CARD_BACKGROUND);

        add(split, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.CARD_BACKGROUND);

        Box rowTitle = Box.createHorizontalBox();
        JLabel lblTitle = makeLabel(
                String.format("Grille #%d – %d match(s)", gridIndex, nMatches),
                13, Font.BOLD
        );
        rowTitle.add(lblTitle);
        rowTitle.add(Box.createHorizontalGlue());

        JButton btnRemove = new JButton("Supprimer");
        btnRemove.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRemove.setBackground(Theme.DANGER_COLOR);
        btnRemove.setForeground(Color.WHITE);
        btnRemove.setFocusPainted(false);
        btnRemove.addActionListener(e-> {
            if(onRemove!=null) onRemove.run();
        });
        rowTitle.add(btnRemove);

        p.add(rowTitle);
        p.add(Box.createVerticalStrut(8));

        Box rowHeader = Box.createHorizontalBox();
        rowHeader.add(Box.createHorizontalStrut(15));
        rowHeader.add(makeLabel("Mch", 11, Font.BOLD));
        rowHeader.add(Box.createHorizontalStrut(15));
        rowHeader.add(makeLabel("1", 11, Font.BOLD));
        rowHeader.add(Box.createHorizontalStrut(15));
        rowHeader.add(makeLabel("N", 11, Font.BOLD));
        rowHeader.add(Box.createHorizontalStrut(15));
        rowHeader.add(makeLabel("2", 11, Font.BOLD));
        p.add(rowHeader);

        for(int i=0; i<nMatches; i++){
            Box row = Box.createHorizontalBox();
            row.add(Box.createHorizontalStrut(15));
            row.add(makeLabel("M"+(i+1), 11, Font.PLAIN));
            row.add(Box.createHorizontalStrut(15));

            JCheckBox cb1 = new JCheckBox();
            JCheckBox cbN = new JCheckBox();
            JCheckBox cb2 = new JCheckBox();

            styleCheckBox(cb1, new Color(0, 128, 0));   // Vert
            styleCheckBox(cbN, new Color(255, 140, 0)); // Orange
            styleCheckBox(cb2, new Color(200, 0, 0));   // Rouge

            row.add(cb1);
            row.add(Box.createHorizontalStrut(8));
            row.add(cbN);
            row.add(Box.createHorizontalStrut(8));
            row.add(cb2);

            p.add(row);
            p.add(Box.createVerticalStrut(2));

            checkRows.add(new JCheckBox[]{cb1, cbN, cb2});
        }
        return p;
    }

    private void styleCheckBox(JCheckBox cb, Color selectedColor) {
        final Color defaultBG = Theme.CARD_BACKGROUND;

        cb.setBackground(defaultBG);
        cb.setForeground(Theme.TEXT_COLOR);
        cb.setFocusPainted(false);

        cb.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                cb.setBackground(selectedColor);
            } else {
                cb.setBackground(defaultBG);
            }
        });
    }

    private JPanel buildCotesPanel() {
        JPanel p = new JPanel(new MigLayout(
                "wrap 4",
                "[right][grow,fill][grow,fill][grow,fill]",
                "[]10[]"
        ));
        p.setBackground(Theme.CARD_BACKGROUND);
        p.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(100,100,100)),
                "Cotes (1/N/2)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial",Font.BOLD,12),
                Theme.TEXT_COLOR
        ));

        for(int i=0; i<nMatches; i++){
            JLabel lbl = makeLabel("M"+(i+1)+":", 12, Font.BOLD);

            JTextField f1 = new JTextField(""+matchOdds[i][0], 4);
            JTextField fN = new JTextField(""+matchOdds[i][1], 4);
            JTextField f2 = new JTextField(""+matchOdds[i][2], 4);
            styleField(f1);
            styleField(fN);
            styleField(f2);

            p.add(lbl);
            p.add(f1);
            p.add(fN);
            p.add(f2);

            oddsRows.add(new JTextField[]{f1, fN, f2});
        }

        JButton btnApply = new JButton("Appliquer cotes");
        btnApply.setFont(new Font("Arial", Font.PLAIN, 12));
        btnApply.setBackground(Theme.PRIMARY_COLOR);
        btnApply.setForeground(Color.WHITE);
        btnApply.setFocusPainted(false);
        btnApply.addActionListener(e->applyOdds());

        p.add(btnApply, "span 4, align right");
        return p;
    }

    private JPanel buildStatsPanel() {
        JPanel p = new JPanel(new MigLayout(
                "insets 8 10 8 10, wrap 2, gapx 12, gapy 4",
                "[right]10[left]",
                "[]"
        ));
        p.setBackground(Theme.CARD_BACKGROUND);
        p.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(100, 100, 100)),
                "Statistiques",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Inter", Font.BOLD, 14),
                Theme.PRIMARY_COLOR
        ));

        lblTickets = addStatLine(p, "Tickets",      "0");
        lblBudget  = addStatLine(p, "Budget",       "0 €");
        lblCover   = addStatLine(p, "Couverture",   "0,00 %");
        lblAll     = addStatLine(p, "Couvrir tous", "0,00 %");

        p.add(new JSeparator(), "span 2, growx, gaptop 6, gapbottom 4");

        JPanel pDist = new JPanel(new BorderLayout(6,6));
        pDist.setBackground(Theme.CARD_BACKGROUND);
        pDist.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(new Color(80,80,80)),
                "Distribution",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                Theme.TEXT_COLOR
        ));

        txtDist = new JTextArea(5, 28);
        txtDist.setEditable(false);
        txtDist.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        txtDist.setBackground(Theme.CARD_BACKGROUND);
        txtDist.setForeground(Theme.TEXT_COLOR);
        txtDist.setBorder(new EmptyBorder(4,4,4,4));
        txtDist.setLineWrap(false);

        JScrollPane distScroll = new JScrollPane(txtDist,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        distScroll.setBorder(BorderFactory.createEmptyBorder());
        distScroll.getVerticalScrollBar().setUnitIncrement(16);
        pDist.add(distScroll, BorderLayout.CENTER);

        p.add(pDist, "span 2, growx, gaptop 4");

        lblAtLeast1   = addStatLine(p, "≥1 bon",   "—");
        lblAtLeastHalf= addStatLine(p, "≥X bons",  "—");
        lblAtLeastAll = addStatLine(p, "100 %",    "—");

        p.add(new JSeparator(), "span 2, growx, gaptop 6, gapbottom 4");

        lblScenMiss = addStatLine(p, "Manquants",  "—");
        lblWorst    = addStatLine(p, "Worst-case", "—");
        lblEff      = addStatLine(p, "Efficacité", "—");
        lblMean     = addStatLine(p, "Moyenne",    "—");
        lblStd      = addStatLine(p, "Écart-type", "—");
        lblConfigs  = addStatLine(p, "Configs",    "—");
        lblForces   = addStatLine(p, "Forcées",    "—");

        return p;
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8));
        p.setBackground(Theme.CARD_BACKGROUND);

        JButton btnCalc = new JButton("Calculer");
        btnCalc.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCalc.setBackground(Theme.PRIMARY_COLOR);
        btnCalc.setForeground(Color.WHITE);
        btnCalc.addActionListener(e->calculate());
        p.add(btnCalc);

        JButton btnAuto = new JButton("Auto-Grille");
        btnAuto.setFont(new Font("Arial", Font.PLAIN, 12));
        btnAuto.setBackground(Theme.PRIMARY_COLOR);
        btnAuto.setForeground(Color.WHITE);
        btnAuto.addActionListener(e->autoGrille());
        p.add(btnAuto);

        return p;
    }

    private void applyOdds(){
        try {
            for(int i=0; i<nMatches; i++){
                JTextField[] fields = oddsRows.get(i);
                double c1 = Double.parseDouble(fields[0].getText().replace(",", "."));
                double cN = Double.parseDouble(fields[1].getText().replace(",", "."));
                double c2 = Double.parseDouble(fields[2].getText().replace(",", "."));

                matchOdds[i][0] = c1;
                matchOdds[i][1] = cN;
                matchOdds[i][2] = c2;
                matchProb[i]    = Calcul.oddsToProb(c1,cN,c2);
            }
            JOptionPane.showMessageDialog(this,
                    "Cotes appliquées et probabilités mises à jour.",
                    "Cotes",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,
                    "Cote invalide !",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void calculate() {
        DecimalFormat pctFmt  = new DecimalFormat("0.00");
        DecimalFormat stdFmt  = new DecimalFormat("0.0000");

        // combos
        int combos = 1;
        for (JCheckBox[] row : checkRows) {
            int sel = 0;
            for (JCheckBox cb : row) if (cb.isSelected()) sel++;
            if (sel == 0) {
                combos=0;
                break;
            }
            combos *= sel;
        }
        lblTickets.setText(String.valueOf(combos));
        lblBudget.setText(combos + " €");

        double totalScen = Math.pow(3, nMatches);
        double covPct    = combos * 100.0 / totalScen;
        lblCover.setText(pctFmt.format(covPct) + " %");

        double[] pMatch = new double[nMatches];
        double pAll = 1.0;
        for (int i = 0; i < nMatches; i++){
            double pSel=0.0;
            JCheckBox[] row= checkRows.get(i);
            if(row[0].isSelected()) pSel+= matchProb[i][0];
            if(row[1].isSelected()) pSel+= matchProb[i][1];
            if(row[2].isSelected()) pSel+= matchProb[i][2];
            pMatch[i]= pSel;
            pAll    *= pSel;
        }
        lblAll.setText(pctFmt.format(pAll*100)+" %");

        double[] dist= Calcul.buildDistribution(pMatch);
        StringBuilder sb= new StringBuilder();
        for(int k=0; k<= nMatches; k++){
            sb.append(String.format("%2d bon(s) : %6.2f %%", k, dist[k]*100));
            if(k%2==1 || k==nMatches){
                sb.append("\n");
            } else {
                sb.append("     ");
            }
        }
        txtDist.setText(sb.toString());

        double pAtLeast1= 1.0- dist[0];
        int half= (nMatches+1)/2;
        double pAtLeastHalf=0;
        for(int k=half; k<=nMatches; k++){
            pAtLeastHalf+= dist[k];
        }
        lblAtLeast1.setText(pctFmt.format(pAtLeast1*100)+" %");
        lblAtLeastHalf.setText(pctFmt.format(pAtLeastHalf*100)+" %");
        lblAtLeastAll.setText(pctFmt.format(dist[nMatches]*100)+" %");

        double efficiency= (combos>0? (covPct/100.0)/ combos :0);
        lblEff.setText(stdFmt.format(efficiency));

        double[] stats= Calcul.computeStats(dist);
        lblMean.setText(pctFmt.format(stats[0]));
        lblStd.setText(pctFmt.format(stats[1]));

        int forced=0;
        for(JCheckBox[] row: checkRows){
            for(JCheckBox cb: row){
                if(cb.isSelected()) forced++;
            }
        }
        int totalCases= 3*nMatches;
        int freeCases= totalCases- forced;
        long c= (long)Math.pow(2, freeCases);
        lblConfigs.setText(String.valueOf(c));

        long combForced= comb(totalCases, forced);
        lblForces.setText(String.valueOf(combForced));

        if(nMatches<=12){
            int uncovered= countUncoveredDirect();
            int worstHit= worstCaseHitsDirect();
            lblScenMiss.setText(String.valueOf(uncovered));
            lblWorst.setText(String.valueOf(worstHit));
        } else {
            lblScenMiss.setText("???");
            lblWorst.setText("???");
        }
    }

    private void autoGrille(){
        try{
            int combos=1;
            for(JCheckBox[] row: checkRows){
                int sel=0;
                for(JCheckBox cb: row) if(cb.isSelected()) sel++;
                if(sel==0){ combos=0; break; }
                combos*= sel;
            }
            if(combos<=0){
                JOptionPane.showMessageDialog(this,
                        "Aucun ticket n'est possible (0 combos).",
                        "Auto-Grille",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int k= combos;
            double[][] probList= this.matchProb;

            List<List<Integer>> allowedChoices= new ArrayList<>();
            for(int i=0; i<nMatches; i++){
                JCheckBox[] row= checkRows.get(i);
                List<Integer> singleAllowed= new ArrayList<>();
                if(row[0].isSelected()) singleAllowed.add(0);
                if(row[1].isSelected()) singleAllowed.add(1);
                if(row[2].isSelected()) singleAllowed.add(2);
                allowedChoices.add(singleAllowed);
            }
            List<Calcul.ScenarioCost> autoScens=
                    Autogrille.computeAutoGrilles(probList, allowedChoices, k);
            if(autoScens.isEmpty()){
                JOptionPane.showMessageDialog(this,
                        "Aucune auto-grille générée (cases cochées ?).",
                        "Auto-Grille",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            Theme.applyCurrentTheme();
            JFrame autoFrame= new JFrame("Auto-Grilles Générées (X = " + combos + ")");
            autoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            autoFrame.setSize(900,600);
            autoFrame.setLocationRelativeTo(null);

            JPanel mainPanel= new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBackground(Theme.CARD_BACKGROUND);

            int idx=1;
            java.util.List<Autogrille.ReadOnlyGridFrameWithStats> panels = new java.util.ArrayList<>();
            for(Calcul.ScenarioCost sc: autoScens){
                Autogrille.ReadOnlyGridFrameWithStats roPanel=
                        new Autogrille.ReadOnlyGridFrameWithStats(
                                sc.scenario, idx, sc.cost, probList
                        );
                roPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(80,80,80)),
                        new EmptyBorder(8,8,8,8)
                ));
                panels.add(roPanel);
                mainPanel.add(roPanel);
                idx++;
            }
            JScrollPane sp= new JScrollPane(mainPanel);
            sp.setBackground(Theme.CARD_BACKGROUND);
            autoFrame.add(sp, BorderLayout.CENTER);
            autoFrame.addWindowListener(new java.awt.event.WindowAdapter(){
                @Override public void windowClosed(java.awt.event.WindowEvent e){
                    for(var p: panels) Autogrille.unregister(p);
                }
            });
            autoFrame.setVisible(true);

        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la génération d'auto-grilles:\n" + e.getMessage(),
                    "Auto-Grille",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private long comb(int n, int k){
        if(k>n) return 0;
        if(k>n-k) k=n-k;
        long res=1;
        for(int i=0; i<k; i++){
            res*= (n-i);
            res/= (i+1);
        }
        return res;
    }

    private int countUncoveredDirect(){
        List<int[]> tickets= enumerateTickets();
        int total= (int)Math.pow(3,nMatches);
        boolean[] covered= new boolean[total];
        for(int[] t: tickets){
            int code= Calcul.ticketToInt(t);
            covered[code]= true;
        }
        int nb=0;
        for(boolean b: covered){
            if(!b) nb++;
        }
        return nb;
    }

    private int worstCaseHitsDirect(){
        List<int[]> tickets= enumerateTickets();
        if(tickets.isEmpty()) return 0;
        int total= (int)Math.pow(3,nMatches);
        if(tickets.size()== total) return nMatches;

        int worst= nMatches;
        for(int code=0; code< total; code++){
            int[] scen= Calcul.intToScen(code,nMatches);
            int bestLocal=0;
            for(int[] t: tickets){
                int hits=0;
                for(int i=0; i<nMatches; i++){
                    if(t[i]== scen[i]) hits++;
                }
                if(hits>bestLocal) bestLocal= hits;
            }
            if(bestLocal<worst) worst=bestLocal;
            if(worst==0) break;
        }
        return worst;
    }

    private List<int[]> enumerateTickets(){
        List<int[]> out= new ArrayList<>();
        List<int[]> choices= new ArrayList<>();
        for(JCheckBox[] arr: checkRows){
            List<Integer> issues= new ArrayList<>();
            if(arr[0].isSelected()) issues.add(0);
            if(arr[1].isSelected()) issues.add(1);
            if(arr[2].isSelected()) issues.add(2);
            if(issues.isEmpty()) return List.of();
            int[] c= new int[issues.size()];
            for(int i=0; i<c.length; i++){
                c[i]= issues.get(i);
            }
            choices.add(c);
        }
        int[] current= new int[nMatches];
        cartesian(choices,0,current,out);
        return out;
    }

    private void cartesian(List<int[]> choices, int idx, int[] curr, List<int[]> out){
        if(idx>= choices.size()){
            out.add(curr.clone());
            return;
        }
        for(int val: choices.get(idx)){
            curr[idx]= val;
            cartesian(choices, idx+1, curr, out);
        }
    }

    private void styleField(JTextField f){
        f.setFont(new Font("Arial", Font.PLAIN, 12));
        f.setBackground(Theme.INPUT_BACKGROUND);
        f.setForeground(Theme.TEXT_COLOR);
        f.setBorder(new EmptyBorder(2,4,2,4));
    }

    private JLabel makeLabel(String text, int fontSize, int style){
        JLabel lbl= new JLabel(text);
        lbl.setForeground(Theme.TEXT_COLOR);
        lbl.setFont(new Font("Arial", style, fontSize));
        return lbl;
    }

    private JLabel addStatLine(JPanel container, String name, String init){
        container.add(makeLabel(name+" :", 12, Font.PLAIN), "align right");
        JLabel val= makeLabel(init, 12, Font.BOLD);
        container.add(val, "align left");
        return val;
    }

    public void refreshTheme() {
        setBackground(Theme.CARD_BACKGROUND);
        for (JCheckBox[] row : checkRows) {
            for (JCheckBox cb : row) {
                cb.setBackground(Theme.CARD_BACKGROUND);
                cb.setForeground(Theme.TEXT_COLOR);
            }
        }
        for (JTextField[] row : oddsRows) {
            for (JTextField f : row) {
                f.setBackground(Theme.INPUT_BACKGROUND);
                f.setForeground(Theme.TEXT_COLOR);
            }
        }
        txtDist.setBackground(Theme.CARD_BACKGROUND);
        txtDist.setForeground(Theme.TEXT_COLOR);

        JLabel[] labels = {lblTickets, lblBudget, lblCover, lblAll,
                lblAtLeast1, lblAtLeastHalf, lblAtLeastAll,
                lblScenMiss, lblWorst, lblEff, lblMean, lblStd,
                lblConfigs, lblForces};
        for (JLabel lbl : labels) {
            if (lbl != null) lbl.setForeground(Theme.TEXT_COLOR);
        }

        SwingUtilities.updateComponentTreeUI(this);
    }
}
