
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.net.Socket;
import java.util.ArrayList;
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
    public static String    LOGIN   = "login";
    public static String    PLAY    = "play";
    public static String    SEARCH  = "search";
    public static String    INVITE  = "invite";
    public static String    ANSWER  = "answer";
}

class Response
{
    public static Integer   OK      = 1;
    public static Integer   ERROR   = 2;
}

class TcpToServerReceiver implements Runnable
{
    private final Socket socket;
    
    public TcpToServerReceiver(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        //
        try
        {
            String cmd;
            InputStream iStream = this.socket.getInputStream();
            ObjectInputStream oiStream = new ObjectInputStream(iStream);
            
            do
            {
                cmd = (String)oiStream.readObject();
                runCommand(cmd, oiStream);
                
            }while(!cmd.equals("finish"));
            oiStream.close();
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(TcpToServerReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void runCommand(String command, ObjectInputStream oiStream)
            throws IOException, ClassNotFoundException
    {
        //
        if(command.equals(Command.PLAY))
        {
            Integer response = (Integer)oiStream.readObject();
            if(response.equals(Response.OK))
            {
                System.out.println("You are playing now!");
            }
            else
            {
                System.out.println("An error occurred!");
            }
        }
        else if(command.equals(Command.SEARCH))
        {
            List<String> response = (List<String>)oiStream.readObject();
            if(response.size() > 0)
            {
                for(String name : response)
                {
                    System.out.println(name);
                }
            }
            else
            {
                System.out.println("There is no results!");
            }
        }
        else if(command.equals(Command.INVITE))
        {
            //
        }
        else if(command.equals(Command.LOGIN))
        {
            Integer response = (Integer)oiStream.readObject();
            if(response.equals(Response.OK))
            {
                System.out.println("You are connected now!");
            }
            else
            {
                System.out.println("An error occurred!");
            }
        }
    }
}