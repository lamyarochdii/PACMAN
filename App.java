
import javax.swing.*;
import java.awt.event.*;

public class App {
    public static void main(String[] args) {
        // Créer une fenêtre pour demander le nom du joueur
        JFrame nameFrame = new JFrame("Entrez votre nom");
        nameFrame.setSize(300, 150);
        nameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        nameFrame.setLocationRelativeTo(null);  // Centrer la fenêtre

        // Créer un champ de texte pour entrer le nom
        JTextField nameField = new JTextField(15);
        
        // Créer un bouton pour soumettre le nom
        JButton submitButton = new JButton("Inscrire");

        // Ajouter un écouteur d'événement au bouton
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerName = nameField.getText().trim();

                // Si le champ est vide, utiliser un nom par défaut
                if (playerName.isEmpty()) {
                    playerName = "Joueur";
                }

                // Fermer la fenêtre de saisie du nom
                nameFrame.dispose();

                // Créer une nouvelle fenêtre pour le jeu
                int rowCount = 21;
                int columnCount = 19;
                int tileSize = 32;
                int boardWidth = columnCount * tileSize;
                int boardHeight = rowCount * tileSize;

                // Créer la fenêtre principale du jeu PacMan
                JFrame gameFrame = new JFrame("Pac Man");
                gameFrame.setSize(boardWidth, boardHeight);
                gameFrame.setLocationRelativeTo(null);
                gameFrame.setResizable(false);
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Créer l'objet PacMan 
                PacMan pacmanGame = new PacMan();

                // Ajouter le jeu à la fenêtre
                gameFrame.add(pacmanGame);

                // Afficher la fenêtre du jeu
                gameFrame.setVisible(true);
            }
        });

        // Créer un panneau pour contenir le champ de texte et le bouton
        JPanel panel = new JPanel();
        panel.add(new JLabel("Entrez votre nom :"));
        panel.add(nameField);
        panel.add(submitButton);

        // Ajouter le panneau à la fenêtre
        nameFrame.add(panel);
        
        // Afficher la fenêtre de saisie du nom
        nameFrame.setVisible(true);
    }
}
