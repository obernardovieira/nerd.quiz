

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpToServer
{
    private Socket socket;
    public static ObjectOutputStream ooStream;
    
    public static String last_command;
    public static boolean connected;
    public static boolean playing;

    public TcpToServer(String hostname, int port)
    {
        connected = false;
        playing = false;
        
        try
        {
            this.socket = new Socket(hostname, port);
            
            Scanner sc;
            String cmd;
            
            OutputStream oStream = this.socket.getOutputStream();
            ooStream = new ObjectOutputStream(oStream);
            
            TcpToServerReceiver tcpconn = new TcpToServerReceiver(socket);
            Thread thread_tcpconn = new Thread(tcpconn);
            thread_tcpconn.start();
            
            sc = new Scanner(System.in);
            System.out.print("Login:");
            cmd = sc.nextLine();//"login ber obv";//sc.nextLine();
            last_command = cmd;
            ooStream.writeObject(cmd);
            ooStream.flush();
            
            do
            {
                System.out.print("Command:");
                cmd = sc.nextLine();
                last_command = cmd;
                ooStream.writeObject(cmd);
                ooStream.flush();
                
            } while(!cmd.equals("finish"));
            oStream.close();
            ooStream.close();
            
            thread_tcpconn.join();
        }
        catch (IOException | InterruptedException ex)
        {
            Logger.getLogger(TcpToServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}