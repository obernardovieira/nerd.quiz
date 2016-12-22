package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class EditProfileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String file_profile_pic = preferences.getString(getString(R.string.profile_pic), "error");

        ImageView iv_profile_pic = (ImageView) findViewById(R.id.iv_profile_pic);
        File imgFile = new File(getApplicationContext().getFilesDir(), file_profile_pic);

        if(imgFile.exists())
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            iv_profile_pic.setImageBitmap(myBitmap);
        }

    }

    public void changeProfilePhoto(View view)
    {
        //
    }
}
