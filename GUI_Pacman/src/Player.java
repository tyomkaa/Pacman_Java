import java.io.Serializable;

public class Player implements Comparable<Player>, Serializable {

    String name;
    int result;

    public Player(String name, int result) {
        this.name = name;
        this.result = result;
    }

    @Override
    public String toString() {
        return name + ": " + result;
    }

    @Override
    public int compareTo(Player o) {
        if (o.result > this.result){
            return 1;
        }else if (o.result < this.result){
            return -1;
        }
        else
            return 0;
    }
}
