package org.example;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class Aide extends JFrame {

    private static final List<Aide> OPEN_AIDES = new ArrayList<>();

    private JPanel mainPanel;
    private JTextPane aideTextPane;
    private JLabel lblTitle;

    public Aide() {
        super("Aide - LotoYoYo");

        // Appliquer le thème courant
        Theme.applyCurrentTheme();

        // Configuration de base de la fenêtre
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND_COLOR);
        mainPanel = new JPanel(new MigLayout(
                "insets 12, fill",
                "[grow]",
                "[][]"
        ));
        mainPanel.setBackground(Theme.BACKGROUND_COLOR);

        lblTitle = new JLabel("Guide d'utilisation de LotoYoYo");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(Theme.PRIMARY_COLOR);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainPanel.add(lblTitle, "center, wrap");

        aideTextPane = new JTextPane();
        aideTextPane.setContentType("text/html");
        aideTextPane.setEditable(false);
        aideTextPane.setBackground(Theme.CARD_BACKGROUND);
        aideTextPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        aideTextPane.setText(getHelpTextHtml());

        aideTextPane.setForeground(Theme.TEXT_COLOR);
        aideTextPane.setCaretColor(Theme.TEXT_COLOR);

        JScrollPane scrollPane = new JScrollPane(aideTextPane);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, "grow, push");

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        OPEN_AIDES.add(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                OPEN_AIDES.remove(Aide.this);
            }
        });
    }

    static void refreshOpenWindows() {
        for (Aide a : new ArrayList<>(OPEN_AIDES)) {
            a.applyTheme();
        }
    }

    void applyTheme() {
        getContentPane().setBackground(Theme.BACKGROUND_COLOR);
        mainPanel.setBackground(Theme.BACKGROUND_COLOR);
        lblTitle.setForeground(Theme.PRIMARY_COLOR);
        aideTextPane.setBackground(Theme.CARD_BACKGROUND);
        aideTextPane.setForeground(Theme.TEXT_COLOR);
        aideTextPane.setCaretColor(Theme.TEXT_COLOR);
        SwingUtilities.updateComponentTreeUI(this);
    }

    private String getHelpTextHtml() {
        return
                "<html>" +
                        "<body style='font-family: Arial; font-size: 14px; color: #E6EDF3; background-color: #161B22;'>" +

                        "<p style='text-align:center;'>" +
                        "Logiciel développé par <b>Valentin G.</b> (<i>nodig63@gmail.com</i>), avec l’assistance d’une IA (JetBrains)<br/>" +
                        "<br/>" +
                        "Guide complet d’utilisation du logiciel LotoYoYo" +
                        "</p>" +

                        "<span style='color: #FFA500; font-size: 16pt; font-weight: bold;'>Introduction</span><br/>" +
                        "LotoYoYo est un logiciel professionnel conçu pour la génération, l’analyse détaillée " +
                        "et l’optimisation de grilles de pronostics sportifs. Chaque match présente trois résultats potentiels :<br/><br/>" +
                        "1 : Victoire de l’équipe évoluant à domicile.<br/>" +
                        "N : Match nul entre les deux équipes.<br/>" +
                        "2 : Victoire de l’équipe évoluant à l’extérieur.<br/><br/>" +
                        "L’utilisateur peut sélectionner un ou plusieurs résultats par match afin d’améliorer ses chances " +
                        "de gain en augmentant la couverture des scénarios possibles. Cette flexibilité entraîne cependant " +
                        "une hausse exponentielle du nombre de tickets nécessaires, et donc du coût global de la mise.<br/><br/>" +

                        "<span style='color: #FFA500; font-size: 15pt; font-weight: bold;'>1. Principe général de fonctionnement</span><br/>" +
                        "Le logiciel fonctionne selon les principes suivants :<br/><br/>" +
                        "<b>Choix initial :</b> Vous déterminez, pour chaque match, le ou les résultats que vous jugez les plus probables.<br/>" +
                        "<b>Gestion du risque :</b> Sélectionner plusieurs résultats pour un même match diminue le risque d’erreur, " +
                        "mais augmente le nombre total de combinaisons (tickets).<br/>" +
                        "<b>Optimisation stratégique :</b> Une utilisation raisonnable des sélections multiples permet un équilibre " +
                        "entre couverture des scénarios et budget engagé.<br/><br/>" +

                        "<span style='color: #FFA500; font-size: 15pt; font-weight: bold;'>2. Remplissage détaillé d’une grille de pronostics</span><br/>" +
                        "Voici les étapes précises pour créer et compléter efficacement vos grilles :<br/><br/>" +
                        "<b>Indiquez le nombre de matchs :</b><br/>" +
                        "Renseignez le nombre total de rencontres que vous souhaitez inclure dans votre pronostic dans " +
                        "le champ « Matches » (par exemple, 7).<br/><br/>" +
                        "<b>Génération d’une grille :</b><br/>" +
                        "Cliquez sur le bouton « Ajouter » afin de créer une grille vide, prête à être remplie.<br/><br/>" +
                        "<b>Sélectionnez les résultats souhaités :</b><br/>" +
                        "Pour chaque match, cochez l’une ou plusieurs des cases suivantes :<br/>" +
                        "• 1 (Victoire domicile)<br/>" +
                        "• N (Match nul)<br/>" +
                        "• 2 (Victoire extérieur)<br/><br/>" +
                        "<b>Saisissez les cotes des matchs :</b><br/>" +
                        "Dans la section « Cotes », entrez les cotes proposées par votre bookmaker (ex : 2.5 / 3.0 / 2.7).<br/>" +
                        "Cliquez ensuite sur « Appliquer cotes » pour actualiser automatiquement les probabilités " +
                        "associées à chaque résultat.<br/><br/>" +

                        "<span style='color: #FFA500; font-size: 15pt; font-weight: bold;'>3. Indicateurs statistiques avancés</span><br/>" +
                        "Lorsque vous cliquez sur « Calculer », LotoYoYo vous fournit une analyse complète, incluant :<br/><br/>" +

                        "<b>• Nombre total de Tickets :</b><br/>" +
                        "Le nombre exact de combinaisons issues de vos choix. " +
                        "On multiplie le nombre d’options cochées pour chaque match.<br/><br/>" +

                        "<b>• Budget total nécessaire :</b><br/>" +
                        "Le coût total est le produit du nombre de tickets par le prix unitaire d’un ticket (souvent 1€).<br/><br/>" +

                        "<b>• Couverture des scénarios (%) :</b><br/>" +
                        "Pourcentage de tous les scénarios théoriques (3^nombre de matchs) que vous couvrez au moins une fois.<br/><br/>" +

                        "<b>• Probabilité de couvrir le scénario exact :</b><br/>" +
                        "Estime la probabilité que le « vrai » scénario final se trouve parmi vos tickets, " +
                        "en se basant sur les probabilités déduites des cotes.<br/><br/>" +

                        "<b>• Distribution des bons pronostics :</b><br/>" +
                        "Histogramme montrant vos chances d’obtenir 0,1,2,..., jusqu’à tous les pronostics exacts.<br/><br/>" +

                        "<b>• Probabilité d’obtenir au moins un résultat correct (≥1 bon) :</b><br/>" +
                        "Chance d’avoir au moins un match pronostiqué correctement.<br/><br/>" +

                        "<b>• Probabilité d’obtenir au moins la moitié des résultats corrects (≥ moitié bons) :</b><br/>" +
                        "Permet d’évaluer la qualité globale de votre grille en cas d’incertitude modérée.<br/><br/>" +

                        "<b>• Worst-case (scénario le moins favorable) :</b><br/>" +
                        "Nombre minimal de bons résultats garantis, même dans la configuration la moins favorable. " +
                        "Pour >12 matchs, le résultat peut être approximatif ou indisponible (« ??? »).<br/><br/>" +

                        "<b>• Moyenne attendue et écart-type :</b><br/>" +
                        "Espérance mathématique (moyenne des bons pronostics) et écart-type (dispersion).<br/><br/>" +

                        "<span style='color: #FFA500; font-size: 15pt; font-weight: bold;'>4. Fonction avancée « Auto-Grille »</span><br/>" +
                        "Cette fonctionnalité génère automatiquement un ensemble de tickets optimisés, en tenant compte " +
                        "de vos probabilités calculées à partir des cotes et des options que vous avez cochées. " +
                        "Elle permet de créer rapidement un lot pertinent de paris sans devoir tout gérer manuellement.<br/><br/>" +

                        "<span style='color: #FFA500; font-size: 15pt; font-weight: bold;'>5. Conseils et bonnes pratiques pour une utilisation optimale</span><br/>" +
                        "<b>Limiter le nombre d’options par match :</b> " +
                        "Plus vous sélectionnez de cases, plus le coût explose.<br/>" +
                        "<b>Contrôle rigoureux des cotes :</b> " +
                        "Veillez à la justesse de ces valeurs pour obtenir des probabilités fiables.<br/>" +
                        "<b>Interpréter correctement le Worst-case :</b> " +
                        "Appuyez-vous sur cette information et la distribution pour jauger la robustesse de votre pronostic.<br/>" +
                        "<b>Utilisation stratégique d’Auto-Grille :</b> " +
                        "Lancez cette fonction après avoir analysé les statistiques pour créer un ensemble de tickets optimal.<br/><br/>" +

                        "<span style='color: #FFA500; font-size: 15pt; font-weight: bold;'>6. Exemples pratiques d’utilisation</span><br/>" +
                        "<b>Exemple concret simple :</b><br/>" +
                        "• Certain d’un match (victoire domicile) : cochez uniquement « 1 ».<br/>" +
                        "• Doute entre nul et victoire extérieure : cochez « N » + « 2 ».<br/>" +
                        "• Cliquez sur « Calculer » pour obtenir votre budget et la probabilité de couvrir le véritable scénario.<br/>" +
                        "• Si le coût est trop élevé, réduisez certaines sélections.<br/><br/>" +

                        "<span style='color: #FFA500; font-size: 15pt; font-weight: bold;'>7. Limitations techniques importantes</span><br/>" +
                        "Pour un nombre de matchs élevé (>12), le nombre de scénarios 3^M croît de manière exponentielle. " +
                        "Les ressources nécessaires (temps, mémoire) augmentent considérablement, ce qui peut entraîner :<br/><br/>" +
                        "• Un temps de calcul plus important pour générer les statistiques complètes.<br/>" +
                        "• Des approximations (par échantillonnage) pour certains indicateurs comme le Worst-case.<br/>" +
                        "• L’impossibilité de réaliser un calcul exhaustif dans des délais raisonnables.<br/><br/>" +
                        "Il est recommandé de ne pas dépasser une douzaine de matchs par grille si vous désirez une analyse exhaustive complète.<br/><br/>" +

                        "<span style='color: #FFA500; font-size: 15pt; font-weight: bold;'>8. Support et assistance technique</span><br/>" +
                        "Pour toute question, remarque ou suggestion, contactez directement <b>Valentin G.</b> (<i>nodig63@gmail.com</i>) " +
                        "ou rendez-vous sur le dépôt GitHub du projet. Vos retours sont précieux pour continuer d’améliorer " +
                        "les performances et l’expérience utilisateur.<br/><br/>" +

                        "</body>" +
                        "</html>";
    }
}
