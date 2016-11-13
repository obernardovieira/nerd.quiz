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
        startActivityForResult(new Intent(this, SearchPlayerActivity.class), INVITE_PLAYER_CODE);
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
        Toast.makeText(this, username, Toast.LENGTH_LONG).show();
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
