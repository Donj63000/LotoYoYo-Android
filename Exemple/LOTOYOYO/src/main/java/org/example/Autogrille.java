package org.example;  // chemin : src/main/java/org/example

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.example.Theme;
import net.miginfocom.swing.MigLayout;

public class Autogrille {

    private static final List<ReadOnlyGridFrameWithStats> OPEN_PANELS = new ArrayList<>();


    public static List<Calcul.ScenarioCost> computeAutoGrilles(
            double[][] probList,
            List<List<Integer>> allowedChoices,
            int k)
    {
        return Calcul.kBestClosestScenarios(probList, allowedChoices, k);
    }

    static void register(ReadOnlyGridFrameWithStats p) {
        OPEN_PANELS.add(p);
    }

    static void unregister(ReadOnlyGridFrameWithStats p) {
        OPEN_PANELS.remove(p);
    }

    public static void refreshOpenPanels() {
        for (ReadOnlyGridFrameWithStats p : new ArrayList<>(OPEN_PANELS)) {
            p.applyTheme();
        }
    }

    public static JCheckBox makeReadonlyCheckBoxGreen(boolean selected) {
        JCheckBox cb = new JCheckBox();
        cb.setOpaque(true);

        if (selected) {
            cb.setBackground(new Color(0, 128, 0)); // vert
        } else {
            cb.setBackground(Theme.CARD_BACKGROUND); // sombre par défaut
        }

        cb.setSelected(selected);

        cb.setEnabled(false);

        cb.setForeground(Theme.TEXT_COLOR);


        cb.setFocusPainted(false);

        return cb;
    }


    public static class ReadOnlyGridFrameWithStats extends JPanel {

        private final int[]    scenarioTuple;
        private final int      gridIndex;
        private final double   costValue;
        private final double[][] probList;
        private final int      numMatches;

        private JPanel leftPanel;
        private JPanel rightPanel;


        private JTextArea textDistribution;

        private JLabel labelNbCombinaisons;
        private JLabel labelCost;
        private JLabel labelCoverage;
        private JLabel labelProbaCouvrir;
        private JLabel labelAtLeast1;
        private JLabel labelAtLeastHalf;
        private JLabel labelAtLeastAll;
        private JLabel labelUncovered;
        private JLabel labelWorstCase;
        private JLabel labelEfficiency;
        private JLabel labelMeanHits;
        private JLabel labelStdHits;
        private JLabel labelDisposition;
        private JLabel labelPossibilites;

        public ReadOnlyGridFrameWithStats(int[] scenarioTuple,
                                          int gridIndex,
                                          double costValue,
                                          double[][] probList)
        {
            this.scenarioTuple = scenarioTuple;
            this.gridIndex     = gridIndex;
            this.costValue     = costValue;
            this.probList      = probList;
            this.numMatches    = scenarioTuple.length;

            setLayout(new BorderLayout(8,8));
            setBorder(new LineBorder(new Color(80,80,80), 1));
            setBackground(Theme.CARD_BACKGROUND);

            leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBackground(Theme.CARD_BACKGROUND);

            rightPanel = new JPanel(new MigLayout(
                    "insets 8 8 8 8, wrap 2, gapx 8, gapy 4",
                    "[right]8[left]",
                    "[]"
            ));
            rightPanel.setBackground(Theme.CARD_BACKGROUND);

            add(leftPanel,  BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);

            createLeftPanel();
            createRightPanel();
            updateCalculs();
            Autogrille.register(this);
        }

        private void createLeftPanel() {
            JPanel pLeft = new JPanel();
            pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
            pLeft.setBackground(Theme.CARD_BACKGROUND);
            pLeft.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(new Color(80,80,80)),
                    "AutoGrille #"+ gridIndex,
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 12),
                    Theme.TEXT_COLOR
            ));

            JLabel lblTitle = new JLabel(numMatches + " Match(s)");
            lblTitle.setFont(new Font("Arial", Font.BOLD, 12));
            lblTitle.setForeground(Theme.TEXT_COLOR);
            pLeft.add(lblTitle);

            double probEst = Math.exp(-costValue);
            String infoStr = String.format("Cost = %.4f, Prob ~ %.2f %%", costValue, probEst*100);
            JLabel lblInfo = new JLabel(infoStr);
            lblInfo.setForeground(Color.CYAN);
            lblInfo.setFont(new Font("Arial", Font.PLAIN, 11));
            pLeft.add(lblInfo);

            Box header = Box.createHorizontalBox();
            header.add(new JLabel("Match")); header.add(Box.createHorizontalStrut(20));
            header.add(new JLabel("1"));     header.add(Box.createHorizontalStrut(20));
            header.add(new JLabel("N"));     header.add(Box.createHorizontalStrut(20));
            header.add(new JLabel("2"));
            pLeft.add(header);

            for(int i=0; i<numMatches; i++){
                Box row = Box.createHorizontalBox();
                row.add(new JLabel("Match "+(i+1)+"  "));

                boolean sel1= (scenarioTuple[i]==0);
                boolean selN= (scenarioTuple[i]==1);
                boolean sel2= (scenarioTuple[i]==2);

                JCheckBox cb1= makeReadonlyCheckBoxGreen(sel1);
                JCheckBox cbN= makeReadonlyCheckBoxGreen(selN);
                JCheckBox cb2= makeReadonlyCheckBoxGreen(sel2);

                row.add(cb1); row.add(Box.createHorizontalStrut(10));
                row.add(cbN); row.add(Box.createHorizontalStrut(10));
                row.add(cb2);

                pLeft.add(row);
            }

            leftPanel.add(pLeft);
        }

        private void createRightPanel() {
            JLabel lblStatsTitle = new JLabel("Statistiques");
            lblStatsTitle.setFont(new Font("Arial", Font.BOLD, 12));
            lblStatsTitle.setForeground(Theme.TEXT_COLOR);
            rightPanel.add(lblStatsTitle, "span 2, align center, wrap");

            labelNbCombinaisons = addStatLine("Nombre de tickets différents", "1");
            labelCost           = addStatLine("Budget total (1 €/ticket)", "1 €");
            labelCoverage       = addStatLine("Part des scénarios couverts", "0.00 %");
            labelProbaCouvrir   = addStatLine("Chance de couvrir tous", "0.00 %");

            rightPanel.add(new JSeparator(), "span 2, growx, gaptop 6, gapbottom 4");

            JPanel distPanel = new JPanel(new BorderLayout(5,5));
            distPanel.setBackground(Theme.CARD_BACKGROUND);
            distPanel.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(new Color(80,80,80)),
                    "Probabilités (# bons résultats)",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 12),
                    Theme.TEXT_COLOR
            ));
            textDistribution = new JTextArea(6,30);
            textDistribution.setLineWrap(true);
            textDistribution.setWrapStyleWord(true);
            textDistribution.setEditable(false);
            textDistribution.setBackground(Theme.INPUT_BACKGROUND);
            textDistribution.setForeground(Theme.TEXT_COLOR);
            textDistribution.setBorder(new EmptyBorder(4,4,4,4));

            JScrollPane distScroll = new JScrollPane(textDistribution);
            distScroll.setBorder(BorderFactory.createEmptyBorder());
            distPanel.add(distScroll, BorderLayout.CENTER);
            rightPanel.add(distPanel, "span 2, growx, wrap, gaptop 6");

            labelAtLeast1    = addStatLine("Chance d’avoir au moins 1 bon", "0.00 %");
            labelAtLeastHalf = addStatLine("Chance d’avoir au moins la moitié", "0.00 %");
            labelAtLeastAll  = addStatLine("Chance d’être 100 % juste", "0.00 %");
            labelUncovered   = addStatLine("Scénarios manquants", "0");
            labelWorstCase   = addStatLine("Pronostics corrects (pire cas)", "0");
            labelEfficiency  = addStatLine("Efficacité", "0.0000");
            labelMeanHits    = addStatLine("Moyenne bons pronostics", "0.00");
            labelStdHits     = addStatLine("Écart-type bons pronostics", "0.00");
            labelDisposition = addStatLine("Configurations possibles", "1");
            labelPossibilites= addStatLine("Façons de fixer cases sélectionnées", "1");
        }

        private void updateCalculs() {
            DecimalFormat pctFmt= new DecimalFormat("0.00");
            DecimalFormat stdFmt= new DecimalFormat("0.0000");

            int M= numMatches;
            labelNbCombinaisons.setText("1");
            labelCost.setText("1 €");

            double coverageFrac= (1.0 / Math.pow(3,M))*100.0;
            labelCoverage.setText( String.format("%.2f %%", coverageFrac) );

            double scenarioProb=1.0;
            for(int i=0; i<M; i++){
                int out= scenarioTuple[i];
                scenarioProb*= probList[i][out];
            }
            labelProbaCouvrir.setText( String.format("%.2f %%", scenarioProb*100) );

            double[] pMatch= new double[M];
            for(int i=0; i<M; i++){
                int out= scenarioTuple[i];
                pMatch[i]= probList[i][out];
            }
            double[] dist= Calcul.buildDistribution(pMatch);

            StringBuilder sb= new StringBuilder();
            for(int k=0; k<=M; k++){
                sb.append(String.format("%d bon(s) : %5.2f %%\n", k, dist[k]*100));
            }
            textDistribution.setText(sb.toString());

            double pAtLeast1= 1.0 - dist[0];
            int half= (M+1)/2;
            double pHalf=0.0;
            for(int k=half; k<= M; k++){
                pHalf+= dist[k];
            }
            double pAll= dist[M];

            labelAtLeast1.setText( String.format("%.2f %%", pAtLeast1*100) );
            labelAtLeastHalf.setText( String.format("%.2f %%", pHalf*100) );
            labelAtLeastAll.setText( String.format("%.2f %%", pAll*100) );

            int uncovered= (int)Math.pow(3,M)-1;
            labelUncovered.setText( String.valueOf(uncovered) );

            labelWorstCase.setText("0");

            double eff= coverageFrac/100.0;
            labelEfficiency.setText( String.format("%.4f", eff) );

            double[] stats= Calcul.computeStats(dist);
            labelMeanHits.setText( String.format("%.2f", stats[0]) );
            labelStdHits.setText( String.format("%.2f", stats[1]) );

            labelDisposition.setText("1");

            long poss= comb(3*M, M);
            labelPossibilites.setText( String.valueOf(poss) );
        }

        void applyTheme() {
            setBackground(Theme.CARD_BACKGROUND);
            leftPanel.setBackground(Theme.CARD_BACKGROUND);
            rightPanel.setBackground(Theme.CARD_BACKGROUND);
            textDistribution.setBackground(Theme.INPUT_BACKGROUND);
            textDistribution.setForeground(Theme.TEXT_COLOR);

            JLabel[] labels = {labelNbCombinaisons, labelCost, labelCoverage,
                    labelProbaCouvrir, labelAtLeast1, labelAtLeastHalf, labelAtLeastAll,
                    labelUncovered, labelWorstCase, labelEfficiency,
                    labelMeanHits, labelStdHits, labelDisposition, labelPossibilites};
            for (JLabel l : labels) {
                if (l != null) l.setForeground(Theme.TEXT_COLOR);
            }

            SwingUtilities.updateComponentTreeUI(this);
        }

        private JLabel addStatLine(String key, String initValue){
            JLabel lblKey= new JLabel(key+" :");
            lblKey.setForeground(Theme.TEXT_COLOR);
            lblKey.setFont(new Font("Arial", Font.PLAIN, 12));
            rightPanel.add(lblKey, "align right");

            JLabel lblVal= new JLabel(initValue);
            lblVal.setForeground(Theme.TEXT_COLOR);
            lblVal.setFont(new Font("Arial", Font.BOLD, 12));
            rightPanel.add(lblVal, "align left, wrap");
            return lblVal;
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
    }
}
