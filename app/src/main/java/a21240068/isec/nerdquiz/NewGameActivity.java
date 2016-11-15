package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class NewGameActivity extends Activity {

    private final int INVITE_PLAYER_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
    }

    public void clickSearchPlayerButton(View view)
    {
        //startActivityForResult(new Intent(this, SearchPlayerActivity.class), INVITE_PLAYER_CODE);
        invitePlayer("abc");
    }

    public void clickPlayGameButton(View view)
    {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

        finish();
    }

    private void invitePlayer(String username)
    {
        // send request for game
        // wait for answer

        ProfilesData pdata = new ProfilesData(this);
        pdata.add("Quim");
        pdata.add("Zé");
        pdata.add("Bicho");
        pdata.add("Manel");

        GamesData gdata = new GamesData(this);
        gdata.add("Quim", 100, 150);
        gdata.add("Zé", 110, 130);
        gdata.add("Bicho", 190, 160);
        gdata.add("Manel", 180, 140);
        gdata.add("Manel", 150, 155);

        Toast.makeText(this, "added!", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == INVITE_PLAYER_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                // A contact was picked.  Here we will just display it
                // to the user.
                invitePlayer(data.getStringExtra("name"));
            }
        }
    }


}
