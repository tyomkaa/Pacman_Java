import javax.swing.*;
import java.util.ArrayList;

public class MyList extends AbstractListModel {

    public ArrayList<Player> players;

    public MyList(ArrayList<Player> players) {
        this.players = players;
    }

    @Override
    public int getSize() {
        return players.size();
    }

    @Override
    public Object getElementAt(int index) {
        return players.get(index);
    }
}
