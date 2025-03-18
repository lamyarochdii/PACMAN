
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class App {
    public static void main(String[] args) {
        // Créer une fenêtre pour demander le nom du joueur
        JFrame nameFrame = new JFrame("Inscription");
        nameFrame.setSize(350, 250); // Taille de la fenêtre
        nameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        nameFrame.setLocationRelativeTo(null);  // Centrer la fenêtre

        // Créer un panneau principal avec un layout GridBagLayout
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Ajoute des espaces entre les composants

        // Ajouter un titre "Game Tool Box"
        JLabel titleLabel = new JLabel("Games Tool Box");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Définir la police du titre
        gbc.gridwidth = 2; // Occuper deux colonnes pour le titre
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER; // Centrer le titre
        panel.add(titleLabel, gbc);

        // Ajouter une étiquette et un champ de texte pour le login
        JLabel loginLabel = new JLabel("Login:");
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Alignement à gauche
        panel.add(loginLabel, gbc);

        JTextField nameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(nameField, gbc);

        // Ajouter une étiquette et un champ de texte pour le mot de passe
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(passwordField, gbc);

        // Ajouter un bouton "Inscrire"
        JButton submitButton = new JButton("Inscrire");
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER; // Centrer le bouton
        submitButton.setPreferredSize(new Dimension(100, 40));
        panel.add(submitButton, gbc);

        // Ajouter un écouteur d'événement au bouton
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = nameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
        
                if (playerName.isEmpty()) {
                    playerName = "Joueur";
                }
        
                // Fermer la fenêtre d'inscription
                nameFrame.dispose();
        
                // 👉 Nouvelle fenêtre pour choisir le personnage
                JFrame characterFrame = new JFrame("Choisissez votre personnage");
                characterFrame.setSize(300, 200);
                characterFrame.setLocationRelativeTo(null);
                characterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
                JPanel characterPanel = new JPanel(new GridLayout(3, 1, 10, 10));
                JLabel chooseLabel = new JLabel("Choisissez votre personnage :", SwingConstants.CENTER);
                JButton pacmanButton = new JButton("Pacman");
                JButton ladyPacmanButton = new JButton("LadyPacman");
        
                characterPanel.add(chooseLabel);
                characterPanel.add(pacmanButton);
                characterPanel.add(ladyPacmanButton);
        
                characterFrame.add(characterPanel);
                characterFrame.setVisible(true);
        
                // 👉 Action bouton Pacman
                pacmanButton.addActionListener(evt -> {
                    characterFrame.dispose();
                    launchGame("pacman");  // méthode séparée pour lancer le jeu
                });
        
                // 👉 Action bouton LadyPacman
                ladyPacmanButton.addActionListener(evt -> {
                    characterFrame.dispose();
                    launchGame("ladypacman");  // méthode séparée pour lancer le jeu
                });
            }
        });
        

        // Ajouter le panneau à la fenêtre
        nameFrame.add(panel);

        // Afficher la fenêtre de saisie du nom
        nameFrame.setVisible(true);
    }

    public static void launchGame(String characterChoice) {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;
    
        JFrame gameFrame = new JFrame("Pac Man");
        gameFrame.setSize(boardWidth, boardHeight);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(false);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        PacMan pacmanGame = new PacMan(characterChoice); // 👈 Passe le personnage ici
        gameFrame.add(pacmanGame);
    
        String filePath = "Pac-Man-Theme-Original.wav";
        new Thread(() -> playMusic(filePath)).start();
    
        gameFrame.setVisible(true);
    }
    
    public static void playMusic(String filePath) {
        try {
            // Charger le fichier audio
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath));

            // Obtenir le clip audio
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Jouer la musique
            clip.start();
            // Attendre la fin de la lecture (simplement attendre la durée du clip)
            Thread.sleep(clip.getMicrosecondLength() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



