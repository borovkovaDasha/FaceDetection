package pi.hse.facedetection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class LogOnActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_on);


    }

    public void onBackPressed() {

    }

    public void onSubmitPressed(View view){

// saving file as username_Filename;
    }

    public void onLogoffPressed(View view){

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}