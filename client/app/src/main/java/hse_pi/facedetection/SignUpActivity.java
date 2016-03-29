package hse_pi.facedetection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.NameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private EditText name_field;
    private EditText email_field;
    private TextView step1Txt;
    private TextView step2Txt;
    private static boolean serverResponse = false;
    private int ret;
    private final int IDD_DIALOG_USER_EXISTS = 0;
    private final int IDD_DIALOG_NETWORK_ERROR = 1;

    InputStream inputStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            boolean isImmersiveModeEnabled =
                    ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        }
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (Build.VERSION.SDK_INT >= 18) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
        }
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        setContentView(R.layout.activity_signup);
        name_field = (EditText) findViewById(R.id.sua_nameEdt);
        email_field = (EditText) findViewById(R.id.sua_emailEdt);
        step1Txt = (TextView) findViewById(R.id.sua_step1Txt1);
        step2Txt = (TextView) findViewById(R.id.sua_step2Txt1);
    }

    public void onNextPressed(View view) {

        // checking email and name in database
        // if ok -> step 3
        String name = name_field.getText().toString();
        String email = email_field.getText().toString();
        step1Txt.setText("");
        step2Txt.setText("");
        // check username and email for wrong symbols
        Pattern pattern = Pattern.compile("^[a-zA-Z ]+$");
        Matcher matcher = pattern.matcher(name);
        if (!matcher.matches())
        {
            step1Txt.setText(R.string.invalidName);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            step2Txt.setText(R.string.invalidEmail);
            return;
        }
        sendToServer(name, email);
        if (ret == 0 && serverResponse) {
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra("name", name_field.getText().toString());
            intent.putExtra("email", email_field.getText().toString());
            startActivity(intent);
        }
        if (ret == 0 &&  !serverResponse){
            //user with this email already exists
            showDialog(IDD_DIALOG_USER_EXISTS);
        }
    }

    public void sendToServer(final String name, final String email){

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://dashulya.myftp.org:1234/register");
                    List <NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("username", name));
                    params.add(new BasicNameValuePair("email", email));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);
                    serverResponse = false;
                    ret = 0;
                    if (the_string_response.equals("true")){
                        serverResponse = true;
                    }
                    else
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                } catch (Exception e) {
                    ret = 1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    SignUpActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            showDialog(IDD_DIALOG_NETWORK_ERROR);
                        }
                    });

                }
            }
        });
        t.start();
        try{
            t.join();
        }
        catch(InterruptedException e){
            //ignore...
        }
    }

    public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException {
        String res = "";
        StringBuffer buffer = new StringBuffer();
        inputStream = response.getEntity().getContent();
        int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..
        if (contentLength < 0) {
        } else {
            byte[] data = new byte[512];
            int len = 0;
            try {
                while (-1 != (len = inputStream.read(data))) {
                    buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer…..
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close(); // closing the stream…..
            } catch (IOException e) {
                e.printStackTrace();
            }
            res = buffer.toString();     // converting stringbuffer to string…..
        }
        return res;
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case IDD_DIALOG_USER_EXISTS:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("User with entered email already exist! Enter another email or login")
                        .setTitle("Registration failed!")
                        .setCancelable(false)
                        .setPositiveButton("Try again",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        name_field.setText("");
                                        email_field.setText("");
                                    }
                                })
                        .setNegativeButton("login",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(SignUpActivity.this, CameraActivity.class);
                                        startActivity(intent);
                                    }
                                });
                return builder.create();
            case IDD_DIALOG_NETWORK_ERROR:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setMessage("Something wrong with internet connection! Would you like to try again?")
                        .setTitle("Sign up")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();

                                    }
                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        finish();
                                        System.exit(0);
                                    }
                                });
                return builder2.create();
            default:
                return null;
        }
    }
}