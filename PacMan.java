import java.awt.*; // Fournit des classes pour la gestion des éléments graphiques (images, couleurs, tailles, etc.)
import java.awt.event.*;// Permet la gestion des événements comme les clics, les frappes clavier, et les temporisations
import java.util.HashSet; // Collection qui stocke des objets uniques, utile pour gérer des ensembles de murs, de nourriture, etc.
import java.util.Random; // Générateur de nombres aléatoires, utilisé ici pour les déplacements aléatoires des fantômes
import javax.swing.*; // Bibliothèque Swing, utilisée pour créer des interfaces graphiques (panneaux, timers, images)


// La classe principale du programme, représentant l'ensemble du jeu Pac-Man
public class PacMan extends JPanel implements ActionListener, KeyListener {
    //*Remarque : `JPanel` est un conteneur graphique, et `ActionListener` / `KeyListener` sont des interfaces
    // qui permettent de répondre aux événements comme les entrées clavier ou les temporisations.
    //private String playerName;
    //private String playerSurname;
    // Classe interne représentant un bloc sur le plateau de jeu (mur, nourriture, Pac-Man ou fantôme)
    class Block {
        int x;//position d'un objet en x
        int y; //position d'un objet en y
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
            //tileSize/4 signifie que la vitesse est un quart de la taille d'un "tile" (case).
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
        //this.playerName = name;
        //this.playerSurname = surname;
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

        // Vérification des collisions avec les fantômes
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1; // Réduit les vies de 1 si Pacman entre en collision avec un fantôme
                if (lives == 0) { // Si Pacman n'a plus de vies, la partie est terminée
                    gameOver = true;
                    return;
                }
                resetPositions();  // Si Pacman perd une vie, réinitialise les positions des personnages
            }

            // Si le fantôme atteint la ligne horizontale de la zone du jeu (niveau du bas), change de direction
            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U'); // Le fantôme se dirige vers le haut
            }
            // Mise à jour de la position du fantôme en fonction de sa vitesse
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

             // Vérification des collisions avec les murs
            for (Block wall : walls) {
                // Si le fantôme entre en collision avec un mur ou dépasse les limites du jeu, il inverse sa direction
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX; // Annule le mouvement du fantôme sur l'axe X
                    ghost.y -= ghost.velocityY;// Annule le mouvement du fantôme sur l'axe Y
                    char newDirection = directions[random.nextInt(4)]; // Choisit une nouvelle direction aléatoire
                    ghost.updateDirection(newDirection); // Choisit une nouvelle direction aléatoire
                }
            }
        }

        // Vérification des collisions avec la nourriture
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food; // Enregistre la nourriture que Pacman a mangée
                score += 10;// Augmente le score de 10 points
            }
        }

        // Retirer la nourriture mangée de la liste
        foods.remove(foodEaten);

        // Si toute la nourriture a été mangée, on recharge la carte et réinitialise les positions
        if (foods.isEmpty()) {
            loadMap();  // Recharge la carte du jeu
            resetPositions(); // Réinitialise les positions des personnages
        }
    }

    // Méthode pour vérifier les collisions entre deux objets (Pacman ou un fantôme)
    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    // Réinitialise les positions de Pacman et des fantômes
    public void resetPositions() {
        pacman.reset(); // Réinitialise Pacman à sa position de départ
        pacman.velocityX = 0;  // Réinitialise la vitesse de Pacman
        pacman.velocityY = 0; // Réinitialise la vitesse de Pacman
        for (Block ghost : ghosts) {
            ghost.reset(); // Réinitialise la position du fantôme
            char newDirection = directions[random.nextInt(4)]; // Choisit une nouvelle direction aléatoire pour le fantôme
            ghost.updateDirection(newDirection);  // Met à jour la direction du fantôme
        }
    }

    //Gérer les événements d'action du jeu
    @Override
    public void actionPerformed(ActionEvent e) {
        move(); // Effectue le déplacement de Pacman et des fantômes
        repaint(); // Rafraîchit l'affichage du jeu
        if (gameOver) { // Si la partie est terminée, arrête la boucle du jeu
            gameLoop.stop();
        }
    }


    //Gestion des événements du clavier
    @Override
    public void keyTyped(KeyEvent e) {
    // Cette méthode est appelée lorsque l'utilisateur appuie sur une touche du clavier.
    // Cependant, dans ce code, elle n'est pas utilisée, donc elle reste vide.
    }

    @Override
    public void keyPressed(KeyEvent e) {
         // Cette méthode est appelée dès qu'une touche est enfoncée.
         // Cependant, dans ce code, elle n'est pas utilisée, donc elle reste vide.
    }

    @Override
    public void keyReleased(KeyEvent e) {
         // Cette méthode est appelée lorsque l'utilisateur relâche une touche du clavier.
        if (gameOver) { // Si le jeu est terminé (gameOver est vrai)
            loadMap();  // Recharge la carte du jeu, probablement pour redémarrer le niveau ou le jeu
            resetPositions(); // Réinitialise les positions de Pacman et des fantômes
            lives = 3;  // Réinitialise le nombre de vies à 3 
            score = 0; // Réinitialise le score à 0
            gameOver = false; // Met fin à l'état de "game over"
            gameLoop.start();  // Redémarre la boucle du jeu (démarre l'animation et la logique du jeu)
        }

        // Gestion des déplacements de Pacman en fonction des touches directionnelles
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U'); // Si la touche "flèche haut" est pressée, Pacman se déplace vers le haut
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');  // Si la touche "flèche bas" est pressée, Pacman se déplace vers le bas
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L'); // Si la touche "flèche gauche" est pressée, Pacman se déplace vers la gauche
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }


        // Change l'image de Pacman en fonction de sa direction actuelle.
    // Cela est fait pour donner l'impression que Pacman "regarde" dans la direction qu'il prend.
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
