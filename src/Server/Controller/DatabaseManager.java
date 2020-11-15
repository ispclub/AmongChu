/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Server.Model.User;
import Server.Model.UserTable;
import Server.Model.UserTableData;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author hoang
 */
public class DatabaseManager implements Serializable{
    private String dbUrl = "jdbc:mysql://localhost:3306/ltm";
    private String dbClass = "com.mysql.cj.jdbc.Driver";
    private Connection con;
    public DatabaseManager() throws SQLException
    {
        try 
        {
            Class.forName(dbClass);
            con = DriverManager.getConnection (dbUrl, "root", "admin");
        }
        catch(ClassNotFoundException | SQLException e) 
        {
            System.out.println("Database connect failed");
            System.exit(-1);
        }
        //need to update later
        String query = "UPDATE User_Account set isOnline = FALSE AND isPlaying = FALSE;";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }
    
    public int checkLogin(User user) throws SQLException
    {
        String query = "SELECT * FROM User_Account WHERE username = '" + user.getAccount_id() + "' AND password = '" + user.getPassword() + "'";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next())
        {
            return 1;           // Sai tai khoan hoac mat khau
        }
        query = "SELECT * FROM User_Account WHERE username = '" + user.getAccount_id() + "' AND isOnline = FALSE";
        rs = stmt.executeQuery(query);
        if (rs.next())
        {
            return 0;           // OK
        }
        return 2;               // Da login o noi khac
    }
    
    public void setLogout(String user) throws SQLException
    {
        if (user.isEmpty())
            return;
        String query = "UPDATE User_Account set isOnline = FALSE where username = '" + user + "'";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }
    
    public void setLogin(User user) throws SQLException
    {
        String query = "UPDATE User_Account set isOnline = TRUE where username = '" + user.getAccount_id() + "'";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
        query = "UPDATE User_Account set isPlaying = FALSE where username = '" + user.getAccount_id() + "'";
        stmt.executeUpdate(query);
    }
    
    public UserTable getUserTable() throws SQLException
    {
        UserTable ut = new UserTable();
        String query = "select username, point, isPlaying from User_Account where isOnline = TRUE;";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next())
        {
            UserTableData utd = new UserTableData();
            utd.setUsername(rs.getString(1));
            utd.setPoint(rs.getInt(2));
            utd.setStatus(rs.getBoolean(3) ? UserTableData.STATUS.PLAYING : UserTableData.STATUS.ONLINE);
            ut.addElement(utd);
        }
        ut.Sort();
        return ut;
    }
    public boolean clientNeedUpdate(String username) throws SQLException
    {
        String query = "select username from User_Account where (username = '" + username + "' and isOnline = TRUE and isPlaying = FALSE);";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next())
        {
            return false;
        }
        return true;
    }
}
/*
create database ltm;
CREATE TABLE User_Account (
    AccountID int NOT NULL AUTO_INCREMENT,
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    PRIMARY KEY (AccountID)

);
 */