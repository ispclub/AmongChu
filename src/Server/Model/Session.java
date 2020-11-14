/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Model;

/**
 *
 * @author hoang
 */
public class Session {
    private boolean isLogged = false;
    private int account_id = 0;
    private boolean isPlaying = false;
    private String accountID;
    private int point;

    public String getAccountID() {
        return accountID;
    }

    public int getPoint() {
        return point;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public boolean isIsLogged() {
        return isLogged;
    }

    public int getAccount_id() {
        return account_id;
    }

    public boolean isIsPlaying() {
        return isPlaying;
    }

    public void setIsLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public Session() {
    }
    
}
