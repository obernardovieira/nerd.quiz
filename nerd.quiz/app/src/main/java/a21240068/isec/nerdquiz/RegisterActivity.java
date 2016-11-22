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

    Handler mainHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        selectedImageUri = null;
        mainHandler = new Handler();
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
        if(selectedImageUri == null)
        {
            Toast.makeText(this, "A profile image is missing!", Toast.LENGTH_LONG);
            return;
        }

        TextView tv_username = (TextView) findViewById(R.id.et_username);
        TextView tv_password = (TextView) findViewById(R.id.et_password);
        ImageView iv_profile_pic = (ImageView) findViewById(R.id.iv_profile_pic);

        String username = tv_username.getText().toString();
        String password = tv_password.getText().toString();

        BitmapDrawable drawable = (BitmapDrawable) iv_profile_pic.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, 70, 70, true);

        //String profile_pic = saveToInternalStorage(bitmap);

        if(username.length() < 1)
        {
            Toast.makeText(this, "Username is missing!", Toast.LENGTH_LONG);
            return;
        }

        if(password.length() < 1)
        {
            Toast.makeText(this, "Password is missing!", Toast.LENGTH_LONG);
            return;
        }

        //registerOnServer(username, password, profile_pic);
    }

    public void registerOnServer(String username, String password, String profile_pic)
    {
        //
        //Thread t = new Thread(new ServerThread(username, password, profile_pic));
        //t.start();
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
    /*
    private String saveToInternalStorage(Bitmap bitmap)
    {
        ContextWrapper  cw          = new ContextWrapper(getApplicationContext());
        File            directory   = cw.getDir("directoryName", Context.MODE_PRIVATE);
        String          fileName    = "profile.jpg";
        File            myPath      = new File(directory, fileName);

        try
        {
            FileOutputStream fos;
            fos = new FileOutputStream(myPath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    class ServerThread extends Thread {

        private String username;
        private String password;
        private String profile_pic;

        public ServerThread(String username, String password, String profile_pic) {
            this.username = username;
            this.password = password;
            this.profile_pic = profile_pic;
        }

        @Override
        public void run() {
            Connection connection = (Connection) getApplication();
            //
            try
            {
                Log.d("RPS", "Connecting to the server  " + connection.serverIP);
                connection.socketToServer = new Socket(connection.serverIP, connection.serverPort);
                //
                Log.d("ServerThread", "1");
                //wait for messages from server!
                InputStream iStream = connection.socketToServer.getInputStream();
                OutputStream oStream = connection.socketToServer.getOutputStream();
                ObjectInputStream oiStream = new ObjectInputStream(iStream);
                ObjectOutputStream ooStream = new ObjectOutputStream(oStream);
                Log.d("ServerThread", "a");
                ooStream.writeObject("register" + username + " " + password);
                Log.d("ServerThread", "b");
                Integer success = (Integer) oiStream.readObject();
                Log.d("ServerThread", "c");
                if (success.equals(1))
                {
                    //send image file
                    FileInputStream requestedFileInputStream;

                    byte []fileChunck = new byte[1024];
                    int nbytes;
                    requestedFileInputStream = new FileInputStream(profile_pic);
                    Toast.makeText(getBaseContext(), "Uploading ...", Toast.LENGTH_LONG);

                    while((nbytes = requestedFileInputStream.read(fileChunck)) > 0)
                    {

                        oStream.write(fileChunck, 0, nbytes);
                        oStream.flush();

                    }

                    Toast.makeText(getBaseContext(), "Uploaded!", Toast.LENGTH_LONG);
                }

                iStream.close();
                oiStream.close();

                connection.socketToServer.close();
                connection.socketToServer = null;
            }
            catch(UnknownHostException e)
            {
                System.out.println("Destino desconhecido:\n\t"+e);
            }
            catch(NumberFormatException e)
            {
                System.out.println("O porto do servidor deve ser um inteiro positivo:\n\t"+e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    */
}
