package chat.sample.buddy.com.buddychat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.User;

import chat.sample.buddy.com.buddychat.R;

//
// Sign up a new user
//
public class Signup extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        final EditText editUsername = (EditText)findViewById(R.id.editUserName);
        final EditText editPassword = (EditText)findViewById(R.id.editPassword);
        final EditText editName = (EditText)findViewById(R.id.editName);

        final Button btnSignup = (Button)super.findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String name = editName.getText().toString();
                String lastName = null;
                int firstSpace = name.indexOf(' ');

                if (firstSpace != -1) {
                    lastName = name.substring(firstSpace + 1, name.length());
                    name = name.substring(0, firstSpace);
                }

                // collect the info then create the account.  If successful, head over to the main screen
                //
                Buddy.createUser(
                        editUsername.getText().toString(),
                        editPassword.getText().toString(),
                        name,
                        lastName,
                        null,
                        null,
                        null,
                        null,
                        new BuddyCallback<User>(User.class) {
                            @Override
                            public void completed(BuddyResult<User> result) {
                                if (result.getIsSuccess()) {
                                    BuddyChatApplication.instance.setCurrentUser(result.getResult());

                                    Intent i = new Intent(getBaseContext(), MainScreen.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    // Username or password false, display and an error
                                    final AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(Signup.this);

                                    dlgAlert.setMessage(String.format("%s", result.getError()));
                                    dlgAlert.setTitle("Error Creating User");
                                    dlgAlert.setPositiveButton("OK", null);
                                    dlgAlert.setCancelable(true);
                                    dlgAlert.create().show();

                                    dlgAlert.setPositiveButton("Ok",null);
                                }
                            }
                });
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
