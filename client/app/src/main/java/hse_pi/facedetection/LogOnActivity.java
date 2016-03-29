package hse_pi.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LogOnActivity extends Activity{

    String email;
    EditText textField;
    TextView name;
    String data ;
    InputStream inputStream;
    private final int IDD_DIALOG_NETWORK_ERROR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        //
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
        setContentView(R.layout.activity_logon);
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        textField = (EditText)findViewById(R.id.notepad);
        name = (TextView) findViewById(R.id.lna_email);
        name.setText(email);
        sendToServer(email, "get", true);
    }

    public void onSubmitPressed(View view){
        // saving file as username_Filename;
        data = textField.getText().toString();
        System.out.println(data);
        sendToServer(email, data, false);
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
    }

    public void onLogoffPressed(View view){
        Intent intent = new Intent(this, StartActivity.class);
        startActivity(intent);
    }

    public void sendToServer(final String email, final String action, final boolean getting){

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://dashulya.myftp.org:1234/sendtext");
                    List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                    params.add(new BasicNameValuePair("email", email));
                    params.add(new BasicNameValuePair("action",action));
                    httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);
                    if (getting){
                        textField.setText(the_string_response);
                    }
                    else {
                        if (!the_string_response.equals("true")){
                            showDialog(IDD_DIALOG_NETWORK_ERROR);
                        }
                    }
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
                    LogOnActivity.this.runOnUiThread(new Runnable() {
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