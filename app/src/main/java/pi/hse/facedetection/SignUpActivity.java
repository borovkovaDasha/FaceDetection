package pi.hse.facedetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SignUpActivity extends AppCompatActivity {
    EditText name_field;
    EditText email_field;

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

        Intent intent = new Intent(this, PhotoActivity.class);
        intent.putExtra("name", name_field.getText().toString());
        intent.putExtra("email", email_field.getText().toString());
        startActivity(intent);


    }
}
