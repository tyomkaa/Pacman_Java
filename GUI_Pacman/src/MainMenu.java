import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainMenu extends JFrame implements ActionListener {

    JLabel welcomeText;

    JButton newGame;
    JButton highScores;
    JButton exit;

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String filePath) {
            try {
                backgroundImage = Toolkit.getDefaultToolkit().getImage(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
            }
        }
    }

    MainMenu() {
        this.setTitle("Pacman");
        this.setSize(500, 500);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        BackgroundPanel backgroundPanel = new BackgroundPanel("background.png");
        backgroundPanel.setLayout(null);

        welcomeText = new JLabel();
        welcomeText.setText("Welcome in Pacman :)");
        welcomeText.setHorizontalAlignment(JLabel.CENTER);
        welcomeText.setVerticalAlignment(JLabel.TOP);
        welcomeText.setFont(new Font("MV Boli", Font.PLAIN, 25));
        welcomeText.setOpaque(true);
        backgroundPanel.add(welcomeText);

        newGame = new JButton();
        newGame.setText("New Game");
        newGame.setFocusable(false);
        newGame.setBackground(new Color(126, 34, 75));
        newGame.setFont(new Font("MV Boli", Font.ITALIC, 20));
        newGame.setBounds(170, 100, 150, 50);
        newGame.addActionListener(this);
        backgroundPanel.add(newGame);

        highScores = new JButton();
        highScores.setText("High Scores");
        highScores.setFocusable(false);
        highScores.setBackground(new Color(221, 150, 83));
        highScores.setFont(new Font("MV Boli", Font.ITALIC, 20));
        highScores.setBounds(170, 170, 150, 50);
        highScores.addActionListener(this);
        backgroundPanel.add(highScores);

        exit = new JButton();
        exit.setText("Exit");
        exit.setFocusable(false);
        exit.setBackground(Color.RED);
        exit.setFont(new Font("MV Boli", Font.ITALIC, 20));
        exit.setBounds(170, 240, 150, 50);
        exit.addActionListener(this);
        backgroundPanel.add(exit);




        this.setContentPane(backgroundPanel);
        this.setVisible(true);
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exit) {
            System.exit(1);
        } else if (e.getSource() == highScores) {
            try {
                new HighScore();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } else if (e.getSource() == newGame) {
            openNewGameWindow();
            this.dispose();
        }
    }

    private void openNewGameWindow() {
        String sizeInput = JOptionPane.showInputDialog("Enter the size of table (from 10 do 100):");

        try {
            int size = Integer.parseInt(sizeInput);

            if (size >= 10 && size <= 100) {
                new GameWindow(size);
            } else {
                JOptionPane.showMessageDialog(this, "Wrong size.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Wrong size.");
        }
    }


}

