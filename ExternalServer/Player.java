
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author bernardovieira
 */
public class Player {
    
    
    private final Socket socket;
    private String name;
    private boolean connected;
    private boolean playing;

    public Player(Socket socket)
    {
        this.socket = socket;
        this.name = "";
        this.connected = false;
        this.playing = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public String getName() {
        return name;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isPlaying() {
        return playing;
    }
    
    
}
