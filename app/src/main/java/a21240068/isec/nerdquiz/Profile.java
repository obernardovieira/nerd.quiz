package a21240068.isec.nerdquiz;

import android.graphics.drawable.Drawable;

/**
 * Created by bernardovieira on 06-11-2016.
 */

public class Profile {
    private String name;
    private String profile_pic;

    public Profile(String name, String profile_pic)
    {
        this.name = name;
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
