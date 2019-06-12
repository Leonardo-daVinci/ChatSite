package apps.nocturnal.com.chatsite;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Button mSavebtn;
    private TextInputLayout mStatus;

    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentuser;

    private ProgressDialog mStatusProg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.status_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value = getIntent().getStringExtra("Status value");

        mCurrentuser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentuser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mStatus = (TextInputLayout) findViewById(R.id.status_changetext);
        mSavebtn = (Button) findViewById(R.id.status_changebtn);

        mStatus.getEditText().setText(status_value);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mStatusProg = new ProgressDialog(StatusActivity.this);
                mStatusProg.setTitle("Saving Changes");
                mStatusProg.setMessage("Saving our changes..");
                mStatusProg.setCanceledOnTouchOutside(false);
                mStatusProg.show();

                String status = mStatus.getEditText().getText().toString();

                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mStatusProg.dismiss();

                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Cannot make changes right now. Try again later.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
}
