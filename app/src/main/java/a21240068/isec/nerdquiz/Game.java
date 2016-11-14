package a21240068.isec.nerdquiz;

import java.util.Date;

/**
 * Created by bernardovieira on 14-11-2016.
 */

public class Game {

    private String opponent_name;
    private int opponent_points;
    private int player_points;
    private String date;

    public Game(String opponent_name, int opponent_points, int player_points, String date)
    {
        this.opponent_name = opponent_name;
        this.opponent_points = opponent_points;
        this.player_points = player_points;
        this.date = date;
    }

    public String getOpponentName() {
        return opponent_name;
    }

    public int getOpponentPoints() {
        return opponent_points;
    }

    public int getPlayerPoints() {
        return player_points;
    }

    public String getDate() {
        return date;
    }
}
