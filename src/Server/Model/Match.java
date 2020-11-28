/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Model;

import java.io.Serializable;

/**
 *
 * @author trminhnhat99
 */
public class Match implements Serializable{
    private String player_1, player_2;
    private int score_1 = 0, score_2 = 0;

    public Match(String player_1, String player_2) {
        this.player_1 = player_1;
        this.player_2 = player_2;
    }

    public boolean setPlayer(String p)
    {
        if (player_1.isEmpty())
        {
            player_1 = p;
            return true;
        }
        if (player_2.isEmpty())
        {
            player_2 = p;
            return true;
        }
        return false;
    }
    public boolean setPoint(String player, int p)
    {
        if (player.equals(player_1))
        {
            score_1 = p;
            return true;
        }
        if (player.equals(player_2))
        {
            score_2 = p;
            return true;
        }
        return false;
    }
    public String getWinner()
    {
        if (player_1.isEmpty() || player_2.isEmpty())
            return null;
        if (score_1 * score_2 == 0)
            return null;
        if (score_1 == score_2)
            return (String)"\n";
        return (score_1 > score_2 ? player_1 : player_2);
    }
    
    public String getOther(String cur)
    {
        if (player_1.equals(cur))
            return player_2;
        return player_1;
    }

    public String getPlayer_1() {
        return player_1;
    }

    public String getPlayer_2() {
        return player_2;
    }
}
