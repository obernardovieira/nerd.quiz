package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AuthenticationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
    }


    public void clickLoginButton(View view)
    {
        TextView tv_username = (TextView) findViewById(R.id.et_username);
        TextView tv_password = (TextView) findViewById(R.id.et_password);

        String username = tv_username.getText().toString();
        String password = tv_password.getText().toString();

        //

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.user_name), username);
        editor.apply();

        Toast.makeText(this, username, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);

        finish();
    }

    public void clickNotRegisteredText(View view)
    {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

        finish();
    }

}
