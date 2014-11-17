package chat.sample.buddy.com.buddychat;

import android.app.AlertDialog;
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
// Login the user
//
public class Login extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText editUsername = (EditText)findViewById(R.id.editUserName);
        final EditText editPassword = (EditText)findViewById(R.id.editPassword);

        final Button btnLogin = (Button)super.findViewById(R.id.btnLogin);
        final Button btnSignup = (Button)super.findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Buddy.loginUser(editUsername.getText().toString(), editPassword.getText().toString(), new BuddyCallback<User>(User.class) {
                    @Override
                    public void completed(BuddyResult<User> result) {
                        if (result.getIsSuccess()) {
                            // if successful, pop over to the main screen
                            //
                            BuddyChatApplication.instance.setCurrentUser(result.getResult());
                            Intent i = new Intent(getBaseContext(), MainScreen.class);
                            startActivity(i);
                            finish();
                        }
                        else {
                            // show error
                            String error = result.getError();

                            // Username or password false, display and an error
                            final AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(Login.this);

                            dlgAlert.setTitle("Error Logging In");
                            dlgAlert.setPositiveButton("OK", null);
                            dlgAlert.setCancelable(true);

                            dlgAlert.setPositiveButton("Ok",null);
                            if ("AuthBadUsernameOrPassword".equalsIgnoreCase(error)) {
                                dlgAlert.setMessage("Bad username or password, please try again.");
                            }
                            else {
                                dlgAlert.setMessage(String.format("Error attempting login: %s.", result.getError()));
                            }
                            dlgAlert.create().show();

                        }
                    }
                });
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Login.this.getBaseContext(),Signup.class);
                Login.this.startActivity(i);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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
