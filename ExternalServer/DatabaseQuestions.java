

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import a21240068.isec.nerdquiz.Objects.DownloadQuestion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bernardovieira
 */
public class DatabaseQuestions {
    
    private Connection c = null;
    private Statement stmt = null;
        
    public DatabaseQuestions()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:questions.db");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully!");
            createTables();
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            Logger.getLogger(DatabaseQuestions.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(DatabaseQuestions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void createTables() throws SQLException
    {
        stmt = c.createStatement();
            
        String sql = "CREATE TABLE IF NOT EXISTS questions " +
               "(type           TEXT    NOT NULL," +
               " question       TEXT    NOT NULL," + 
               " ranswer         TEXT    NOT NULL," +
               " version        INT     NOT NULL)";
        stmt.executeUpdate(sql);

        stmt.close();
        c.commit();

        System.out.println("Tables created!");
    }
    
    public ArrayList<DownloadQuestion> getQuestions(int min_version) throws SQLException
    {
        ArrayList<DownloadQuestion> questions = new ArrayList<>();
        stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM questions WHERE version > " + min_version);

        DownloadQuestion question;
        while(rs.next())
        {
            question = new DownloadQuestion(
                rs.getString("type"),
                rs.getString("question"),
                rs.getString("ranswer"));
            
            questions.add(question);
        }
        rs.close();
        stmt.close();

        return questions;
    }
    
    public int getLastVersionNumber() throws SQLException
    {
        int version = 0;
        
        ResultSet rs = stmt.executeQuery( "SELECT DISTINCT MAX(version) FROM questions");
        if(rs.next())
        {
            version = rs.getInt(1);
        }
        rs.close();
        stmt.close();
        
        return version;
    }
    
}
