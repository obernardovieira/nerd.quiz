package a21240068.isec.nerdquiz.Objects;

import android.graphics.drawable.Drawable;

/**
 * Created by bernardovieira on 06-11-2016.
 */

public class Profile {
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