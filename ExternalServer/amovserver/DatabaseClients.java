package amovserver;

import amovserver.Response;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bernardovieira
 */
public class DatabaseClients {
    
    private Connection c = null;
    private Statement stmt = null;
        
    public DatabaseClients()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:clients.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully!");
            createTables();
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            Logger.getLogger(DatabaseClients.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close()
    {
        try
        {
            c.close();
        }
        catch (SQLException ex)
        {
            Logger.getLogger(DatabaseClients.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createTables() throws SQLException
    {
        stmt = c.createStatement();
            
        String sql = "CREATE TABLE  IF NOT EXISTS users " +
               "(id             INTEGER PRIMARY KEY   AUTOINCREMENT," +
               " name           TEXT    NOT NULL," + 
               " password       TEXT    NOT NULL," +
               " profile_pic    TEXT    NOT NULL)"; 
        stmt.executeUpdate(sql);

        stmt.close();
        c.commit();

        System.out.println("Tables created!");
    }
    
    public void addUser(String username, String password, String profile_pic)
            throws SQLException
    {
        stmt = c.createStatement();
            
        String sql = "INSERT INTO users(name, password, profile_pic) VALUES('" +
                username + "','" + password + "', '" + profile_pic + "')"; 
        stmt.executeUpdate(sql);

        stmt.close();
        c.commit();
    }
    
    public String getProfilePhotoName(String username)
            throws SQLException
    {
        String found = "";
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT profile_pic FROM users WHERE name='" + username + "'");

        System.out.println("pic for " + username);
        if(rs.next())
        {
            found = rs.getString("profile_pic");
            System.out.println("found " + found);
        }
        rs.close();
        stmt.close();

        return found;
    }
    
    public void removeUser(String username)
            throws SQLException
    {
        stmt = c.createStatement();
            
        String sql = "DELETE FROM users WHERE name = '" + username + "')"; 
        stmt.executeUpdate(sql);

        stmt.close();
        c.commit();
    }
    
    public int checkLogin(String username, String password)
            throws SQLException
    {
        if(!checkUser(username))
            return Response.ERROR;
        
        Integer found = Response.ERROR;
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM users WHERE name='" +
                username + "' AND password='" + password + "'");

        if(rs.next())
        {
            found = Response.OK;
        }
        rs.close();
        stmt.close();

        return found;
    }
    
    public boolean checkUser(String username)
            throws SQLException
    {
        boolean found = false;
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM users WHERE name='" + username + "'");

        if(rs.next())
        {
            found = true;
        }
        rs.close();
        stmt.close();

        return found;
    }
}
