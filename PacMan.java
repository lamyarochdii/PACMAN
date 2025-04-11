
import java.awt.*; // Fournit des classes pour la gestion des éléments graphiques (images, couleurs, tailles, etc.)
import java.awt.event.*;// Permet la gestion des événements comme les clics, les frappes clavier, et les temporisations
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet; // Collection qui stocke des objets uniques, utile pour gérer des ensembles de murs, de nourriture, etc.
import java.util.Random; // Générateur de nombres aléatoires, utilisé ici pour les déplacements aléatoires des fantômes
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*; // Bibliothèque Swing, utilisée pour créer des interfaces graphiques (panneaux, timers, images)
import java.util.List; // Importe l'interface List
import java.util.ArrayList; // Importe la classe ArrayList

// La classe principale du programme, représentant l'ensemble du jeu Pac-Man
public class PacMan extends JPanel implements ActionListener, KeyListener {

    public boolean canPlay = false; // 👈 rends-le bien PUBLIC
    public boolean showReady = true;
    private long lastEatingTime = 0;


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

        boolean isGhost = false; // 👈 Ajout ici !

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
            int speed = tileSize / 4;
        
            // 👻 Si c’est un fantôme et qu’il est en mode frightened → on le ralentit
            if (isGhost && isFrightenedMode) {
                speed = tileSize / 8; // Deux fois plus lent
            }
        
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -speed;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = speed;
            } else if (this.direction == 'L') {
                this.velocityX = -speed;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = speed;
                this.velocityY = 0;
            }
        
            // tileSize/4 signifie une vitesse normale, tileSize/8 = ralenti
        }
        

        // Méthode : Réinitialise la position du bloc à ses coordonnées de départ
        void reset() {
            System.out.println("🔁 RESET to " + startX + ", " + startY); // 🪪 Debug
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    // Classe pour gérer les yeux des fantômes qui retournent à la Ghost House
    class FloatingEyes {
        int x, y;
        int goalX, goalY;
        int velocityX = 0, velocityY = 0;
        char direction = 'U';
        char lastDirection = ' '; // 🧠 Pour se souvenir de la dernière direction

        boolean reachedGoal = false;
        long arrivalTime = 0;
    
        FloatingEyes(int startX, int startY) {
            this.x = startX;
            this.y = startY;
            this.goalX = 9 * tileSize; // 👁️ Destination = red ghost 'r'
    this.goalY = 8 * tileSize;
            updateDirectionTowardGoal();
        }
    
        void move() {
            if (reachedGoal) return;
    
            x += velocityX;
            y += velocityY;
    
            if (x % tileSize == 0 && y % tileSize == 0) {
                if (x == goalX && y == goalY) {
                    reachedGoal = true;
                    arrivalTime = System.currentTimeMillis();
                    velocityX = 0;
                    velocityY = 0;
                } else {
                    updateDirectionTowardGoal();
                }
            }
        }
    
        boolean shouldDisappear() {
            return reachedGoal && System.currentTimeMillis() - arrivalTime > 1000;
        }
    
        private void updateDirectionTowardGoal() {
            int speed = tileSize / 4;
            int currentRow = y / tileSize;
            int currentCol = x / tileSize;
            int targetRow = goalY / tileSize;
            int targetCol = goalX / tileSize;
        
            List<Character> directions = new ArrayList<>();
        
            // 👁️ Étape 1 : directions qui rapprochent de la cible
            if (Math.abs(currentCol - targetCol) >= Math.abs(currentRow - targetRow)) {
                if (currentCol < targetCol) directions.add('R');
                if (currentCol > targetCol) directions.add('L');
                if (currentRow < targetRow) directions.add('D');
                if (currentRow > targetRow) directions.add('U');
            } else {
                if (currentRow < targetRow) directions.add('D');
                if (currentRow > targetRow) directions.add('U');
                if (currentCol < targetCol) directions.add('R');
                if (currentCol > targetCol) directions.add('L');
            }
        
            // Étape 2 : compléter avec les autres directions restantes
            for (char dir : List.of('U', 'D', 'L', 'R')) {
                if (!directions.contains(dir)) {
                    directions.add(dir);
                }
            }
        
            // Étape 3 : exclure la direction opposée à la précédente
            char opposite = switch (lastDirection) {
                case 'U' -> 'D';
                case 'D' -> 'U';
                case 'L' -> 'R';
                case 'R' -> 'L';
                default -> ' ';
            };
        
            // Essaye les directions, sauf celle qui est strictement opposée à lastDirection
            for (char dir : directions) {
                if (dir != opposite && canMove(dir)) {
                    setDirection(dir, speed);
                    lastDirection = dir; // 🧠 mise à jour mémoire
                    return;
                }
            }
        
            // Dernier recours : essayer quand même la direction opposée (cul-de-sac)
            if (canMove(opposite)) {
                setDirection(opposite, speed);
                lastDirection = opposite;
                return;
            }
        
            // 😵 Totalement bloqué
            velocityX = 0;
            velocityY = 0;
        }
        

        private boolean isOpposite(char d1, char d2) {
            return (d1 == 'U' && d2 == 'D') || (d1 == 'D' && d2 == 'U') ||
                   (d1 == 'L' && d2 == 'R') || (d1 == 'R' && d2 == 'L');
        }
        
        

        private boolean canMove(char dir) {
            int testX = x, testY = y;
        
            if (dir == 'U') testY -= tileSize;
            if (dir == 'D') testY += tileSize;
            if (dir == 'L') testX -= tileSize;
            if (dir == 'R') testX += tileSize;
        
            Rectangle nextTile = new Rectangle(testX, testY, tileSize, tileSize);
        
            for (Block wall : walls) {
                Rectangle wallRect = new Rectangle(wall.x, wall.y, wall.width, wall.height);
                if (wallRect.intersects(nextTile)) {
                    return false; // ❌ collision avec un mur
                }
            }
        
            return true; // ✅ passage possible
        }

        private void setDirection(char dir, int speed) {
            direction = dir;
            switch (dir) {
                case 'U': velocityX = 0; velocityY = -speed; break;
                case 'D': velocityX = 0; velocityY = speed; break;
                case 'L': velocityX = -speed; velocityY = 0; break;
                case 'R': velocityX = speed; velocityY = 0; break;
                default: velocityX = 0; velocityY = 0; break;
            }
        }
        
        
        
        
        
        
    }
    
    
    class TimedCherry {
        Block block;
        long spawnTime;
    
        TimedCherry(Block block) {
            this.block = block;
            this.spawnTime = System.currentTimeMillis();
        }
    
        boolean isExpired() {
            return System.currentTimeMillis() - spawnTime > 10000; // 10 secondes
        }
    }
    

// Dimensions du plateau de jeu
private int rowCount = 19;  // Avant c'était 21, maintenant c'est 20
private int columnCount = 19;  // La largeur reste 19 colonnes
private int tileSize = 32;  // Chaque case fait 32 pixels

private int boardWidth = columnCount * tileSize;  // Largeur totale du jeu
private int boardHeight = rowCount * tileSize;  // Hauteur totale du jeu




    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;
    private Image cherryImage; // 🍒 Image des cerises
    private Image blackWallImage;
    private Image frightenedGhostImage;
    private Image deuxcentImage;

    private Image readyImage;

 


private Image chronometerImage;
private int frightenedTimeRemaining = 0;
private Timer frightenedCountdownTimer;

private boolean showDeuxCent = false;
private long deuxCentDisplayStartTime = 0;
private int deuxcentX = 0; // Position d'affichage (on peut la centrer sur le ghost mangé)
private int deuxcentY = 0;

private char requestedDirection = ' '; // rien demandé tant que l'utilisateur n'appuie pas
private boolean firstMoveStarted = false;


private Image eyesImage;
private Clip eatingClip; // Son de mastication
private Clip powerPelletClip;

private Image whiteGhostImage;
private Image gameOverImage;




private boolean isDying = false;
private int dyingFrameIndex = 0;
private List<Image> dyingFrames = new ArrayList<>();
private Timer dyingAnimationTimer;




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
        "XOOX X       X XOOX",
        "XXXX X XXrXX X XXXX",
        "X      XbpoX      X",
        "XXXX X XXXXX X XXXX",
        "XOOX X       X XOOX",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "                   ", // ✅ Aucune case de mur ici
        "                   "  // ✅ Aucune case de mur ici
    };
    

    
    // Structures de données pour stocker les blocs du jeu
    HashSet<Block> walls; // Ensemble des blocs représentant les murs
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;
   
    private boolean isFrightenedMode = false;
private Timer frightenedModeTimer;
private HashMap<Block, Image> originalGhostImages = new HashMap<>();
private List<Block> eatenGhostsDuringFrightened = new ArrayList<>();
List<FloatingEyes> floatingEyesList = new ArrayList<>();

Set<Point> powerPelletPoints = Set.of(
    new Point(1, 3),
    new Point(17, 2),
    new Point(2, 16),
    new Point(16, 16)
);






    Timer gameLoop; // Timer pour gérer les mises à jour du jeu à intervalle régulier
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();// Générateur aléatoire pour les mouvements des fantômes
    int score = 0;  // Score du joueur
    int lives = 3; // Nombre de vies restantes
    boolean gameOver = false; // Indique si le jeu est terminé

    private JButton exitButton;

    // Constructeur de la classe PacMan
    String selectedCharacter;
    public PacMan(String selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
        if (selectedCharacter.equals("ladypacman")) {
            pacmanUpImage = new ImageIcon(getClass().getResource("./ladyPacmanUp.png")).getImage();
            pacmanDownImage = new ImageIcon(getClass().getResource("./ladyPacmanDown.png")).getImage();
            pacmanLeftImage = new ImageIcon(getClass().getResource("./ladyPacmanLeft.png")).getImage();
            pacmanRightImage = new ImageIcon(getClass().getResource("./ladyPacmanRight.png")).getImage();
        } else {
            pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
            pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
            pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
            pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
        }
        
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
        //this.setLayout(new BorderLayout());
        // Créer un JPanel pour le bouton "Exit"
       // JPanel bottomPanel = new JPanel();
        //bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Aligner le bouton à gauche

        this.setLayout(null);

        // Créer le bouton "Exit"
        
      this.setVisible(true);

       // bottomPanel.add(exitButton);

        // Ajouter le panel avec le bouton "Exit" en bas
       // this.add(bottomPanel, BorderLayout.SOUTH);

        // Afficher la fenêtre
       // this.pack();
        this.setVisible(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        frightenedGhostImage = new ImageIcon(getClass().getResource("./frightenedGhost.png")).getImage();
       
        cherryImage = new ImageIcon(getClass().getResource("./cherry.png")).getImage();
        blackWallImage = new ImageIcon(getClass().getResource("./blackWall.png")).getImage();
        chronometerImage = new ImageIcon(getClass().getResource("./chronometer.png")).getImage();
        deuxcentImage = new ImageIcon(getClass().getResource("./deuxcent.png")).getImage();

        eyesImage = new ImageIcon(getClass().getResource("./eyes.png")).getImage();
        whiteGhostImage = new ImageIcon(getClass().getResource("./whiteGhost.png")).getImage();
        readyImage = new ImageIcon(getClass().getResource("./ready.png")).getImage();

        gameOverImage = new ImageIcon(getClass().getResource("./Gameover.png")).getImage();


        // Animation mort de Pacman
String[] frameNames = {
    "un.png", "deux.png", "trois.png", "quatre.png", "cinq.png", "six.png",
    "sept.png", "huit.png", "neuf.png", "dix.png", "onze.png", "douze.png"
};

for (String name : frameNames) {
    dyingFrames.add(new ImageIcon(getClass().getResource("./" + name)).getImage());
}

        

        // Charge la carte initiale du jeu à partir des données du tableau
        loadMap();

        // Initialise une direction aléatoire pour chaque fantôme
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
         // Configure la boucle principale du jeu avec un intervalle de 50ms entre chaque frame (20 FPS)
        gameLoop = new Timer(44, this); //20fps (1000/50)
        // Démarre la boucle de jeu
        gameLoop.start();

    }

    // Méthode pour charger la carte du jeu
   

    public void loadMap() {
        // 🔄 Réinitialisation des listes
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();
    
        // 🧱 Chargement de la carte
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tileMapChar = tileMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;
    
                switch (tileMapChar) {
                    case 'X':
                        walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                        break;
    
                    case 'b':
                        Block blue = new Block(blueGhostImage, x, y, tileSize, tileSize);
                        blue.isGhost = true;
                        ghosts.add(blue);
                        break;
    
                    case 'o':
                        Block orange = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                        orange.isGhost = true;
                        ghosts.add(orange);
                        break;
    
                    case 'p':
                        Block pink = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                        pink.isGhost = true;
                        ghosts.add(pink);
                        break;
    
                    case 'r':
                        Block red = new Block(redGhostImage, x, y, tileSize, tileSize);
                        red.isGhost = true;
                        ghosts.add(red);
                        break;
    
                    case 'P':
                        if (selectedCharacter.equals("ladypacman")) {
                            pacman = new Block(new ImageIcon(getClass().getResource("./ladyPacmanRight.png")).getImage(), x, y, tileSize, tileSize);
                        } else {
                            pacman = new Block(new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage(), x, y, tileSize, tileSize);
                        }
                        break;
    
                    case ' ':
                        if (r < rowCount - 2) {
                            boolean isPower = powerPelletPoints.contains(new Point(c, r));
                            int size = isPower ? 8 : 4;
                            int offset = (tileSize - size) / 2;
                            foods.add(new Block(null, c * tileSize + offset, r * tileSize + offset, size, size));
                        }
                        break;
                }
            }
        }
    
        // 🕳️ Ajout de murs invisibles sur les 2 dernières lignes
        for (int r = rowCount - 2; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                int x = c * tileSize;
                int y = r * tileSize;
                walls.add(new Block(null, x, y, tileSize, tileSize));
            }
        }
    }
    
    
    

    // Méthode pour dessiner les éléments du jeu
    public void paintComponent(Graphics g) {
        super.paintComponent(g);// Nettoie l'écran avant de dessiner
        draw(g);// Appelle la méthode de dessin des éléments
    }

    public void draw(Graphics g) {

        // 💀 Si Pacman est en train de mourir, dessine l’animation par-dessus tout
// 💀 Si Pacman est en train de mourir, on affiche l’animation
if (isDying && dyingFrameIndex < dyingFrames.size()) {
    Image currentFrame = dyingFrames.get(dyingFrameIndex);
    int w = currentFrame.getWidth(null);
    int h = currentFrame.getHeight(null);

    double scale = 2.0; // 🔥 plus grand mais toujours propre
    int newW = (int)(w * scale);
    int newH = (int)(h * scale);

    int offsetX = (pacman.width - newW) / 2;
    int offsetY = (pacman.height - newH) / 2;

    g.drawImage(currentFrame, dyingX + offsetX, dyingY + offsetY, newW, newH, null);
}







        // 🍒 Dessiner les cerises (vies restantes)
        for (int i = 0; i < lives; i++) {
            g.drawImage(pacmanLeftImage, i * 30 + 10, boardHeight - 40, 24, 24, null);
        }
        // ⏱️ Affichage du chronomètre uniquement pendant frightened mode
if (isFrightenedMode && frightenedTimeRemaining > 0) {
    int chronoX = lives * 30 + 20; // Position après les cerises

    // Fond arrondi derrière le chrono (optionnel)
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRoundRect(chronoX - 5, boardHeight - 45, 90, 36, 10, 10);

    // Image chronomètre
    g.drawImage(chronometerImage, chronoX, boardHeight - chronometerImage.getHeight(null), null);


    // Texte blanc du compteur
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.BOLD, 18));
    g.drawString(frightenedTimeRemaining + "s", chronoX + 50, boardHeight - 20);
}

    
     // Dessine Pac-Man uniquement s’il ne meurt pas
     if (!isDying && !gameOver) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
    }

    
        updateFrightenedGhostBlinking(); // 👈 pour faire clignoter les fantômes

        // Dessine les fantômes
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // 👁️ Dessiner les yeux fantômes en mouvement


    
        // Dessine les murs 
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
    
        // Dessine la nourriture sous forme de petits rectangles blancs
        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        

        
    
        // Dessine les cerises
        // 🍒 Dessine les cerises temporisées (TimedCherry)
for (TimedCherry tc : cherries) {
    g.drawImage(tc.block.image, tc.block.x, tc.block.y, tc.block.width, tc.block.height, null);
}

    
        // 🏆 Affiche le score en bas à droite
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, boardWidth - 120, boardHeight - 10);

    
        // 💀 Affichage du "Game Over" en GRAND au milieu
        if (gameOver && gameOverImage != null) {
            int imageWidth = gameOverImage.getWidth(null);
            int imageHeight = gameOverImage.getHeight(null);
        
            int centerX = (boardWidth - imageWidth) / 2;
            int centerY = 11 * tileSize + (tileSize - imageHeight) / 2;
        
            g.drawImage(gameOverImage, centerX, centerY, null);
        }
        
        // 💥 Affichage de deuxcent.png pendant 2 secondes
if (showDeuxCent) {
    long currentTime = System.currentTimeMillis();
    if (currentTime - deuxCentDisplayStartTime <= 1000) {
        g.drawImage(deuxcentImage, deuxcentX, deuxcentY, null); // taille naturelle
    } else {
        showDeuxCent = false;
    }
}

// 👀 Affiche les yeux fixes pendant 2s
// 👁️ Affiche tous les yeux fantômes
for (FloatingEyes fe : floatingEyesList) {
    int scale = 3; // ou 3 si tu veux encore plus gros
int newWidth = eyesImage.getWidth(null) * scale / 2;
int newHeight = eyesImage.getHeight(null) * scale / 2;

int offsetX = (tileSize - newWidth) / 2;
int offsetY = (tileSize - newHeight) / 2;

g.drawImage(eyesImage, fe.x + offsetX, fe.y + offsetY, newWidth, newHeight, null);

}


// 🔥 READY ! affiché pendant 5s au démarrage
if (showReady && readyImage != null) {
    int imageWidth = readyImage.getWidth(null);
    int imageHeight = readyImage.getHeight(null);

    int centerX = (boardWidth - imageWidth) / 2;
    int centerY = 11 * tileSize + (tileSize - imageHeight) / 2;


    g.drawImage(readyImage, centerX, centerY, null);
}




    }


    private void frightenedGhostApparition() {
        isFrightenedMode = true;
        frightenedTimeRemaining = 6; // ⏱️ Affiche 6s
    
        if (frightenedCountdownTimer != null) frightenedCountdownTimer.stop();
        if (frightenedModeTimer != null) frightenedModeTimer.stop();
    
        for (Block ghost : ghosts) {
            if (!originalGhostImages.containsKey(ghost)) {
                originalGhostImages.put(ghost, ghost.image);
            }
            ghost.image = frightenedGhostImage;
            ghost.updateVelocity(); // 👈 Ralentir tout de suite
        }
    
        frightenedCountdownTimer = new Timer(1000, e -> {
            frightenedTimeRemaining--;
            if (frightenedTimeRemaining <= 0) {
                frightenedCountdownTimer.stop();
            }
            repaint();
        });
        frightenedCountdownTimer.start();
    
        frightenedModeTimer = new Timer(6000, e -> { // ⏳ Réel timer 6s
            isFrightenedMode = false;
    
            for (Block ghost : ghosts) {
                if (originalGhostImages.containsKey(ghost)) {
                    ghost.image = originalGhostImages.get(ghost);
                }
    
                ghost.reset(); // 🔁 Retour à la position de départ
                char newDir = directions[random.nextInt(4)];
                ghost.direction = newDir;
                ghost.updateVelocity(); // vitesse normale selon direction
            }
    
            for (Block eaten : eatenGhostsDuringFrightened) {
                if (!ghosts.contains(eaten)) {
                    ghosts.add(eaten);
                }
    
                if (originalGhostImages.containsKey(eaten)) {
                    eaten.image = originalGhostImages.get(eaten);
                }
    
                eaten.reset(); // 🔁 Retour à la base aussi
                char newDir = directions[random.nextInt(4)];
                eaten.direction = newDir;
                eaten.updateVelocity();
            }
    
            eatenGhostsDuringFrightened.clear();
            originalGhostImages.clear();
    
            frightenedCountdownTimer.stop();
            frightenedModeTimer.stop();
    
            repaint();
        });
    
        frightenedModeTimer.setRepeats(false);
        frightenedModeTimer.start();
    }
    
    
    
    
    private List<TimedCherry> cherries = new ArrayList<>();

private boolean cherrySpawned = false; // Pour éviter de faire apparaître plusieurs cerises

private void spawnCherry() {
    if (!foods.isEmpty()) {
        List<Block> foodList = new ArrayList<>(foods);
        Block randomFood = foodList.get(random.nextInt(foodList.size()));

        Block cherryBlock = new Block(cherryImage, randomFood.x, randomFood.y, 20, 20);
        cherries.add(new TimedCherry(cherryBlock));
        foods.remove(randomFood);
    }
}




    // Méthode pour déplacer Pac-Man et les fantômes
    public void move() {

        

        if (!canPlay) return; // ⛔ empêche tout mouvement si les 5s ne sont pas passées

        // 1. Appliquer la direction demandée si possible
        if (requestedDirection != ' ') {
            int testVX = 0;
            int testVY = 0;
    
            if (requestedDirection == 'U') {
                testVX = 0;
                testVY = -tileSize / 4;
            } else if (requestedDirection == 'D') {
                testVX = 0;
                testVY = tileSize / 4;
            } else if (requestedDirection == 'L') {
                testVX = -tileSize / 4;
                testVY = 0;
            } else if (requestedDirection == 'R') {
                testVX = tileSize / 4;
                testVY = 0;
            }
    
            // Simule un déplacement dans la direction demandée
            Block testMove = new Block(null, pacman.x + testVX, pacman.y + testVY, pacman.width, pacman.height);
    
            boolean canChangeDirection = true;
            for (Block wall : walls) {
                if (collision(testMove, wall)) {
                    canChangeDirection = false;
                    break;
                }
            }
    
            // Si le changement est possible, on l'applique réellement
            if (canChangeDirection) {
                pacman.direction = requestedDirection;
                pacman.updateVelocity();
    
                // Met à jour l’image de Pacman seulement quand la direction a changé
                if (requestedDirection == 'U') {
                    pacman.image = pacmanUpImage;
                } else if (requestedDirection == 'D') {
                    pacman.image = pacmanDownImage;
                } else if (requestedDirection == 'L') {
                    pacman.image = pacmanLeftImage;
                } else if (requestedDirection == 'R') {
                    pacman.image = pacmanRightImage;
                }
    
                firstMoveStarted = true;
            }
        }
    
        // 2. Déplacement réel de Pacman uniquement si le jeu a commencé
        if (firstMoveStarted) {
            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;
    
            // Vérification collision avec murs
            for (Block wall : walls) {
                if (collision(pacman, wall)) {
                    pacman.x -= pacman.velocityX;
                    pacman.y -= pacman.velocityY;
                    break;
                }
            }
        }
    
        // 3. Collisions avec fantômes
        Block ghostEaten = null;
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (isFrightenedMode) {
                    ghostEaten = ghost;
                    score += 200;
                   
            
                    showDeuxCent = true;
                    deuxCentDisplayStartTime = System.currentTimeMillis();
                    deuxcentX = ghost.x;
                    deuxcentY = ghost.y;
            
                    eatenGhostsDuringFrightened.add(ghost);
            
                    // 👁️ Ajout des yeux fantôme qui vont vers une position b/p/o
                    int alignedX = (ghost.x / tileSize) * tileSize;
                    int alignedY = (ghost.y / tileSize) * tileSize;
                    floatingEyesList.add(new FloatingEyes(alignedX, alignedY));
                    
// décalé de 16px à droite


            
                } else {
                    lives -= 1;
if (lives == 0) {
    // 👇 Capture bien la position AVANT toute action
    startDyingAnimation();
    return;
}
resetPositions();

                }
            }
            
    
            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
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
    
        if (ghostEaten != null) {
            ghosts.remove(ghostEaten);
        }
    
        // 4. Collision avec nourriture
        Block foodEaten = null;
       
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
        
                if (food.width == 8 && food.height == 8) {
                    frightenedGhostApparition();
                    playPowerPelletSound();
                } else {
                    score += 10;
        
                    // 🛑 Ne joue PAS le son si on est en mode frightened
                    if (canPlay && !isFrightenedMode) {
                        playEatingSound();
                        lastEatingTime = System.currentTimeMillis();
                        //playSound("Alarm.wav");
                    }
                }
            }
        }
        foods.remove(foodEaten);
        
      
        
    
        // 5. Génération d'une cerise si score atteint
        if (score >= 170 && !cherrySpawned) {
            spawnCherry();
            cherrySpawned = true;
        }
    
        // 6. Niveau terminé = relancer la carte
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
        
        
        // 7. Collision avec cerise
        List<TimedCherry> expiredCherries = new ArrayList<>();
        TimedCherry eatenCherry = null;
        
        for (TimedCherry tc : cherries) {
            if (tc.isExpired()) {
                expiredCherries.add(tc); // ⏳ cherry disparue
            } else if (collision(pacman, tc.block)) {
                eatenCherry = tc;
                score += 100;
                // Optionnel : playSound("Cherry.wav");
            }
        }
        
        cherries.removeAll(expiredCherries);
        if (eatenCherry != null) cherries.remove(eatenCherry);
        

        // 🎧 Si Pacman ne mange plus depuis 500 ms, on coupe le son
if (eatingClip != null && eatingClip.isRunning()) {
    long now = System.currentTimeMillis();
    System.out.println("⏱️ TICK " + now);
    if (now - lastEatingTime > 600) {
        System.out.println("🛑 Pacman ne mange plus → arrêt du son");
        eatingClip.stop();
        eatingClip.close();
        eatingClip = null;
    }
}

for (FloatingEyes fe : floatingEyesList) {
            fe.move();
        }
        floatingEyesList.removeIf(fe -> fe.shouldDisappear());
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
        // 👁️ Déplacement des yeux fantômes


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
    if (!canPlay) return;
    if (gameOver) {
        loadMap();
        resetPositions();
        lives = 3;
        score = 0;
        gameOver = false;
        gameLoop.start();
    }

    // Enregistre la direction demandée
    if (e.getKeyCode() == KeyEvent.VK_UP) {
        requestedDirection = 'U';
    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
        requestedDirection = 'D';
    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
        requestedDirection = 'L';
    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
        requestedDirection = 'R';
    }
}

private void updateFrightenedGhostBlinking() {
    if (!isFrightenedMode) return;

    long elapsed = System.currentTimeMillis() - frightenedModeTimer.getInitialDelay() + frightenedTimeRemaining * 1000L;

    // Si on est dans les 2 dernières secondes
    if (frightenedTimeRemaining <= 4) {
        long currentTime = System.currentTimeMillis();
        boolean blinkWhite = ((currentTime / 500) % 2 == 0); // Alterne toutes les 0.5 secondes

        for (Block ghost : ghosts) {
            if (!eatenGhostsDuringFrightened.contains(ghost)) {
                ghost.image = blinkWhite ? whiteGhostImage : frightenedGhostImage;
            }
        }
    }
}


public void playSound(String fileName) {
    new Thread(() -> {
        try {
            File soundFile = new File(fileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            
           
System.out.println("🔊 Son joué : " + fileName + " à " + System.currentTimeMillis());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }).start();
}

private int dyingX, dyingY;

private void startDyingAnimation() {
    isDying = true;
    dyingFrameIndex = 0;

    // 🧊 Figer la position de Pacman
    dyingX = pacman.x;
    dyingY = pacman.y;

    // 🔇 Stopper tous les autres sons
    if (eatingClip != null && eatingClip.isRunning()) {
        eatingClip.stop();
        eatingClip.close();
        eatingClip = null;
    }

    if (powerPelletClip != null && powerPelletClip.isRunning()) {
        powerPelletClip.stop();
        powerPelletClip.close();
        powerPelletClip = null;
    }

    // 💀 Joue le son de mort
    playDeathSound();

    // ⏱️ Timer pour jouer l'animation
    dyingAnimationTimer = new Timer(100, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            dyingFrameIndex++;
            if (dyingFrameIndex >= dyingFrames.size()) {
                dyingAnimationTimer.stop();
                isDying = false;
                gameOver = true;
            }
            repaint();
        }
    });

    dyingAnimationTimer.start();
}




public void playPowerPelletSound() {
    try {
        if (powerPelletClip != null) {
            powerPelletClip.stop();   // ⛔ Arrête le précédent
            powerPelletClip.close();  // 🚫 Libère la ressource
        }

        File soundFile = new File("PacmanPowerPellet.wav");
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
        powerPelletClip = AudioSystem.getClip();
        powerPelletClip.open(audioIn);
        powerPelletClip.start();     // ▶️ Joue depuis le début
        System.out.println("💥 PowerPelletSound RESET + JOUÉ !");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private Clip deathClip;

public void playDeathSound() {
    try {
        if (deathClip != null && deathClip.isRunning()) {
            deathClip.stop();
            deathClip.close();
        }

        File soundFile = new File("death.wav");
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
        deathClip = AudioSystem.getClip();
        deathClip.open(audioIn);
        deathClip.start();
        System.out.println("💀 Son de mort joué !");
    } catch (Exception e) {
        e.printStackTrace();
    }
}


public void playEatingSound() {
    try {
        // Ne rien faire si le son est déjà en train de jouer
        if (eatingClip != null && eatingClip.isRunning()) return;

        // Sinon, charger et jouer le son
        File soundFile = new File("PacmanEating.wav");
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
        eatingClip = AudioSystem.getClip();
        eatingClip.open(audioIn);
        eatingClip.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
}





}


