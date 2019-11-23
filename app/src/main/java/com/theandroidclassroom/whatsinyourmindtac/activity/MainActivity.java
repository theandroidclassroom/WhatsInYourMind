package com.theandroidclassroom.whatsinyourmindtac.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.theandroidclassroom.whatsinyourmindtac.R;
import com.theandroidclassroom.whatsinyourmindtac.storage.MySharedPreferences;
import com.theandroidclassroom.whatsinyourmindtac.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.name)
    EditText mNameEt;
    @BindView(R.id.email)
    EditText mEmailEt;
    @BindView(R.id.password)
    EditText mPasswordEt;
    @BindView(R.id.btn)
    Button mSignUpBtn;
    @BindView(R.id.login)Button mLoginBtn;
    @BindView(R.id.relative)RelativeLayout mParent;
    private String name, email, password;
    private FirebaseAuth mAuth;

    private CollectionReference mRef;
    private AlertDialog dialog;
    private MySharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = MySharedPreferences.getInstance(this);
        if (sp.getLogin().equals("1")){
            Intent intent = new Intent(MainActivity.this,SecondStepRegisterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        if (sp.getLogin().equals("2")){
            Intent intent = new Intent(MainActivity.this,HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        dialog = Utils.getAlertDialog(this,"Signing Up..");
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseFirestore.getInstance().collection("users");
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDataAndLogin();
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });


    }

    private void checkDataAndLogin() {
        name = mNameEt.getText().toString();
        email = mEmailEt.getText().toString().trim();
        password = mPasswordEt.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()|| password.length() < 6) {
            Utils.showSnackbar(mParent,"Invalid Credentials.");
        } else {

            dialog.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()){
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d("MUR", "onComplete: ");
                        saveUserCredentials();
                    }
                }
            });
        }
    }
    private void saveUserCredentials(){
        Map<String,String> map = new HashMap<>();
        map.put("name",name);
        map.put("email",email);
        map.put("password",password);
        map.put("id",mAuth.getUid());

        mRef.document(mAuth.getUid()).set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                if (!task.isSuccessful()){
                    Utils.showSnackbar(mParent,task.getException().getLocalizedMessage());
                }
                else {
                    sp.setLogin("1");
                    sp.setUserID(mAuth.getUid());
                    Toast.makeText(getApplicationContext(),"SignUp Successful",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this,SecondStepRegisterActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });

    }


}
