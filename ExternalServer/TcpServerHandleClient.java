

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bernardovieira
 */

import amovserver.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import paservidor.Database;

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
    //public static String    ANSWER    = "answer";
    public static String    REJECT_INV  = "reject";
    public static String    ACCEPT_INV  = "accept";
}

public class TcpServerHandleClient implements Runnable {

    private final Player player;
    private final Database database;
    
    private ObjectOutputStream ooStream;
    private ObjectInputStream oiStream;
    
    public TcpServerHandleClient(Player player, Database database)
    {
        this.player = player;
        this.database = database;
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
        catch (IOException | ClassNotFoundException ex)
        {
            System.out.println("O cliente desligou-se!");
        }
        catch (SQLException ex)
        {
            System.out.println("Erro na base de dados!");
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
            ArrayList<Profile> profiles = new ArrayList<>();
            for(Player player_in_list : TcpServer.players)
            {
                if(player_in_list.isConnected() && !player_in_list.isPlaying())
                {
                    if(player_in_list != player)
                        profiles.add(player_in_list.getProfile());
                }
            }
            ooStream.writeObject(Command.SEARCH);
            ooStream.writeObject(profiles);
            ooStream.flush();
        }
        else if(command.startsWith(Command.SEARCH))
        {
            //search for a name
        }
        else if(command.startsWith(Command.PLAY))
        {
            player.setConnected(true);
        }
        else if(command.startsWith(Command.AUTO_LOGIN))
        {
            String [] params = command.split(" ");
            if(!database.checkUser(params[1]))
            {
                //ooStream.writeObject(Response.ERROR);
            }
            else
            {
                player.setName(params[1]);
                player.setConnected(true);
            }
        }
        else if(command.startsWith(Command.LOGIN))
        {
            String [] params = command.split(" ");
            Integer response = database.checkLogin(params[1], params[2]);
            
            if(response.equals(Response.OK))
            {
                player.setName(params[1]);
                player.setConnected(true);
            }
            ooStream.writeObject(response);
            ooStream.flush();
        }
        else if(command.startsWith(Command.REGISTER))
        {
            String [] params = command.split(" ");
            
            if(database.checkUser(params[1]))
            {
                ooStream.writeObject(Response.ERROR);
            }
            else
            {
                try
                {
                    database.addUser(params[1], params[2]);
                    ooStream.writeObject(Response.OK);
                }
                catch(SQLException e)
                {
                    ooStream.writeObject(Response.ERROR);
                    throw new SQLException();
                }
            }
            ooStream.flush();
        }
        else if(command.startsWith(Command.INVITE))
        {
            String [] params = command.split(" ");
            
            ooStream.writeObject(Response.OK);
            ooStream.flush();
            
            for(Player p : TcpServer.players)
            {
                if(p.getName().equals(params[1]))
                {
                    ObjectOutputStream iooStream = p.getOoStream();
                    iooStream.writeObject(Command.INVITED + " " + player.getName());
                    iooStream.flush();
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
                iooStream.writeObject(Command.ACCEPT_INV + " " + params[1]);
                iooStream.writeObject(oiStream.readObject());//adress
                iooStream.writeObject(oiStream.readObject());//port
            }
            else
            {
                System.out.println("Um erro!");
            }
        }
    }
}

