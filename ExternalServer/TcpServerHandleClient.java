

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
            
            player.setName((String)oiStream.readObject());
            
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
        if(command.equals("play"))
        {
            player.setPlaying(true);
            ooStream.writeObject(command);
            ooStream.writeObject(Response.OK);
            ooStream.flush();
        }
        else if(command.equals("search"))
        {
            //search random
        }
        else if(command.startsWith("search"))
        {
            //search for a name
        }
    }
}

