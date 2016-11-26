package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class RegisterActivity extends Activity {

    private static final int PICK_IMAGE = 1;
    private Uri selectedImageUri;
    Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        selectedImageUri = null;
        connection = (Connection)getApplication();
    }

    public void changeProfilePhoto(View view)
    {
        try
        {
            Intent gintent = new Intent();
            gintent.setType("image/*");
            gintent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(gintent, "Select Picture"), PICK_IMAGE);
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }
    }

    public void clickRegisterButton(View view)
    {

    }

    public void registerOnServer(String username, String password, String profile_pic)
    {
    }

    public void clickRegisteredText(View view)
    {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);

        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK)
                {
                    ImageView iv_profile_pic = (ImageView) findViewById(R.id.iv_profile_pic);
                    selectedImageUri = data.getData();
                    iv_profile_pic.setImageURI(selectedImageUri);
                    Log.d("Uri", data.getData().toString());
                }
                break;
        }
    }

}
