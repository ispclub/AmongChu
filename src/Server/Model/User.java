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
public class User implements  Serializable{
    private String account_id;
    private String password;

    public String getAccount_id() {
        return account_id;
    }

    public String getPassword() {
        return password;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User() {
    }

    public User(String account_id, String password) {
        this.account_id = account_id;
        this.password = password;
    }
    
}
