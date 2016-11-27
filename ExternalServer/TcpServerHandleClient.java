

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bernardovieira
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
    //public static String    ANSWER  = "answer";
}

class Response
{
    public static Integer   OK      = 1;
    public static Integer   ERROR   = 2;
}

public class TcpServerHandleClient implements Runnable {

    private final Player player;
    
    private ObjectOutputStream ooStream;
    private ObjectInputStream oiStream;
    
    public TcpServerHandleClient(Player player)
    {
        this.player = player;
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
    }
    
    private void executeCommand(String command) throws IOException
    {
        //
        if(command.equals(Command.PLAY))
        {
            player.setConnected(true);
            ooStream.writeObject(Command.PLAY);
            ooStream.writeObject(Response.OK);
            ooStream.flush();
        }
        else if(command.equals(Command.SEARCH))
        {
            List<String> names = new ArrayList<>();
            for(Player player : TcpServer.players)
            {
                if(player.isConnected() && !player.isPlaying())
                    names.add(player.getName());
            }
            ooStream.writeObject(Command.SEARCH);
            ooStream.writeObject(names);
            ooStream.flush();
        }
        else if(command.startsWith(Command.SEARCH))
        {
            //search for a name
        }
        else if(command.startsWith(Command.LOGIN))
        {
            String [] params = command.split(" ");
            player.setName(params[1]);
            ooStream.writeObject(Command.LOGIN);
            ooStream.writeObject(Response.OK);
            ooStream.flush();
        }
    }
}

