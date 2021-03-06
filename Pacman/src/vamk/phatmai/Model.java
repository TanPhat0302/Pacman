package vamk.phatmai;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Model extends JPanel implements ActionListener {

	private Dimension d; //describe the height and weight of playing field  
    private final Font smallFont = new Font("Arial", Font.BOLD, 14); //display the text in game
    private boolean inGame = false;// in game check use for game running
    private boolean dying = false;// dying check if pacman still alive

    private int N_GHOSTS = 6;// number of ghost from the beginning
    private int lives, score;
    private int[] dx, dy; // position of ghost
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed; //determinent the number and position of the ghost

    private final int BLOCK_SIZE = 24; //describe how big of block in game
    private final int N_BLOCKS = 15;// indicate number of block in game(15x15)
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;// 15x24
    private final int MAX_GHOSTS = 12; //maximum ghost number
    private final int PACMAN_SPEED = 6; //speed of pacman

   
    private Image heart, ghost; // object image
    private Image up, down, left, right; //pacman animation when moving

    private int pacman_x, pacman_y, pacmand_x, pacmand_y;
    private int req_dx, req_dy;
// Draw map in Pacman with 
//0 is blue,1 is left border, 2 is top border, 4 is right border, 8 is bottom border, 16 is white dot
    private final short mapDrawing[] = {//we have 225 number (15x15)
    	19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 28, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        0,  0,  0,  0,  0,  17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
        17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
        21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
        17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] MapData; // using for re-draw the game
    private Timer timer;

    public Model() {

        imageLoader();
        initVariables();
        addKeyListener(new KeyMove());
        setFocusable(true);
        initGame();//to start the game
    }
    
    
    private void imageLoader() {//import image of the game object
    	down = new ImageIcon("D:/Java/Pacman/src/images/down.gif").getImage();
    	up = new ImageIcon("D:/Java/Pacman//src/images/up.gif").getImage();
    	left = new ImageIcon("D:/Java/Pacman//src/images/left.gif").getImage();
    	right = new ImageIcon("D:/Java/Pacman//src/images/right.gif").getImage();
        ghost = new ImageIcon("D:/Java/Pacman/src/images/ghost.gif").getImage();
        heart = new ImageIcon("D:/Java/Pacman//src/images/heart.PNG").getImage();

    }
       private void initVariables() {//setting value

        MapData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this);// time for re-draw image of game in milisec
        timer.start();
    }

    private void Start(Graphics2D g2d) {

        if (dying) {// when pacman die, function will run

            death();

        } else {//To continue if pacman still alive

            movePacman();
            drawPacman(g2d);
            Ghost_move(g2d);
            Mapchecker();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
 
    	String start = "Run until you die !";
        g2d.setColor(Color.yellow);
        g2d.drawString(start, (SCREEN_SIZE)/8, 150);
    }

    private void drawScore(Graphics2D g) {//score display
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);

        for (int i = 0; i < lives; i++) {// using this loop to check how many live left to display for user
            g.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void Mapchecker() {// for any point left for pacman to eat

        int i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((MapData[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;// if the score increase to 50, the speed of ghost +1

            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            Level();
        }
    }

    private void death() {// if pacman die, life -1 the game still continue until live = 0

    	lives--;

        if (lives == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void Ghost_move(Graphics2D g2d) {

        int pos;
        int count;

        for (int i = 0; i < N_GHOSTS; i++) { //set the position of 6 ghosts by using block_size and number of ghost
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;

                if ((MapData[pos] & 1) == 0 && ghost_dx[i] != 1) {//use border information 1 2 4 8 to determinent how the ghost can move
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((MapData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((MapData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((MapData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((MapData[pos] & 15) == 15) {
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    } else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }

            }

            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);

            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)//if pacman touch the ghost, dying is true, live -1
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {
    	g2d.drawImage(ghost, x, y, this);
        }

    private void movePacman() {

        int pos;
        short ch;

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
            ch = MapData[pos];

            if ((ch & 16) != 0) { //16 is the point that pacman can eat
                MapData[pos] = (short) (ch & 15);
                score++;
            }
//Run this loop to control pacman can run inside the map
            if (req_dx != 0 || req_dy != 0) {// with 1 is the left border, check it so that pacman cant move if pacman on 1 of border
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)// with 4 is the right
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)//2 is the top
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {//8 is the bottom
                    pacmand_x = req_dx;
                    pacmand_y = req_dy;
                }
            }

            // Check for standstill
            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {
                pacmand_x = 0;//if pacman die, set it back to 0
                pacmand_y = 0;
            }
        } 
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }

    private void drawPacman(Graphics2D g2d) { //draw pacman

        if (req_dx == -1) {
        	g2d.drawImage(left, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dx == 1) {
        	g2d.drawImage(right, pacman_x + 1, pacman_y + 1, this);
        } else if (req_dy == -1) {
        	g2d.drawImage(up, pacman_x + 1, pacman_y + 1, this);
        } else {
        	g2d.drawImage(down, pacman_x + 1, pacman_y + 1, this);
        }
    }

    private void drawMaze(Graphics2D g2d) { //2 loop size for screen size and block size

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(new Color(0,72,251));
                g2d.setStroke(new BasicStroke(5));
                
                if ((mapDrawing[i] == 0)) { 
                	g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                 }

                if ((MapData[i] & 1) != 0) { // if 1 the border will draw
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((MapData[i] & 2) != 0) { //2 is the top border
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((MapData[i] & 4) != 0) { // 4 is the right border
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((MapData[i] & 8) != 0) { // 8 is the bottom
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((MapData[i] & 16) != 0) { // 16 is the white dot
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
               }

                i++;
            }
        }
    }

    private void initGame() {//setting for beginning

    	lives = 3;// number of lives
        score = 0;//starting score
        Level();
        N_GHOSTS = 3;// number of ghost
        currentSpeed = 2;
    }

    private void Level() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) { //using for analyze level
            MapData[i] = mapDrawing[i];
        }

        continueLevel();
    }

    private void continueLevel() { //the function continue to defind the position of the ghost

    	int dx = 1;
        int random; //random speed for the ghost

        for (int i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE; //start position
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 6 * BLOCK_SIZE;  //start position
        pacman_y = 11 * BLOCK_SIZE;
        pacmand_x = 0;	//reset direction move
        pacmand_y = 0;
        req_dx = 0;		// reset direction controls
        req_dy = 0;
        dying = false;
    }

 
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);//background color
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            Start(g2d);
        } else {
            showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    //controls
    class KeyMove extends KeyAdapter { //Game controller 

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } 
            } else {
                if (key == KeyEvent.VK_SPACE) {// using for starting the game
                    inGame = true;
                    initGame();
                }
            }
        }
}

	
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
		
	}
