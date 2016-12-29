package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class EditProfileActivity extends Activity
{
    private final int TAKE_NEW_PHOTO = 0;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String file_profile_pic = preferences.getString(getString(R.string.profile_pic), "");

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
        startActivityForResult(new Intent(this, TakePhotoActivity.class), TAKE_NEW_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case TAKE_NEW_PHOTO:
                if (resultCode == Activity.RESULT_OK)
                {
                    Log.d("sfdgthfy","wadesrgfh");
                    selectedImageUri = Uri.fromFile(new File(getApplicationContext().getFilesDir(),
                            getResources().getString(R.string.default_profile_pic_name)));
                    ((ImageView) findViewById(R.id.iv_profile_pic)).setImageURI(selectedImageUri);
                }
                break;
        }
    }
}
