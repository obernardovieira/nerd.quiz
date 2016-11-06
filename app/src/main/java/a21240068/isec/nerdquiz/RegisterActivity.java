package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void changeProfilePhoto(View view)
    {
        //
    }

    public void clickRegisterButton(View view)
    {
        TextView tv_username = (TextView) findViewById(R.id.et_username);
        TextView tv_password = (TextView) findViewById(R.id.et_password);

        String username = tv_username.getText().toString();
        String password = tv_password.getText().toString();

        //


    }

    public void clickRegisteredText(View view)
    {
        //
    }
}
