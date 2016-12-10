package a21240068.isec.nerdquiz.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;

/**
 *
 * @author bernardovieira
 */
public class Profile implements Serializable {
    private String name;
    private String profile_pic;

    public Profile()
    {
        this.name = "";
        this.profile_pic = "";
    }
    
    public Profile(String name, String profile_pic)
    {
        this.name = name;
        this.profile_pic = profile_pic;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setProfilePicture(String profile_pic)
    {
        this.profile_pic = profile_pic;
    }

    public String getName()
    {
        return name;
    }

    public String getProfilePicture()
    {
        return profile_pic;
    }
}
