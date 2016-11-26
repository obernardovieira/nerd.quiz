
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.net.Socket;
import java.util.ArrayList;
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
        Integer response;
        //
        if(command.equals("play"))
        {
            response = (Integer)oiStream.readObject();
            if(response.equals(Response.OK))
            {
                System.out.println("You are playing now!");
            }
        }
    }
}