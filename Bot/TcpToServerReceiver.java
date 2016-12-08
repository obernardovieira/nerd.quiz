
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
    public static String    PLAY        = "play";
    public static String    SEARCH      = "search";
    public static String    INVITE      = "invite";
    public static String    INVITED     = "beinvited";
    public static String    ANSWER      = "answer";
    public static String    REJECT_INV  = "reject";
    public static String    ACCEPT_INV  = "accept";
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
            Object obj;
            InputStream iStream = this.socket.getInputStream();
            ObjectInputStream oiStream = new ObjectInputStream(iStream);
            
            do
            {
                obj = (Object)oiStream.readObject();
                runCommand(obj, oiStream);
                
                if(obj instanceof String)
                {
                    if(((String)obj).equals("finish"))
                        break;
                }
            }while(true);
            oiStream.close();
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(TcpToServerReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void runCommand(Object obj, ObjectInputStream oiStream)
            throws IOException, ClassNotFoundException
    {
        if(TcpToServer.connected == false)
        {
            if(TcpToServer.last_command.startsWith(Command.LOGIN))
            {
                Integer response = (Integer)obj;
                if(response.equals(Response.OK))
                {
                    System.out.println("You are connected now!");
                    TcpToServer.connected = true;
                }
                else
                {
                    System.out.println("An error occurred!");
                }
            }
            else if(TcpToServer.last_command.startsWith(Command.REGISTER))
            {
                Integer response = (Integer)obj;
                if(response.equals(Response.OK))
                {
                    System.out.println("You are registered now!");
                }
                else
                {
                    System.out.println("An error occurred!");
                }
            }
        }
        else
        {
            if(TcpToServer.playing == false)
            {
                //
                if(TcpToServer.last_command.startsWith(Command.INVITE))
                {
                    Integer response = (Integer)obj;
                    if(response.equals(Response.OK))
                    {
                        System.out.println("Invited!");
                    }
                    else
                    {
                        System.out.println("An error occurred!");
                    }
                }
                else if(TcpToServer.last_command.equals(Command.SEARCH))
                {
                    ArrayList<Profile> response = (ArrayList<Profile>)oiStream.readObject();
                    if(response.size() > 0)
                    {
                        for(Profile profile : response)
                        {
                            System.out.println(profile.getName());
                        }
                    }
                    else
                    {
                        System.out.println("There is no results!");
                    }
                }
            }
            else
            {
                //
            }
        }
        //
        /*if(command.equals(Command.PLAY))
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
            ArrayList<Profile> response = (ArrayList<Profile>)oiStream.readObject();
            if(response.size() > 0)
            {
                for(Profile profile : response)
                {
                    System.out.println(profile.getName());
                }
            }
            else
            {
                System.out.println("There is no results!");
            }
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
        else if(command.equals(Command.REGISTER))
        {
            Integer response = (Integer)oiStream.readObject();
            if(response.equals(Response.OK))
            {
                System.out.println("You are registered now!");
            }
            else
            {
                System.out.println("An error occurred!");
            }
        }
        else if(command.equals(Command.INVITE))
        {
            Integer response = (Integer)oiStream.readObject();
            if(response.equals(Response.OK))
            {
                System.out.println("Invited!");
            }
            else
            {
                System.out.println("An error occurred!");
            }
        }
        else if(command.startsWith(Command.INVITED))
        {
            String [] params = command.split(" ");
            System.out.println("You have been invited by " + params[1]);
            
            System.out.print("Accept? (y/n)");
            Scanner question = new Scanner(System.in);
            String ans = "y";//question.nextLine();
            
            if(ans.equals("y") || ans.equals("Y"))
            {
                //abre socket servidor
                //espera adversario
                //gera perguntas
                //começa o jogo
                InputStream in = null;
                OutputStream out = null;
        
                ServerSocket game_socket = new ServerSocket(5009);
                game_socket.setSoTimeout(5000);
                
                TcpToServer.ooStream.writeObject(Command.ACCEPT_INV + " " + params[1]);
                TcpToServer.ooStream.writeObject(game_socket.getInetAddress());
                TcpToServer.ooStream.writeObject(5009);
                
                Socket socket = game_socket.accept();
                in = socket.getInputStream();
                out = socket.getOutputStream();
                
                ObjectOutputStream oStreamG = new ObjectOutputStream(out);
                ObjectInputStream iStreamG = new ObjectInputStream(in);
                
                //gerar perguntas
                ArrayList<String> answers = new ArrayList<>();
                answers.add("200");
                answers.add("404");
                answers.add("502");
                
                List<GameQuestion> questions = new ArrayList<>();
                GameQuestion q1 = new GameQuestion();
                q1.setQuestion("What is the HTTP retrieved code when the request was successful?");
                q1.setRightAnswer("200");
                q1.addAnswers(answers);
                
                GameQuestion q2 = new GameQuestion();
                q1.setQuestion("What is the HTTP code for not found?");
                q1.setRightAnswer("404");
                q1.addAnswers(answers);
                
                GameQuestion q3 = new GameQuestion();
                q1.setQuestion("What is the HTTP code for bad gateway?");
                q1.setRightAnswer("502");
                q1.addAnswers(answers);
                
                questions.add(q1);
                questions.add(q2);
                questions.add(q3);
                //
                
                oStreamG.writeObject(questions);
                
                //
                iStreamG.readObject();
                
                //start game
                
                System.out.println("game starts");
            }
            else
            {
                TcpToServer.ooStream.writeObject(Command.REJECT_INV + " " + params[1]);
                TcpToServer.ooStream.flush();
            }
        }
        else if(command.startsWith(Command.REJECT_INV))
        {
            //
            String [] params = command.split(" ");
            System.out.println(params[1] + " rejected you invitation!");
        }
        else if(command.startsWith(Command.ACCEPT_INV))
        {
            //
            InputStream in = null;
            OutputStream out = null;

            InetAddress address = (InetAddress)oiStream.readObject();
            Socket socket = new Socket(address, (Integer)oiStream.readObject());

            in = socket.getInputStream();
            out = socket.getOutputStream();

            ObjectOutputStream oStreamG = new ObjectOutputStream(out);
            ObjectInputStream iStreamG = new ObjectInputStream(in);

            List<GameQuestion> questions = (List<GameQuestion>)iStreamG.readObject();

            oStreamG.writeObject(1);

            System.out.println("game starts");
        }*/
    }
}