/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Model;

import java.io.Serializable;

/**
 *
 * @author hoang
 */
public class UserTableData implements Serializable {

    private String username;
    private int point;

    public enum STATUS {
        ONLINE,
        PLAYING;
    }
    private STATUS status;

    public UserTableData() {
    }

    public UserTableData(String username, int point, STATUS status) {
        this.username = username;
        this.point = point;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public int getPoint() {
        return point;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

}
