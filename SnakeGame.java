import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            Tile tile = (Tile) obj;
            return x == tile.x && y == tile.y;
        }
    }

    enum GameMode {
        NORMAL,
        LIMITED_TIME
    }

    enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        PROFESSIONAL
    }

    int boardWidth;
    int boardHeight;
    int tileSize = 25;

    // Snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    // Food
    Tile food;
    Random random;

    // Obstacles
    ArrayList<Tile> obstacles;
    boolean obstaclesEnabled = false;

    // Game logic
    Timer gameLoop;
    int velocityX;
    int velocityY;
    boolean gameOver = false;
    int eatenCount = 0;
    Difficulty difficulty;
    GameMode gameMode;

    // Limited Time mode
    Timer limitedTimeTimer;
    int foodCountNeeded = 4;
    int timeLimitInSeconds = 30;
    int timeLeftInSeconds = timeLimitInSeconds;

    private boolean firstMove = false;
    private boolean gameStarted = false;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        initializeGame();
    }

    private void initializeGame() {
        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();
        food = new Tile(10, 10);
        random = new Random();
        placeFood();
        velocityX = 0;
        velocityY = 0;
        gameLoop = new Timer(500, this);
    }

    private void chooseGameMode() {
        Object[] modeOptions = { "Normal", "Limited Time" };
        int modeChoice = JOptionPane.showOptionDialog(null, "Choose Game Mode", "Game Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, modeOptions, modeOptions[0]);

        switch (modeChoice) {
            case 0:
                gameMode = GameMode.NORMAL;
                break;
            case 1:
                gameMode = GameMode.LIMITED_TIME;
                break;
            default:
                gameMode = GameMode.NORMAL;
        }
    }

    private void chooseDifficulty() {
        Object[] options = { "Easy", "Medium", "Hard", "Professional" };
        int difficultyChoice = JOptionPane.showOptionDialog(null, "Choose Difficulty", "Difficulty",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (difficultyChoice == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        switch (difficultyChoice) {
            case 0:
                difficulty = Difficulty.EASY;
                gameLoop.setDelay(500);
                break;
            case 1:
                difficulty = Difficulty.MEDIUM;
                gameLoop.setDelay(300);
                break;
            case 2:
                difficulty = Difficulty.HARD;
                gameLoop.setDelay(100);
                break;
            case 3:
                difficulty = Difficulty.PROFESSIONAL;
                gameLoop.setDelay(500);
                break;
            default:
                difficulty = Difficulty.EASY;
                gameLoop.setDelay(500);
        }
    }

    private void chooseObstacles() {
        int obstacleChoice = JOptionPane.showConfirmDialog(null, "Do you want to add obstacles?", "Obstacles",
                JOptionPane.YES_NO_OPTION);

        if (obstacleChoice == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        obstaclesEnabled = obstacleChoice == JOptionPane.YES_OPTION;

        if (obstaclesEnabled) {
            obstacles = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                int obstacleX = random.nextInt(boardWidth / tileSize);
                int obstacleY = random.nextInt(boardHeight / tileSize);
                obstacles.add(new Tile(obstacleX, obstacleY));
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        setBackground(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (gameStarted) {
            draw(g);
        }
    }

    private void draw(Graphics g) {
        g.setColor(Color.pink);
        g.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);

        g.setColor(Color.yellow);
        g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);

        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
        }

        if (obstaclesEnabled) {
            g.setColor(Color.gray);
            for (Tile obstacle : obstacles) {
                g.fill3DRect(obstacle.x * tileSize, obstacle.y * tileSize, tileSize, tileSize, true);
            }
        }

        g.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game over: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
        } else {
            g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
        }

        if (gameMode == GameMode.LIMITED_TIME) {
            g.drawString("Time left: " + timeLeftInSeconds, tileSize - 16, tileSize * 2);
            g.drawString("Food collected: " + eatenCount + " / " + foodCountNeeded, tileSize - 16, tileSize * 3);
        }
    }

    private void placeFood() {
        food.x = random.nextInt(boardWidth / tileSize);
        food.y = random.nextInt(boardHeight / tileSize);
    }

    private boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    private boolean collisionWithObstacles(Tile tile) {
        if (obstaclesEnabled) {
            for (Tile obstacle : obstacles) {
                if (collision(tile, obstacle)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void move() {
        if (firstMove) {
            if (collision(snakeHead, food)) {
                snakeBody.add(new Tile(food.x, food.y));
                placeFood();
                eatenCount++;

                if (difficulty == Difficulty.PROFESSIONAL && eatenCount % 2 == 0) {
                    gameLoop.setDelay(gameLoop.getDelay() - 50);
                }
            }

            for (int i = snakeBody.size() - 1; i >= 0; i--) {
                Tile snakePart = snakeBody.get(i);
                if (i == 0) {
                    snakePart.x = snakeHead.x;
                    snakePart.y = snakeHead.y;
                } else {
                    Tile prevSnakePart = snakeBody.get(i - 1);
                    snakePart.x = prevSnakePart.x;
                    snakePart.y = prevSnakePart.y;
                }
            }

            snakeHead.x += velocityX;
            snakeHead.y += velocityY;

            if (collisionWithObstacles(snakeHead)) {
                gameOver = true;
            }

            for (int i = 0; i < snakeBody.size(); i++) {
                Tile snakePart = snakeBody.get(i);
                if (collision(snakeHead, snakePart)) {
                    gameOver = true;
                }
            }

            if (snakeHead.x * tileSize < 0 || snakeHead.x * tileSize > boardWidth ||
                    snakeHead.y * tileSize < 0 || snakeHead.y * tileSize > boardHeight) {
                gameOver = true;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
            if (gameMode == GameMode.LIMITED_TIME) {
                limitedTimeTimer.stop();
            }
            showEndGameButtons(false);
        } else if (gameMode == GameMode.LIMITED_TIME && eatenCount >= foodCountNeeded) {
            gameLoop.stop();
            limitedTimeTimer.stop();
            showEndGameButtons(true);
        }
    }

    private void startLimitedTimeMode() {
        limitedTimeTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeftInSeconds--;
                if (timeLeftInSeconds <= 0 || eatenCount >= foodCountNeeded) {
                    limitedTimeTimer.stop();
                    if (eatenCount >= foodCountNeeded) {
                        showEndGameButtons(true);
                    } else {
                        showEndGameButtons(false);
                    }
                    gameOver = true;
                }
            }
        });
        limitedTimeTimer.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!firstMove) {
            firstMove = true;
            if (gameMode == GameMode.LIMITED_TIME) {
                startLimitedTimeMode();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void restartGame() {
        gameOver = false;
        resetGame();
        placeFood();
        gameLoop.start();
        if (gameMode == GameMode.LIMITED_TIME) {
            timeLeftInSeconds = timeLimitInSeconds;
            startLimitedTimeMode();
        }
    }

    private void resetGame() {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        velocityX = 0;
        velocityY = 0;
        firstMove = false;
        eatenCount = 0;
        timeLeftInSeconds = timeLimitInSeconds;
        if (limitedTimeTimer != null) {
            limitedTimeTimer.stop();
        }
        gameLoop.stop();
    }

    private void showEndGameButtons(boolean win) {
        String message = win ? "You Win!" : "You Lose!";
        JOptionPane.showMessageDialog(null, message);

        Object[] options = { "Play Again", "Menu", "Exit" };
        int choice = JOptionPane.showOptionDialog(null, "Choose an option:", "Game Over",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0: // Play Again
                restartGame();
                break;
            case 1: // Menu
                chooseGameMode();
                chooseDifficulty();
                chooseObstacles();
                restartGame();
                break;
            case 2: // Exit
                System.exit(0);
                break;
            default:
                System.exit(0);
        }
    }

    public static void main(String[] args) {
        int boardWidth = 600;
        int boardHeight = boardWidth;

        JFrame frame = new JFrame("Snake");
        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SnakeGame snakeGame = new SnakeGame(boardWidth, boardHeight);
        frame.add(snakeGame);
        frame.pack();
        snakeGame.requestFocus();

        JOptionPane.showMessageDialog(null, "Welcome to the game!");
        snakeGame.chooseGameMode();
        snakeGame.chooseDifficulty();
        snakeGame.chooseObstacles();
        snakeGame.gameStarted = true;
        snakeGame.gameLoop.start();
    }
}