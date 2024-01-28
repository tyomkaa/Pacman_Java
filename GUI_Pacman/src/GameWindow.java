import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class GameWindow extends JFrame implements Serializable {

    private JTable gameTable;
    private CustomTableModel tableModel;

    private int pacmanRow;
    private int pacmanCol;

    private Direction pacmanDirection = Direction.NONE;
    private boolean pacmanMouthOpen = false;

    private JLabel livesLabel;
    private JLabel scoreLabel;
    private JLabel timeLabel;

    private int score = 0;

    private MyTimer gameTimer;

    private int[][] ghostPositions = new int[5][2];

    private int lives = 3;

    private Thread pacmanMovementThread;

    private boolean speedBoostActive = false;
    private int speedBoostDuration = 0;

    private boolean invincibleActive = false;
    private int invincibleDuration = 0;

    private boolean canDestroyWalls = false;
    private int wallDestroyingDuration = 0;


    public GameWindow(int size) {
        setTitle("Pacman Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tableModel = new CustomTableModel(size);
        gameTable = new JTable(tableModel);
        int cellSize = 20;
        Dimension tableSize = new Dimension(size * cellSize, size * cellSize);
        gameTable.setPreferredScrollableViewportSize(tableSize);
        gameTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(gameTable);
        add(scrollPane);

        gameTable.setDefaultRenderer(Cell.class, new CellRenderer());

        placePacman();
        gameTable.repaint();

        gameTimer = new MyTimer();

        gameTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.VK_UP:
                        pacmanDirection = Direction.UP;
                        break;
                    case KeyEvent.VK_DOWN:
                        pacmanDirection = Direction.DOWN;
                        break;
                    case KeyEvent.VK_LEFT:
                        pacmanDirection = Direction.LEFT;
                        break;
                    case KeyEvent.VK_RIGHT:
                        pacmanDirection = Direction.RIGHT;
                        break;
                }
                if ((e.getKeyCode() == KeyEvent.VK_Q) && e.isControlDown() && e.isShiftDown()) {
                    serializeScore();
                    MainMenu mainMenu = new MainMenu();
                    mainMenu.setVisible(true);
                    Component component = (Component) e.getSource();
                    Window window = SwingUtilities.windowForComponent(component);
                    window.dispose();
                }
            }
        });

        initializePacmanMovement();

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 3));

        livesLabel = new JLabel("Lives: " + lives);
        scoreLabel = new JLabel("Score: 0");
        timeLabel = new JLabel("Time: 0");

        statusPanel.setBorder(BorderFactory.createEtchedBorder());

        statusPanel.add(livesLabel);
        statusPanel.add(scoreLabel);
        statusPanel.add(timeLabel);

        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        startPacmanAnimation();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializePacmanMovement() {
        pacmanMovementThread = new Thread(() -> {
            while (true) {
                try {
                    if (speedBoostActive) {
                        Thread.sleep(150);
                    } else {
                        Thread.sleep(300);
                    }
                    SwingUtilities.invokeLater(this::updatePacmanPosition);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        pacmanMovementThread.start();
    }

    private void updatePacmanPosition() {
        if (pacmanDirection != Direction.NONE) {
            if (speedBoostActive && speedBoostDuration > 0) {
                speedBoostDuration -= 300;
            } else {
                speedBoostActive = false;
            }
            if (invincibleActive && invincibleDuration > 0) {
                invincibleDuration -= 300;
            } else {
                invincibleActive = false;
            }
            if (canDestroyWalls && wallDestroyingDuration > 0) {
                wallDestroyingDuration -= 300;
            } else {
                canDestroyWalls = false;
            }
            movePacman();
        }
    }

    public void updateLives(int lives) {
        livesLabel.setText("Lives: " + lives);
    }

    public void updateScore(int score) {
        scoreLabel.setText("Score: " + score);
    }

    private void updateTime() {
        long elapsedTime = gameTimer.getElapsedTime();
        int seconds = (int) (elapsedTime / 1000);
        timeLabel.setText("Time: " + seconds + "s");
    }

    private void startPacmanAnimation() {
        Thread pacmanAnimationThread = new Thread(() -> {
            boolean mouthOpen = false;
            while (true) {
                try {
                    Thread.sleep(300);
                    mouthOpen = !mouthOpen;
                    Cell newPacmanState = mouthOpen ? Cell.PACMAN_OPEN : Cell.PACMAN_CLOSED;
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setCell(pacmanRow, pacmanCol, newPacmanState);
                        gameTable.repaint();
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        pacmanAnimationThread.start();

        Thread scoreThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    score += 10;
                    SwingUtilities.invokeLater(() -> updateScore(score));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        scoreThread.start();

        Thread timeThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    updateTime();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        timeThread.start();

            Thread ghostMovementThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(500);
                        SwingUtilities.invokeLater(() -> {
                            tableModel.moveGhosts();
                            gameTable.repaint();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
            ghostMovementThread.start();

        Thread powerUpSpawnerThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    SwingUtilities.invokeLater(() -> tableModel.spawnPowerUp());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        powerUpSpawnerThread.start();


        gameTimer.start();
    }

    private void movePacman() {
        int newRow = pacmanRow;
        int newCol = pacmanCol;

        switch (pacmanDirection) {
            case UP:
                newRow--;
                break;
            case DOWN:
                newRow++;
                break;
            case LEFT:
                newCol--;
                break;
            case RIGHT:
                newCol++;
                break;
            default:
                break;
        }

        if (newRow >= 0 && newRow < tableModel.getRowCount() &&
                newCol >= 0 && newCol < tableModel.getColumnCount()) {
            Cell cell = tableModel.getCell(newRow, newCol);

            if (isPowerUp(cell)) {
                applyPowerUp(cell);
                tableModel.setCell(newRow, newCol, Cell.EMPTY);
            }

            if (canDestroyWalls && cell == Cell.WALL) {
                tableModel.setCell(pacmanRow, pacmanCol, Cell.EMPTY);
                pacmanRow = newRow;
                pacmanCol = newCol;
            }

            if (cell == Cell.EMPTY || cell == Cell.GHOST) {
                tableModel.setCell(pacmanRow, pacmanCol, Cell.EMPTY);
                pacmanRow = newRow;
                pacmanCol = newCol;

                if (cell == Cell.GHOST) {
                    loseLifeAndRespawn();
                }
            }
        }

        tableModel.setCell(pacmanRow, pacmanCol, pacmanMouthOpen ? Cell.PACMAN_OPEN : Cell.PACMAN_CLOSED);

        Rectangle oldRect = gameTable.getCellRect(pacmanRow, pacmanCol, false);
        Rectangle newRect = gameTable.getCellRect(pacmanRow, pacmanCol, false);

        gameTable.repaint(oldRect.union(newRect));
    }

    private boolean isPowerUp(Cell cell) {
        return cell == Cell.SPEED_BOOST || cell == Cell.INVINCIBLE ||
                cell == Cell.DESTROY_WALL || cell == Cell.EXTRA_LIFE ||
                cell == Cell.EXTRA_SCORE;
    }

    private void applyPowerUp(Cell powerUp) {
        switch (powerUp) {
            case SPEED_BOOST:
                speedBoostActive = true;
                speedBoostDuration = 10000;
                System.out.println("speed boost");
                break;
            case INVINCIBLE:
                invincibleActive = true;
                invincibleDuration = 10000;
                System.out.println("invincible");
                break;
            case DESTROY_WALL:
                canDestroyWalls = true;
                wallDestroyingDuration = 7000;
                System.out.println("wall destroying");
                break;
            case EXTRA_LIFE:
                lives++;
                updateLives(lives);
                System.out.println("extra life");
                break;
            case EXTRA_SCORE:
                score += 15;
                updateScore(score);
                System.out.println("extra score");
                break;
        }
    }

    private void loseLifeAndRespawn() {
        if (!invincibleActive) {
            lives--;
            updateLives(lives);
            if (lives > 0) {
                placePacman();
            } else {
                serializeScore();
                dispose();
                MainMenu mainMenu = new MainMenu();
                mainMenu.setVisible(true);
            }
        }
    }

    private void placePacman() {
        do {
            pacmanRow = (int) (Math.random() * tableModel.getRowCount());
            pacmanCol = (int) (Math.random() * tableModel.getColumnCount());
        } while (tableModel.getCell(pacmanRow, pacmanCol) != Cell.EMPTY);
        tableModel.setCell(pacmanRow, pacmanCol, Cell.PACMAN);
    }

    public class CustomTableModel extends AbstractTableModel {
        private Cell[][] board;

        public CustomTableModel(int size) {
            board = new Cell[size][size];
            initializeBoard();
        }

        private void initializeBoard() {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    board[i][j] = Math.random() < 0.2 ? Cell.WALL : Cell.EMPTY;
                }
            }
            for (int i = 0; i < ghostPositions.length; i++) {
                placeGhost(i);
            }
        }

        private void placeGhost(int ghostIndex) {
            int row, col;
            do {
                row = (int) (Math.random() * board.length);
                col = (int) (Math.random() * board[0].length);
            } while (board[row][col] != Cell.EMPTY);
            board[row][col] = Cell.GHOST;
            ghostPositions[ghostIndex][0] = row;
            ghostPositions[ghostIndex][1] = col;
        }

        private void moveGhosts() {
            for (int i = 0; i < ghostPositions.length; i++) {
                int row = ghostPositions[i][0];
                int col = ghostPositions[i][1];

                board[row][col] = Cell.EMPTY;

                int newRow = row + (Math.random() < 0.5 ? -1 : 1);
                int newCol = col + (Math.random() < 0.5 ? -1 : 1);

                if (newRow >= 0 && newRow < board.length &&
                        newCol >= 0 && newCol < board[0].length &&
                        board[newRow][newCol] == Cell.EMPTY) {
                    ghostPositions[i][0] = newRow;
                    ghostPositions[i][1] = newCol;
                }

                board[ghostPositions[i][0]][ghostPositions[i][1]] = Cell.GHOST;
            }
            fireTableDataChanged();
        }

        private void spawnPowerUp() {
            Cell[] powerUps = {Cell.SPEED_BOOST, Cell.INVINCIBLE, Cell.DESTROY_WALL, Cell.EXTRA_LIFE, Cell.EXTRA_SCORE};
            int powerUpIndex = (int) (Math.random() * powerUps.length);

            int row, col;
            do {
                row = (int) (Math.random() * board.length);
                col = (int) (Math.random() * board[0].length);
            } while (board[row][col] != Cell.EMPTY);

            board[row][col] = powerUps[powerUpIndex];
            fireTableCellUpdated(row, col);
        }



        @Override
        public int getRowCount() {
            return board.length;
        }

        @Override
        public int getColumnCount() {
            return board[0].length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return board[rowIndex][columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Cell.class;
        }

        public Cell getCell(int row, int col) {
            return board[row][col];
        }

        public void setCell(int row, int col, Cell cell) {
            board[row][col] = cell;
            fireTableCellUpdated(row, col);
        }
    }

    public class CellRenderer extends DefaultTableCellRenderer {
        private Cell currentState;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            currentState = (Cell) value;
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setText("");
            setIcon(null);

            if (currentState == Cell.WALL) {
                setBackground(Color.blue);
            } else if (currentState == Cell.EMPTY) {
                setBackground(Color.black);
            } else if (currentState == Cell.PACMAN_OPEN || currentState == Cell.PACMAN_CLOSED) {
                setBackground(Color.BLACK);
            } else if (currentState == Cell.GHOST) {
                setBackground(Color.RED);
            } else {
                setBackground(Color.black);
            }
            return c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            if (currentState == Cell.PACMAN_OPEN || currentState == Cell.PACMAN_CLOSED) {
                drawPacman(g, currentState == Cell.PACMAN_OPEN);
            }
            if (currentState == Cell.GHOST) {
                drawGhost(g);
            }
            if (currentState == Cell.SPEED_BOOST || currentState == Cell.INVINCIBLE ||
                    currentState == Cell.DESTROY_WALL || currentState == Cell.EXTRA_LIFE ||
                    currentState == Cell.EXTRA_SCORE) {
                drawPowerUp(g);
            }
        }

        private void drawPowerUp(Graphics g) {
            int diameter = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            g.setColor(Color.GREEN);
            g.fillOval(x, y, diameter, diameter);
        }

        private void drawGhost(Graphics g) {
            int diameter = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            // Eyes
            g.fillOval(x + diameter / 4, y + diameter / 4, diameter / 6, diameter / 6);
            g.fillOval(x + diameter * 5 / 8, y + diameter / 4, diameter / 6, diameter / 6);

            //Menacing grin
            g.setColor(Color.BLACK);
            g.drawLine(x + diameter / 4, y + diameter / 2, x + diameter / 2, y + 3 * diameter / 4);
            g.drawLine(x + diameter / 2, y + 3 * diameter / 4, x + 3 * diameter / 4, y + diameter / 2);

            //Eyebrows
            g.drawLine(x + diameter / 4, y + diameter / 4, x + diameter / 2, y + diameter / 4);
            g.drawLine(x + diameter / 2, y + diameter / 4, x + 3 * diameter / 4, y + diameter / 4);
        }


        private void drawPacman(Graphics g, boolean mouthOpen) {
            int diameter = Math.min(getWidth(), getHeight()) - 2;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            g.setColor(Color.YELLOW);
            g.fillOval(x, y, diameter, diameter);

            if (mouthOpen) {
                g.setColor(getBackground());
                int startAngle = -50;
                int arcAngle = 120;

                if (pacmanDirection == Direction.LEFT) {
                    startAngle = -245;
                } else if (pacmanDirection == Direction.UP) {
                    startAngle = -325;
                } else if (pacmanDirection == Direction.DOWN) {
                    startAngle = -153;
                }

                g.fillArc(x, y, diameter, diameter, startAngle, arcAngle);
            }
        }
    }





    public enum Cell {
        EMPTY, WALL, PACMAN, GHOST, PACMAN_OPEN, PACMAN_CLOSED, SPEED_BOOST, INVINCIBLE, DESTROY_WALL, EXTRA_LIFE, EXTRA_SCORE
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT, NONE
    }


    private void serializeScore() {
        String playerName = JOptionPane.showInputDialog(null, "Enter your name:");
        Player player = new Player(playerName, score);

        ArrayList<Player> playerArrayList = new ArrayList<>();
        try {
            HighScore.readFile("score.ser", playerArrayList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        playerArrayList.add(player);

        try (FileOutputStream fileOut = new FileOutputStream("score.ser");
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {

            for (Player p : playerArrayList) {
                objectOut.writeObject(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
