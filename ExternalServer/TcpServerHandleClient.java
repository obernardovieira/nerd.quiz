

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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
public class TcpServerHandleClient implements Runnable {

    private final Socket socket;
    
    public TcpServerHandleClient(Socket socket)
    {
        this.socket = socket;
    }
    
    @Override
    public void run()
    {
        
        try
        {
            String command;
            InputStream iStream = socket.getInputStream();
            OutputStream oStream = socket.getOutputStream();
            ObjectInputStream oiStream = new ObjectInputStream(iStream);
            ObjectOutputStream ooStream = new ObjectOutputStream(oStream);
            
            do
            {
                command = (String)oiStream.readObject();
                System.out.println(command);
                ooStream.writeObject(command);
                //command
                
            }while(!command.equals("finish"));
            
            iStream.close();
            oiStream.close();
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(TcpServerHandleClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}

