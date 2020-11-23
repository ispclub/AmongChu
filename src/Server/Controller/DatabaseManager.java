/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import Server.Model.UserAccount;
import Server.Model.UserTable;
import Server.Model.UserTableData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author hoang
 */
public class DatabaseManager{
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
    
    public int checkLogin(UserAccount user) throws SQLException
    {
        String query = "SELECT * FROM User_Account WHERE username = '" + user.getUsername()+ "' AND password = '" + user.getPassword() + "'";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (!rs.next())
        {
            return 1;           // Sai tai khoan hoac mat khau
        }
        query = "SELECT * FROM User_Account WHERE username = '" + user.getUsername()+ "' AND isOnline = FALSE";
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
    
    public void setLogin(UserAccount user) throws SQLException
    {
        String query = "UPDATE User_Account set isOnline = TRUE where username = '" + user.getUsername()+ "'";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
        query = "UPDATE User_Account set isPlaying = FALSE where username = '" + user.getUsername()+ "'";
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
    public boolean checkIsLogin(String user) throws SQLException
    {
        String query = "select username from User_Account where isOnline = TRUE;";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs.next();
    }
    public boolean checkIsPlaying(String user) throws SQLException
    {
        String query = "select username from User_Account where isPlaying = TRUE;";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs.next();
    }
    public void setPlaying(String a, String b) throws SQLException
    {
        String query = "UPDATE User_Account set isPlaying = TRUE where (username = '" + a + "' or username = '" + b + "');";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }
    public void setOnline(String a, String b) throws SQLException
    {
        String query = "UPDATE User_Account set isPlaying = FALSE where (username = '" + a + "' or username = '" + b + "');";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }
    public void addPoint(String user) throws SQLException
    {
        String query = "UPDATE User_Account set point = point + 1 where username = '" + user +"'";
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }
}