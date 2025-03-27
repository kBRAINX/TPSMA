package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LibrairieGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static LibrairieGUI instance;

    private JPanel mainPanel;
    private JPanel vendeurPanel;
    private JPanel acheteurPanel;
    private JTextPane logPane;
    private JScrollPane logScroll;
    private Map<String, JTextArea> vendeurAreas = new HashMap<>();
    private Map<String, JTextArea> acheteurAreas = new HashMap<>();

    // Couleurs pour les différents types de messages
    private SimpleAttributeSet vendeurStyle = new SimpleAttributeSet();
    private SimpleAttributeSet acheteurStyle = new SimpleAttributeSet();
    private SimpleAttributeSet transactionStyle = new SimpleAttributeSet();
    private SimpleAttributeSet infoStyle = new SimpleAttributeSet();

    // Format pour les timestamps
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private LibrairieGUI() {
        super("Système Multi-Agent - Librairie");

        // Configuration des styles de texte
        StyleConstants.setForeground(vendeurStyle, new Color(0, 128, 0));  // Vert
        StyleConstants.setBold(vendeurStyle, true);

        StyleConstants.setForeground(acheteurStyle, new Color(0, 0, 255));  // Bleu
        StyleConstants.setBold(acheteurStyle, true);

        StyleConstants.setForeground(transactionStyle, new Color(128, 0, 128));  // Violet
        StyleConstants.setBold(transactionStyle, true);

        StyleConstants.setForeground(infoStyle, new Color(128, 128, 128));  // Gris
        StyleConstants.setItalic(infoStyle, true);

        setupUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Panneau supérieur pour les vendeurs et acheteurs
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Sous-panneau des vendeurs
        vendeurPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        vendeurPanel.setBorder(BorderFactory.createTitledBorder("Vendeurs"));

        // Sous-panneau des acheteurs
        acheteurPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        acheteurPanel.setBorder(BorderFactory.createTitledBorder("Acheteurs"));

        topPanel.add(vendeurPanel);
        topPanel.add(acheteurPanel);

        // Panneau inférieur pour les logs
        logPane = new JTextPane();
        logPane.setEditable(false);
        Font logFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        logPane.setFont(logFont);

        // Auto-scroll
        DefaultCaret caret = (DefaultCaret) logPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        // Créer un JScrollPane avec une taille préférée fixe
        logScroll = new JScrollPane(logPane);
        logScroll.setBorder(BorderFactory.createTitledBorder("Journal des transactions"));
        logScroll.setPreferredSize(new Dimension(1200, 200)); // Hauteur fixe pour le journal
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Diviser la fenêtre en deux parties
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        // Ajouter un séparateur entre le panneau supérieur et inférieur
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    public static synchronized LibrairieGUI getInstance() {
        if (instance == null) {
            instance = new LibrairieGUI();
        }
        return instance;
    }

    public void ajouterVendeur(String nom) {
        SwingUtilities.invokeLater(() -> {
            JTextArea vendeurArea = new JTextArea();
            vendeurArea.setEditable(false);
            vendeurArea.setBorder(BorderFactory.createTitledBorder(nom));
            vendeurArea.setPreferredSize(new Dimension(300, 150));
            vendeurArea.setLineWrap(true);
            vendeurArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(vendeurArea);
            vendeurPanel.add(scrollPane);
            vendeurAreas.put(nom, vendeurArea);

            vendeurPanel.revalidate();

            logMessage("Vendeur " + nom + " connecté au système", infoStyle);
        });
    }

    public void ajouterAcheteur(String nom, String livreRecherche) {
        SwingUtilities.invokeLater(() -> {
            JTextArea acheteurArea = new JTextArea();
            acheteurArea.setEditable(false);
            acheteurArea.setBorder(BorderFactory.createTitledBorder(nom));
            acheteurArea.setPreferredSize(new Dimension(300, 150));
            acheteurArea.append("Recherche: " + livreRecherche + "\n");
            acheteurArea.setLineWrap(true);
            acheteurArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(acheteurArea);
            acheteurPanel.add(scrollPane);
            acheteurAreas.put(nom, acheteurArea);

            acheteurPanel.revalidate();

            logMessage("Acheteur " + nom + " recherche " + livreRecherche, acheteurStyle);
        });
    }

    public void supprimerAcheteur(String nom) {
        SwingUtilities.invokeLater(() -> {
            JTextArea area = acheteurAreas.remove(nom);
            if (area != null) {
                JScrollPane parent = (JScrollPane) area.getParent().getParent();
                acheteurPanel.remove(parent);
                acheteurPanel.revalidate();
                acheteurPanel.repaint();

                logMessage("Acheteur " + nom + " a quitté le système", infoStyle);
            }
        });
    }

    public void miseAJourVendeur(String nom, String message) {
        SwingUtilities.invokeLater(() -> {
            JTextArea area = vendeurAreas.get(nom);
            if (area != null) {
                area.append(message + "\n");
                // Faire défiler automatiquement vers le bas
                area.setCaretPosition(area.getDocument().getLength());
                logMessage("[Vendeur " + nom + "] " + message, vendeurStyle);
            }
        });
    }

    public void miseAJourAcheteur(String nom, String message) {
        SwingUtilities.invokeLater(() -> {
            JTextArea area = acheteurAreas.get(nom);
            if (area != null) {
                area.append(message + "\n");
                // Faire défiler automatiquement vers le bas
                area.setCaretPosition(area.getDocument().getLength());
                logMessage("[Acheteur " + nom + "] " + message, acheteurStyle);
            }
        });
    }

    public void logTransaction(String message) {
        SwingUtilities.invokeLater(() -> {
            logMessage("TRANSACTION: " + message, transactionStyle);
        });
    }

    private void logMessage(String message, SimpleAttributeSet style) {
        try {
            StyledDocument doc = logPane.getStyledDocument();
            String timestamp = "[" + sdf.format(new Date()) + "] ";
            doc.insertString(doc.getLength(), timestamp, infoStyle);
            doc.insertString(doc.getLength(), message + "\n", style);

            // Faire défiler automatiquement vers le bas
            logPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
