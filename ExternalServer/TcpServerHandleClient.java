

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bernardovieira
 */

import a21240068.isec.nerdquiz.Objects.Profile;
import a21240068.isec.nerdquiz.Objects.DownloadQuestion;
import amovserver.DatabaseClients;
import amovserver.Response;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

class Command
{
    public static String    REGISTER    = "register";
    public static String    LOGIN       = "login";
    public static String    AUTO_LOGIN  = "autologin";
    public static String    PLAY        = "play";
    public static String    SEARCH      = "search";
    public static String    INVITE      = "invite";
    public static String    INVITED     = "beinvited";
    public static String    NEW_GAME    = "new_game";
    //public static String    ANSWER    = "answer";
    public static String    REJECT_INV  = "reject";
    public static String    ACCEPT_INV  = "accept";
    public static String    UPDATE_DB   = "updatedb";
    public static String    UPDATE_PASS = "updatepass";
    
    public static String    JOINED      = "joined";
    public static String    LEAVED      = "leaved";
    
    public static String    GET_PPIC    = "getppic";
    public static String    REMOVE_PPIC = "removeppic";
    public static String    PROFILE_PIC_UP     = "profilepup";
    public static String    PROFILE_PIC_DOWN   = "profilepdown";
}

public class TcpServerHandleClient implements Runnable {

    private final Player player;
    private final DatabaseClients database;
    private final DatabaseQuestions db_questions;
    
    private ObjectOutputStream ooStream;
    private ObjectInputStream oiStream;
    
    public TcpServerHandleClient(Player player, DatabaseClients database)
    {
        this.player = player;
        this.database = database;
        this.db_questions = new DatabaseQuestions();
    }
    
    @Override
    public void run()
    {
        try
        {
            String command;
            Socket socket = player.getSocket();
            oiStream = new ObjectInputStream(socket.getInputStream());
            ooStream = new ObjectOutputStream(socket.getOutputStream());
            player.setOoStream(ooStream);
            
            do
            {
                command = (String)oiStream.readObject();
                //show command
                System.out.println(command);
                //
                executeCommand(command);
                
            }while(!command.equals("finish"));
            
            oiStream.close();
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.out.println("Erro na base de dados!");
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(TcpServerHandleClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            database.close();
        }
    }
    
    private void executeCommand(String command)
            throws IOException, SQLException, ClassNotFoundException
    {
        //
        if(command.equals(Command.SEARCH))
        {
            ooStream.writeObject(Command.SEARCH);
            for(Player player_in_list : TcpServer.players)
            {
                if(player_in_list.isConnected() && !player_in_list.isPlaying())
                {
                    if(player_in_list != player)
                    {
                        ooStream.writeObject(new Profile(player_in_list.getName(),
                            player_in_list.getProfilePicture()));
                        System.out.println("vai! " +
                                player_in_list.getName() + " " +
                                player_in_list.getProfilePicture());
                    }
                }
            }
            ooStream.writeObject(Command.SEARCH);
            ooStream.flush();
        }
        else if(command.startsWith(Command.SEARCH))
        {
            String [] params = command.split(" ");
            if(params.length < 2)
            {
                ooStream.writeObject(Command.SEARCH);
                for(Player player_in_list : TcpServer.players)
                {
                    if(player_in_list.isConnected() && !player_in_list.isPlaying())
                    {
                        if(player_in_list != player)
                        {
                            ooStream.writeObject(new Profile(player_in_list.getName(),
                                player_in_list.getProfilePicture()));
                            System.out.println("vai! " +
                                    player_in_list.getName() + " " +
                                    player_in_list.getProfilePicture());
                        }
                    }
                }
                ooStream.writeObject(Command.SEARCH);
                ooStream.flush();
                return;
            }
            ooStream.writeObject(Command.SEARCH);
            for(Player player_in_list : TcpServer.players)
            {
                if(player_in_list.isConnected() && !player_in_list.isPlaying())
                {
                    if(player_in_list != player)
                    {
                        if(player_in_list.getName().compareToIgnoreCase(params[1]) == 0)
                        {
                            ooStream.writeObject(new Profile(player_in_list.getName(),
                                player_in_list.getProfilePicture()));
                            System.out.println("vai! " +
                                    player_in_list.getName() + " " +
                                    player_in_list.getProfilePicture());
                        }
                    }
                }
            }
            ooStream.writeObject(Command.SEARCH);
            ooStream.flush();
        }
        else if(command.startsWith(Command.REMOVE_PPIC))
        {
            String [] params = command.split(" ");
            File file = new File(params[1]);
            if(file.exists())
            {
                file.delete();
            }
        }
        else if(command.startsWith(Command.GET_PPIC))
        {
            String [] params = command.split(" ");
            for(Player player_in_list : TcpServer.players)
            {
                if(player_in_list.getName().equals(params[1]))
                {
                    ooStream.writeObject(player_in_list.getProfilePicture());
                    ooStream.flush();
                    break;
                }
            }
        }
        else if(command.startsWith(Command.UPDATE_PASS))
        {
            String [] params = command.split(" ");
            //
            try
            {
                database.updatePassword(params[1], params[2]);
                ooStream.writeObject(Command.UPDATE_PASS + " " + Response.OK);
            }
            catch(SQLException ex)
            {
                ooStream.writeObject(Command.UPDATE_PASS + " " + Response.ERROR);
            }
            ooStream.flush();
        }
        else if(command.startsWith(Command.AUTO_LOGIN))
        {
            String [] params = command.split(" ");
            if(!database.checkUser(params[1]))
            {
                ooStream.writeObject(Response.NOT_REGISTERED);
            }
            else
            {
                player.setName(params[1]);
                player.setProfilePicture(database.getProfilePhotoName(params[1]));
                player.setConnected(true);
                TcpServer.notifyAllPlayers(Command.JOINED + " " + params[1]);
            }
        }
        else if(command.startsWith(Command.LOGIN))
        {
            String [] params = command.split(" ");
            Integer response = database.checkLogin(params[1], params[2]);
            
            ooStream.writeObject(response);
            if(response.equals(Response.OK))
            {
                TcpServer.notifyAllPlayers(Command.JOINED + " " + params[1] +
                        " " + database.getProfilePhotoName(params[1]));
                player.setName(params[1]);
                player.setProfilePicture(database.getProfilePhotoName(params[1]));
                player.setConnected(true);
                //
                ooStream.writeObject(database.getProfilePhotoName(params[1]));
            }
            //
            ooStream.flush();
        }
        else if(command.startsWith(Command.REGISTER))
        {
            String [] params = command.split(" ");
            
            if(database.checkUser(params[1]))
            {
                ooStream.writeObject(Response.REGISTERED);
            }
            else
            {
                try
                {
                    String profile_pic = new Date().getTime() + "_" + Math.random() * 128 + ".jpg";
                    System.out.println(profile_pic);
                    database.addUser(params[1], params[2], profile_pic);
                    ooStream.writeObject(Response.OK);
                    
                    
                    System.out.println("ppp");
                    Integer size = (Integer)oiStream.readObject();
                    Integer received = 0;
                    BufferedInputStream in = new BufferedInputStream(
                        player.getSocket().getInputStream());

                    OutputStream out = new FileOutputStream(profile_pic);
                    System.out.println("receiving file");

                    byte[] buf = new byte[8192];
                    int len = 0;
                    try
                    {
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                            if(received + len == size)
                                break;
                            else
                                received += len;
                        }
                    }
                    catch(IOException ex)
                    {
                        database.removeUser(params[1]);
                        ooStream.writeObject(Response.ERROR);
                        throw new IOException();
                    }

                    //send file name to client
                    ooStream.writeObject(profile_pic);
                    
                    System.out.println("received");
                    out.close();
                    
                }
                catch(SQLException e)
                {
                    ooStream.writeObject(Response.ERROR);
                    throw new SQLException();
                }
            }
            ooStream.flush();
        }
        else if(command.startsWith(Command.PROFILE_PIC_UP))
        {
            String [] params = command.split(" ");
            //
            String profile_pic = new Date().getTime() + "_" + Math.random() * 128 + ".jpg";
            Integer size = (Integer)oiStream.readObject();
            Integer received = 0;
            BufferedInputStream in = new BufferedInputStream(
                player.getSocket().getInputStream());

            OutputStream out = new FileOutputStream(profile_pic);
            System.out.println("receiving file");

            byte[] buf = new byte[8192];
            int len = 0;
            try
            {
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                    if(received + len == size)
                        break;
                    else
                        received += len;
                }
            }
            catch(IOException ex)
            {
                ooStream.writeObject(Command.PROFILE_PIC_UP + " " + Response.ERROR);
                throw new IOException();
            }

            //send file name to client
            try
            {
                database.updateProfilePhotoName(params[1], profile_pic);
                ooStream.writeObject(Command.PROFILE_PIC_UP + " " + Response.OK);
            }
            catch(SQLException ex)
            {
                ooStream.writeObject(Command.PROFILE_PIC_UP + " " + Response.ERROR);
                File file = new File(profile_pic);
                if(file.exists())
                    file.delete();
                throw new SQLException();
            }
            
            System.out.println("received");
            out.close();
        }
        else if(command.startsWith(Command.PROFILE_PIC_DOWN))
        {
            String [] params = command.split(" ");
            
            InputStream in = new FileInputStream(
                    new File(params[1]));
            ooStream.writeObject((Integer)in.available());
            ooStream.flush();
            
            OutputStream outs = player.getSocket().getOutputStream();

            byte[] buf = new byte[8192];
            int len = 0;
            while ((len = in.read(buf)) != -1) {
                outs.write(buf, 0, len);
                outs.flush();
            }

            in.close();
            //out.close();
        }
        else if(command.startsWith(Command.INVITE))
        {
            String [] params = command.split(" ");
            for(Player p : TcpServer.players)
            {
                if(p.getName().equals(params[1]))
                {
                    ObjectOutputStream iooStream = p.getOoStream();
                    iooStream.writeObject(Command.INVITED + " " + player.getName());
                    iooStream.flush();
                    break;
                }
            }
        }
        else if(command.startsWith(Command.REJECT_INV))
        {
            //
            String [] params = command.split(" ");
            
            for(Player p : TcpServer.players)
            {
                if(p.getName().equals(params[1]))
                {
                    ObjectOutputStream iooStream = p.getOoStream();
                    iooStream.writeObject(Command.REJECT_INV + " " + player.getName());
                    iooStream.flush();
                }
            }
        }
        else if(command.startsWith(Command.NEW_GAME))
        {
            String [] params = command.split(" ");
            
            for(Player p : TcpServer.players)
            {
                if(p.getName().equals(params[1]))
                {
                    ObjectOutputStream iooStream = p.getOoStream();
                    iooStream.writeObject(params[0] + " " + params[2] + " " + params[3]);
                    iooStream.flush();
                }
            }
        }
        else if(command.startsWith(Command.ACCEPT_INV))
        {
            //
            String [] params = command.split(" ");
            
            ObjectOutputStream iooStream = null;
            for(Player p : TcpServer.players)
            {
                if(p.getName().equals(params[1]))
                {
                    iooStream = p.getOoStream();
                    break;
                }
            }
            
            if(iooStream != null)
            {
                //
                iooStream.writeObject(Command.ACCEPT_INV + " " + player.getName());
                iooStream.flush();
            }
            else
            {
                System.out.println("Um erro!");
            }
        }
        else if(command.startsWith(Command.UPDATE_DB))
        {
            //
            String [] params = command.split(" ");
            
            ArrayList<DownloadQuestion> qs =
                    db_questions.getQuestions(Integer.parseInt(params[1]));
            Integer v = db_questions.getLastVersionNumber();
            
            System.out.println("size - " + qs.size());
            
            ooStream.writeObject(qs.size());
            ooStream.flush();
            
            for(DownloadQuestion q : qs)
            {
                ooStream.writeObject(q);
                ooStream.flush();
            }
            
            ooStream.writeObject(v);
            ooStream.flush();
        }
    }
}

