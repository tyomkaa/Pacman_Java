import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class HighScore extends JFrame {

    private ArrayList<Player> players;

    HighScore() throws IOException {
        players = new ArrayList<>();
        File scoreFile = new File("score.ser");
        if (scoreFile.length() == 0) {
            JOptionPane.showMessageDialog(null, "The file is empty.");
        } else {
            readFile("score.ser", players);
        }
        if (!players.isEmpty()) {
            Collections.sort(players);
            MyList myList = new MyList(players);
            JList jList = new JList(myList);
            jList.setModel(myList);

            JScrollPane scrollPane = new JScrollPane(jList);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);

            JFrame highScoresFrame = new JFrame("High Scores");
            highScoresFrame.setLayout(new BorderLayout());
            highScoresFrame.add(panel, BorderLayout.CENTER);
            highScoresFrame.setSize(500, 500);
            highScoresFrame.setLocationRelativeTo(null);
            highScoresFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            highScoresFrame.setVisible(true);
        }

    }

    public static void readFile(String path, ArrayList<Player> playerArrayList) throws IOException {
        try (FileInputStream fileIn = new FileInputStream(path);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

            while (true) {
                try {
                    Player player = (Player) objectIn.readObject();
                    playerArrayList.add(player);
                } catch (EOFException e) {
                    break;
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
