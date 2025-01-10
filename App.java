import javax.swing.JFrame; // Importation de la classe JFrame de la bibliothèque Swing pour créer une fenêtre graphique.

public class App {
    public static void main(String[] args) throws Exception {
        // Définition des paramètres du tableau de jeu
        int rowCount = 21;  // Le nombre de lignes dans le tableau (hauteur)
        int columnCount = 19; // Le nombre de colonnes dans le tableau (largeur)
        int tileSize = 32;  // La taille d'une tuile (unité de mesure de chaque "case" du tableau, ici 32x32 pixels)
        
        // Calcul de la largeur et de la hauteur de la fenêtre du jeu en fonction du nombre de lignes et colonnes
        int boardWidth = columnCount * tileSize; // Largeur de la fenêtre en pixels (19 colonnes * 32 pixels)
        int boardHeight = rowCount * tileSize; // Hauteur de la fenêtre en pixels (21 lignes * 32 pixels)

         // Création d'un objet JFrame (fenêtre graphique) pour le jeu Pac-Man
        JFrame frame = new JFrame("Pac Man"); // Création d'une fenêtre avec le titre "Pac Man"

        // frame.setVisible(true);  // Cette ligne est commentée. Si activée, elle rendrait la fenêtre visible immédiatement.

        // Définition de la taille de la fenêtre
        frame.setSize(boardWidth, boardHeight);

       // Centrer la fenêtre sur l'écran
        frame.setLocationRelativeTo(null); // Positionne la fenêtre au centre de l'écran.

        // Désactive le redimensionnement de la fenêtre (l'utilisateur ne peut pas agrandir ou réduire la taille de la fenêtre)
        frame.setResizable(false);

        // Définit le comportement de la fenêtre lorsqu'on clique sur le bouton de fermeture (ferme l'application)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Création d'une instance de la classe PacMan (c'est probablement un composant graphique personnalisé qui contient la logique du jeu)
        PacMan pacmanGame = new PacMan();

        // Ajoute l'objet `pacmanGame` à la fenêtre. Cela permettra à l'interface graphique de PacMan d'apparaître dans la fenêtre.
        frame.add(pacmanGame);

         // Ajuste la fenêtre pour s'adapter au contenu (bien que la taille de la fenêtre soit déjà définie manuellement)
        frame.pack();

        // Demande à ce que l'objet pacmanGame reçoive les événements de clavier (cela permet de contrôler Pacman avec le clavier)
        pacmanGame.requestFocus();

        // Rendre la fenêtre visible, ce qui lance l'affichage de l'interface graphique
        frame.setVisible(true);

    }
}
