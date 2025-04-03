
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

    // Classe pour gérer les yeux des fantômes qui retournent à la Ghost House
    class FloatingEyes {
        int x, y;
        long spawnTime;
    
        FloatingEyes(int x, int y) {
            this.x = x;
            this.y = y;
            this.spawnTime = System.currentTimeMillis();
        }
    
        boolean isExpired() {
            return System.currentTimeMillis() - spawnTime > 2000;
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
        "X       bpo       X",
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
        exitButton = new JButton("Exit");
        exitButton.setBounds(boardWidth - 200, boardHeight - 40, 80, 30); //
        this.add(exitButton);
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
                        ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'o':
                        ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'p':
                        ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'r':
                        ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
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
                            if (r < rowCount - 2) {
                                boolean isPower = powerPelletPoints.contains(new Point(c, r));
                            
                                int size = isPower ? 8 : 4;
                                int offset = (tileSize - size) / 2;
                            
                                foods.add(new Block(null, c * tileSize + offset, r * tileSize + offset, size, size));
                            }
                            
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
        // 🍒 Dessiner les cerises (vies restantes)
        for (int i = 0; i < lives; i++) {
            g.drawImage(cherryImage, i * 30 + 10, boardHeight - 40, 24, 24, null);
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

    
        // Dessine Pac-Man à ses coordonnées actuelles
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
    
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
        for (Block cherry : cherries) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }
    
        // 🏆 Affiche le score en bas à droite
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, boardWidth - 120, boardHeight - 10);

    
        // 💀 Affichage du "Game Over" en GRAND au milieu
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.setColor(Color.RED);
            String message = "GAME OVER";
            int textWidth = g.getFontMetrics().stringWidth(message);
            int textHeight = g.getFontMetrics().getHeight();
            g.drawString(message, (boardWidth - textWidth) / 2, (boardHeight - textHeight) / 2);
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
List<FloatingEyes> expiredEyes = new ArrayList<>();
for (FloatingEyes fe : floatingEyesList) {
    if (fe.isExpired()) {
        expiredEyes.add(fe);
    } else {
        int row = fe.y / tileSize;
        int col = fe.x / tileSize;

        // ✅ Condition 1 : la position est dans la map valide
        if (row < rowCount - 2 && col >= 0 && col < columnCount) {
            char tileChar = tileMap[row].charAt(col);

            // ✅ Condition 2 : la case est un couloir (pastille ou vide, pas mur)
            boolean isWalkable = tileChar == ' ' || tileChar == 'P' || tileChar == 'b' || tileChar == 'o' || tileChar == 'p' || tileChar == 'r';

            // ✅ Condition 3 : distance avec Pacman < 5 tiles (manhattan)
            int pacRow = pacman.y / tileSize;
            int pacCol = pacman.x / tileSize;
            int dist = Math.abs(row - pacRow) + Math.abs(col - pacCol);

            if (isWalkable && dist <= 5) {
                int offsetX = (tileSize - eyesImage.getWidth(null)) / 2;
                int offsetY = (tileSize - eyesImage.getHeight(null)) / 2;
                g.drawImage(eyesImage, fe.x + offsetX, fe.y + offsetY, null);
            }
        }
    }
}
floatingEyesList.removeAll(expiredEyes);

// 🔥 READY ! affiché pendant 5s au démarrage
if (showReady) {
    g.setFont(new Font("Arial", Font.BOLD, 40));
    g.setColor(Color.YELLOW);
    String message = "READY!";
    int textWidth = g.getFontMetrics().stringWidth(message);
    g.drawString(message, (boardWidth - textWidth) / 2, boardHeight / 2 + 20);
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
            }
    
            for (Block eaten : eatenGhostsDuringFrightened) {
                if (!ghosts.contains(eaten)) {
                    ghosts.add(eaten);
                }
                if (originalGhostImages.containsKey(eaten)) {
                    eaten.image = originalGhostImages.get(eaten);
                }
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
    
    
    
    
    
    
    
    
private List<Block> cherries = new ArrayList<>(); // Liste des cerises
private boolean cherrySpawned = false; // Pour éviter de faire apparaître plusieurs cerises

private void spawnCherry() {
    if (!foods.isEmpty()) {
        List<Block> foodList = new ArrayList<>(foods); // Convertir le HashSet en ArrayList
        Random rand = new Random();
        int randomIndex = rand.nextInt(foodList.size()); // Index aléatoire
        Block randomFood = foodList.get(randomIndex); // Pastille aléatoire

        // Crée une cerise à la position de la pastille
        Block cherry = new Block(cherryImage, randomFood.x, randomFood.y, 20, 20);
        cherries.add(cherry); // Ajoute la cerise à la liste
        foods.remove(randomFood); // Retire la pastille de la liste des pastilles
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
                    floatingEyesList.add(new FloatingEyes(ghost.x + tileSize / 2, ghost.y)); // décalé de 16px à droite


            
                } else {
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true;
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
        Block cherryEaten = null;
        for (Block cherry : cherries) {
            if (collision(pacman, cherry)) {
                cherryEaten = cherry;
                score += 100;
               
               // frightenedGhostApparition();
            }
        }
        cherries.remove(cherryEaten);

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

