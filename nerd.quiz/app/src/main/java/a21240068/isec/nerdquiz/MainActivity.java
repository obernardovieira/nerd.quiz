package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
