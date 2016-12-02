


import java.io.ObjectOutputStream;
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
    private Profile profile;
    private boolean connected;
    private boolean playing;
    private ObjectOutputStream ooStream;

    public Player(Socket socket)
    {
        this.socket = socket;
        this.profile = new Profile("","");
        this.connected = false;
        this.playing = false;
        this.ooStream = null;
    }

    public ObjectOutputStream getOoStream() {
        return ooStream;
    }

    public void setOoStream(ObjectOutputStream ooStream) {
        this.ooStream = ooStream;
    }

    public void setName(String name) {
        this.profile.setName(name);
    }
    
    public void setProfilePicture(String profile_pic) {
        this.profile.setProfilePicture(profile_pic);
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
        return profile.getName();
    }
    
    public String getProfilePicture() {
        return profile.getProfilePicture();
    }
    
    public Profile getProfile() {
        return profile;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isPlaying() {
        return playing;
    }
    
    
}
