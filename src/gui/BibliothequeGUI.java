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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Interface graphique pour visualiser les interactions entre les agents
 */
public class BibliothequeGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static BibliothequeGUI instance;

    // Panneaux principaux
    private JPanel mainPanel;
    private JPanel biblioPanel;
    private JPanel usersPanel;
    private JTextPane logPane;
    private JScrollPane logScroll;

    // Zones de texte pour chaque agent
    private Map<String, JTextArea> biblioAreas = new HashMap<>();
    private Map<String, JTextArea> userAreas = new HashMap<>();

    // Styles de texte pour différents types de messages
    private SimpleAttributeSet biblioStyle = new SimpleAttributeSet();
    private SimpleAttributeSet userStyle = new SimpleAttributeSet();
    private SimpleAttributeSet transactionStyle = new SimpleAttributeSet();
    private SimpleAttributeSet infoStyle = new SimpleAttributeSet();

    // Format pour les timestamps
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private BibliothequeGUI() {
        super("Système Multi-Agents - Bibliothèque");

        // Configuration des styles de texte
        StyleConstants.setForeground(biblioStyle, new Color(0, 102, 51));  // Vert foncé
        StyleConstants.setBold(biblioStyle, true);

        StyleConstants.setForeground(userStyle, new Color(0, 51, 153));    // Bleu foncé
        StyleConstants.setBold(userStyle, true);

        StyleConstants.setForeground(transactionStyle, new Color(153, 0, 76)); // Pourpre
        StyleConstants.setBold(transactionStyle, true);

        StyleConstants.setForeground(infoStyle, new Color(102, 102, 102));  // Gris
        StyleConstants.setItalic(infoStyle, true);

        setupUI();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);

        // Personnalisation de l'icône et apparence
        try {
            // Note: Créez une icône appropriée et placez-la dans le dossier resources
            // ImageIcon icon = new ImageIcon(getClass().getResource("/resources/library_icon.png"));
            // setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("Impossible de charger l'icône: " + e.getMessage());
        }
    }

    private void setupUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(245, 245, 245));

        // En-tête avec titre
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(51, 102, 153));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JLabel titleLabel = new JLabel("Simulation de Bibliothèque avec Système Multi-Agents");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Panneau central pour les agents
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setBackground(new Color(245, 245, 245));

        // Sous-panneau pour la bibliothécaire
        biblioPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        biblioPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 102, 153), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ),
            "Bibliothécaire",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(51, 102, 153)
        ));
        biblioPanel.setBackground(new Color(240, 240, 240));

        // Sous-panneau pour les utilisateurs
        usersPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        usersPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 102, 51), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ),
            "Utilisateurs",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(0, 102, 51)
        ));
        usersPanel.setBackground(new Color(240, 240, 240));

        centerPanel.add(biblioPanel);
        centerPanel.add(usersPanel);

        // Panneau inférieur pour les logs
        logPane = new JTextPane();
        logPane.setEditable(false);
        Font logFont = new Font("Consolas", Font.PLAIN, 12);
        logPane.setFont(logFont);
        logPane.setBackground(new Color(250, 250, 250));

        // Auto-scroll
        DefaultCaret caret = (DefaultCaret) logPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        logScroll = new JScrollPane(logPane);
        logScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(153, 0, 76), 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ),
            "Journal des Transactions",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(153, 0, 76)
        ));
        logScroll.setPreferredSize(new Dimension(1280, 200));
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Assemblage final
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(logScroll, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    public static synchronized BibliothequeGUI getInstance() {
        if (instance == null) {
            instance = new BibliothequeGUI();
        }
        return instance;
    }

    public void ajouterBibliothecaire(String nom) {
        SwingUtilities.invokeLater(() -> {
            if (!biblioAreas.containsKey(nom)) {
                JTextArea biblioArea = createStyledTextArea(nom, new Color(240, 248, 255));
                JScrollPane scrollPane = new JScrollPane(biblioArea);
                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 102, 153), 1));
                biblioPanel.add(scrollPane);
                biblioAreas.put(nom, biblioArea);

                biblioPanel.revalidate();
                biblioPanel.repaint();

                logMessage("Bibliothécaire " + nom + " connectée au système", infoStyle);
            }
        });
    }

    public void ajouterUtilisateur(String nom, String info) {
        SwingUtilities.invokeLater(() -> {
            if (!userAreas.containsKey(nom)) {
                JTextArea userArea = createStyledTextArea(nom, new Color(245, 255, 250));
                userArea.append("Description: " + info + "\n");

                JScrollPane scrollPane = new JScrollPane(userArea);
                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 51), 1));
                usersPanel.add(scrollPane);
                userAreas.put(nom, userArea);

                usersPanel.revalidate();
                usersPanel.repaint();

                logMessage("Utilisateur " + nom + " entre dans la bibliothèque", userStyle);
            }
        });
    }

    private JTextArea createStyledTextArea(String title, Color bgColor) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(51, 51, 51)
        ));
        area.setPreferredSize(new Dimension(480, 150));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(bgColor);
        area.setFont(new Font("Arial", Font.PLAIN, 12));

        return area;
    }

    public void supprimerUtilisateur(String nom) {
        SwingUtilities.invokeLater(() -> {
            JTextArea area = userAreas.remove(nom);
            if (area != null) {
                JScrollPane parent = (JScrollPane) area.getParent().getParent();
                usersPanel.remove(parent);
                usersPanel.revalidate();
                usersPanel.repaint();

                logMessage("Utilisateur " + nom + " a quitté la bibliothèque", infoStyle);
            }
        });
    }

    public void miseAJourBibliothecaire(String nom, String message) {
        SwingUtilities.invokeLater(() -> {
            JTextArea area = biblioAreas.get(nom);
            if (area != null) {
                String timestamp = "[" + sdf.format(new Date()) + "] ";
                area.append(timestamp + message + "\n");
                // Faire défiler automatiquement vers le bas
                area.setCaretPosition(area.getDocument().getLength());
                logMessage("[Bibliothécaire " + nom + "] " + message, biblioStyle);
            }
        });
    }

    public void miseAJourUtilisateur(String nom, String message) {
        SwingUtilities.invokeLater(() -> {
            JTextArea area = userAreas.get(nom);
            if (area != null) {
                String timestamp = "[" + sdf.format(new Date()) + "] ";
                area.append(timestamp + message + "\n");
                // Faire défiler automatiquement vers le bas
                area.setCaretPosition(area.getDocument().getLength());
                logMessage("[Utilisateur " + nom + "] " + message, userStyle);
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
