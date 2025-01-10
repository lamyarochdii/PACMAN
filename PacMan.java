import java.awt.*; // Fournit des classes pour la gestion des éléments graphiques (images, couleurs, tailles, etc.)
import java.awt.event.*;// Permet la gestion des événements comme les clics, les frappes clavier, et les temporisations
import java.util.HashSet; // Collection qui stocke des objets uniques, utile pour gérer des ensembles de murs, de nourriture, etc.
import java.util.Random; // Générateur de nombres aléatoires, utilisé ici pour les déplacements aléatoires des fantômes
import javax.swing.*; // Bibliothèque Swing, utilisée pour créer des interfaces graphiques (panneaux, timers, images)


// La classe principale du programme, représentant l'ensemble du jeu Pac-Man
public class PacMan extends JPanel implements ActionListener, KeyListener {
    // **Remarque :** `JPanel` est un conteneur graphique, et `ActionListener` / `KeyListener` sont des interfaces
    // qui permettent de répondre aux événements comme les entrées clavier ou les temporisations.

    // Classe interne représentant un bloc sur le plateau de jeu (mur, nourriture, Pac-Man ou fantôme)
    class Block {
        int x;
        int y;
        int width; // largeur d'un bloc
        int height; // hauteur d'un bloc 
        Image image;  // Image associée au bloc pour l'affichage graphique

        // Position initiale du bloc pour réinitialisation 
        int startX;
        int startY;

        char direction = 'U'; // U D L R // Direction actuelle ('U' = up/haut, 'D' = down/bas, 'L' = left/gauche, 'R' = right/droite)

         // Vitesse actuelle du bloc dans les directions X et Y
        int velocityX = 0;
        int velocityY = 0;

        // Constructeur pour initialiser un bloc avec ses propriétés (image, position, dimensions)
        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        // Méthode : Met à jour la direction du pacman ou fantôme et vérifie les collisions avec les murs
        void updateDirection(char direction) {
            char prevDirection = this.direction; // Stocke la direction précédente pour la restaurer en cas de collision
            this.direction = direction; // Met à jour la direction actuelle
            updateVelocity(); // Recalcule la vitesse en fonction de la nouvelle direction

            // Déplace le bloc en fonction de la vitesse calculée
            this.x += this.velocityX;
            this.y += this.velocityY;

            // Vérifie si le bloc entre en collision avec l'un des murs
            for (Block wall : walls) { // Parcourt tous les murs du plateau
                if (collision(this, wall)) { // Détecte une collision avec un mur
                    this.x -= this.velocityX; // Annule le déplacement en X
                    this.y -= this.velocityY;// Annule le déplacement en Y
                    this.direction = prevDirection;  // Réinitialise la direction à celle précédente
                    updateVelocity();// Recalcule la vitesse en conséquence
                }
            }
        }

        // Méthode : Met à jour la vitesse (`velocityX` et `velocityY`) en fonction de la direction
        void updateVelocity() {
            if (this.direction == 'U') {  // Si la direction est "up" (haut)
                this.velocityX = 0; // Pas de déplacement horizontal
                this.velocityY = -tileSize/4; // Déplacement vers le haut (négatif sur l'axe Y)
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        // Méthode : Réinitialise la position du bloc à ses coordonnées de départ
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

// Dimensions du plateau de jeu
    private int rowCount = 21; // Nombre de lignes dans la carte
    private int columnCount = 19;  // Nombre de colonnes dans la carte
    private int tileSize = 32; // Taille d'une case (en pixels)
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    // Structures de données pour stocker les blocs du jeu
    HashSet<Block> walls; // Ensemble des blocs représentant les murs
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop; // Timer pour gérer les mises à jour du jeu à intervalle régulier
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();// Générateur aléatoire pour les mouvements des fantômes
    int score = 0;  // Score du joueur
    int lives = 3; // Nombre de vies restantes
    boolean gameOver = false; // Indique si le jeu est terminé

    // Constructeur de la classe PacMan
    PacMan() {
        // Définit la taille du panneau de jeu en fonction de la largeur et de la hauteur du plateau
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // Définit le fond d'écran du jeu en noir
        setBackground(Color.BLACK);
        // Ajoute un écouteur de clavier pour détecter les touches pressées
        addKeyListener(this);
        // Rend le panneau focusable pour capturer les événements clavier
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        // Charge la carte initiale du jeu à partir des données du tableau
        loadMap();

        // Initialise une direction aléatoire pour chaque fantôme
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
         // Configure la boucle principale du jeu avec un intervalle de 50ms entre chaque frame (20 FPS)
        gameLoop = new Timer(50, this); //20fps (1000/50)
        // Démarre la boucle de jeu
        gameLoop.start();

    }

    // Méthode pour charger la carte du jeu
    public void loadMap() {
        // Initialise les ensembles de murs, de nourriture et de fantômes
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        // Parcourt chaque ligne et colonne de la carte
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r]; // Récupère la ligne actuelle de la carte
                char tileMapChar = row.charAt(c); // Caractère représentant un type d'objet (mur, nourriture, fantôme, etc.)

                // Calcule les coordonnées de l'objet en fonction de sa position sur la carte
                int x = c*tileSize;
                int y = r*tileSize;

                 // Vérifie quel type d'objet représente le caractère
                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    // Méthode pour dessiner les éléments du jeu
    public void paintComponent(Graphics g) {
        super.paintComponent(g);// Nettoie l'écran avant de dessiner
        draw(g);// Appelle la méthode de dessin des éléments
    }

    public void draw(Graphics g) {
        // Dessine Pac-Man à ses coordonnées actuelles
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

         // Dessine les fantômes
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Dessine les murs 
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Dessine la nourriture sous forme de petits rectangles blancs
        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

       // Affiche le score et le nombre de vies restantes
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
    }

    // Méthode pour déplacer Pac-Man et les fantômes
    public void move() {
        // Déplacement de Pac-Man selon sa vélocité actuelle
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        //check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) { // Si Pac-Man touche un mur
                pacman.x -= pacman.velocityX;// Annule le déplacement
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        //check ghost collisions
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        //check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }
}
