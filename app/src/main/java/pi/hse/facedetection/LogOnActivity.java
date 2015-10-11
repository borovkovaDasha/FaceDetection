package pi.hse.facedetection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

        Intent intent = new Intent(this, PhotoActivity.class);
        startActivity(intent);
    }
}