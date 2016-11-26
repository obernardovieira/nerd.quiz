package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class MainActivity extends Activity {

    Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection = (Connection)getApplication();
        connection.serverIP = "192.168.1.10";
        connection.serverPort = 5007;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultValue = getResources().getString(R.string.no_user_name_default);
        String username = preferences.getString(getString(R.string.user_name), defaultValue);

        if(!username.equals(defaultValue))
        {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        }

        finish();
    }
}
