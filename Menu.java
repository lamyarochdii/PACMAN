package Frame;

import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {
    
    public Frame() { 
        initialize();
    }

    public void initialize() {
        setTitle("Welcome!");
        setSize(400, 300);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ==== BOUTON EN HAUT À GAUCHE ====
        JButton myButton = new JButton("Crazy games");
        JPanel buttonPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanelLeft.add(myButton);

        // ==== BOUTONS EN HAUT À DROITE ====
        JButton myButton2 = new JButton("Connexion");
        JButton myButton3 = new JButton("Inscription");

        JPanel buttonPanelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanelRight.add(myButton2);
        buttonPanelRight.add(myButton3);

        // Panneau supérieur pour contenir les deux groupes de boutons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(buttonPanelLeft, BorderLayout.WEST);
        topPanel.add(buttonPanelRight, BorderLayout.EAST);

        // ==== TEXTE "Select your game" ====
        JLabel selectGameLabel = new JLabel("Select your game");
        selectGameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        selectGameLabel.setForeground(Color.BLACK); 

        // Panneau principal pour organiser les composants au centre
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 20, 0); // Espacement sous le texte
        mainPanel.add(selectGameLabel, gbc);

        // ==== BOUTONS AVEC IMAGES + TEXTES ====
        JButton game1Button = createImageButton("C:\\Users\\lamia\\Downloads\\game1.png", 120, 120);
        JButton game2Button = createImageButton("C:\\Users\\lamia\\Downloads\\game2.png", 120, 120);

        JLabel pacmanLabel = new JLabel("Pacman", SwingConstants.CENTER);
        JLabel battleLabel = new JLabel("Bataille navale", SwingConstants.CENTER);

        // Panneaux individuels pour chaque jeu (bouton + texte)
        JPanel game1Panel = new JPanel(new BorderLayout());
        game1Panel.add(game1Button, BorderLayout.CENTER);
        game1Panel.add(pacmanLabel, BorderLayout.SOUTH);

        JPanel game2Panel = new JPanel(new BorderLayout());
        game2Panel.add(game2Button, BorderLayout.CENTER);
        game2Panel.add(battleLabel, BorderLayout.SOUTH);

        // Panneau contenant les jeux centrés
        JPanel buttonCenterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonCenterPanel.add(game1Panel);
        buttonCenterPanel.add(game2Panel);

        // Positionner le panneau des boutons sous le texte
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0); // Réinitialisation des marges
        mainPanel.add(buttonCenterPanel, gbc);

        // ==== AJOUT DES COMPOSANTS À LA FENÊTRE ====
        add(topPanel, BorderLayout.NORTH); // Panneau supérieur avec les boutons
        add(mainPanel, BorderLayout.CENTER);  // Texte + boutons centrés

        // Rendre la fenêtre visible
        setVisible(true);
    }

    // === MÉTHODE POUR CHARGER ET REDIMENSIONNER UNE IMAGE ===
    private JButton createImageButton(String imagePath, int width, int height) {
        System.out.println("Chargement de l'image : " + imagePath); // Debug

        ImageIcon icon = new ImageIcon(imagePath);

        // Vérifier si l'image existe
        if (icon.getIconWidth() == -1) {
            System.out.println("Image non trouvée : " + imagePath);
            return new JButton("Image non trouvée"); // Afficher un texte si l'image est absente
        }

        // Redimensionner l'image
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImg);

        JButton button = new JButton(resizedIcon);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    public static void main(String[] args) {
        new Frame(); // Crée et affiche la fenêtre
    }
}
