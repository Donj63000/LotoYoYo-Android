package org.example;  // chemin : src/main/java/org/example

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import net.miginfocom.swing.MigLayout;

public class Autogrille {

    public static List<Calcul.ScenarioCost> computeAutoGrilles(
            double[][] probList,
            List<List<Integer>> allowedChoices,
            int k)
    {
        return Calcul.kBestClosestScenarios(probList, allowedChoices, k);
    }

    public static JCheckBox makeReadonlyCheckBoxGreen(boolean selected) {
        JCheckBox cb = new JCheckBox();
        cb.setOpaque(true);

        if (selected) {
            cb.setBackground(new Color(0, 128, 0)); // vert
        } else {
            cb.setBackground(new Color(50, 50, 50)); // sombre par défaut
        }

        cb.setSelected(selected);

        cb.setEnabled(false);

        cb.setForeground(Color.WHITE);


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
            setBackground(new Color(44,44,44));

            leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBackground(new Color(44,44,44));

            rightPanel = new JPanel(new MigLayout(
                    "insets 8 8 8 8, wrap 2, gapx 8, gapy 4",
                    "[right]8[left]",
                    "[]"
            ));
            rightPanel.setBackground(new Color(44,44,44));

            add(leftPanel,  BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);

            createLeftPanel();
            createRightPanel();
            updateCalculs();
        }

        private void createLeftPanel() {
            JPanel pLeft = new JPanel();
            pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
            pLeft.setBackground(new Color(44,44,44));
            pLeft.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(new Color(80,80,80)),
                    "AutoGrille #"+ gridIndex,
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 12),
                    Color.WHITE
            ));

            JLabel lblTitle = new JLabel(numMatches + " Match(s)");
            lblTitle.setFont(new Font("Arial", Font.BOLD, 12));
            lblTitle.setForeground(Color.WHITE);
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
            lblStatsTitle.setForeground(Color.WHITE);
            rightPanel.add(lblStatsTitle, "span 2, align center, wrap");

            labelNbCombinaisons = addStatLine("Nombre de tickets différents", "1");
            labelCost           = addStatLine("Budget total (1 €/ticket)", "1 €");
            labelCoverage       = addStatLine("Part des scénarios couverts", "0.00 %");
            labelProbaCouvrir   = addStatLine("Chance de couvrir tous", "0.00 %");

            rightPanel.add(new JSeparator(), "span 2, growx, gaptop 6, gapbottom 4");

            JPanel distPanel = new JPanel(new BorderLayout(5,5));
            distPanel.setBackground(new Color(44,44,44));
            distPanel.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(new Color(80,80,80)),
                    "Probabilités (# bons résultats)",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 12),
                    Color.WHITE
            ));
            textDistribution = new JTextArea(6,30);
            textDistribution.setLineWrap(true);
            textDistribution.setWrapStyleWord(true);
            textDistribution.setEditable(false);
            textDistribution.setBackground(new Color(42,42,42));
            textDistribution.setForeground(new Color(220,220,220));
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

        private JLabel addStatLine(String key, String initValue){
            JLabel lblKey= new JLabel(key+" :");
            lblKey.setForeground(Color.WHITE);
            lblKey.setFont(new Font("Arial", Font.PLAIN, 12));
            rightPanel.add(lblKey, "align right");

            JLabel lblVal= new JLabel(initValue);
            lblVal.setForeground(Color.WHITE);
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
