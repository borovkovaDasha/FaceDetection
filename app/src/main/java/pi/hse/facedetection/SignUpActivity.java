package pi.hse.facedetection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.NameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {
    EditText name_field;
    EditText email_field;
    static boolean serverResponse = false;
    private final int IDD_DIALOG_USER_EXISTS = 0;
    private final int IDD_DIALOG_NETWORK_ERROR = 1;

    InputStream inputStream;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        name_field = (EditText) findViewById(R.id.name_edit);
        email_field = (EditText) findViewById(R.id.email_edit);

    }

    public void onNextPressed(View view) {

        // checking email and name in database
        // if ok -> step 2
        String name = name_field.getText().toString();
        String email = email_field.getText().toString();
        // check username and email for wrong symbols
        sendToServer(name, email);
        if (serverResponse) {
         Intent intent = new Intent(this, PhotoActivity.class);
         intent.putExtra("name", name_field.getText().toString());
         intent.putExtra("email", email_field.getText().toString());
         startActivity(intent);
        }
        else {
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
                    HttpPost httppost = new HttpPost("http://192.168.1.2:1234/register");
                    List <NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("username", name));
                    params.add(new BasicNameValuePair("email", email));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);
                    System.out.println("response here!!!");
                    serverResponse = false;
                    System.out.println(the_string_response);
                    if (the_string_response.equals("true")){
                        serverResponse = true;
                    }
                    System.out.println(the_string_response.equals("true"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    System.out.println("error in http connection");
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
                                        //name_field.setText("");
                                        email_field.setText("");
                                    }
                                })
                        .setNegativeButton("login",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(SignUpActivity.this, PhotoActivity.class);
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
