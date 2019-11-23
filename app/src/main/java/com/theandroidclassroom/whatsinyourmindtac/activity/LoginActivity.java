package com.theandroidclassroom.whatsinyourmindtac.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.theandroidclassroom.whatsinyourmindtac.R;
import com.theandroidclassroom.whatsinyourmindtac.storage.MySharedPreferences;
import com.theandroidclassroom.whatsinyourmindtac.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.email)EditText mEmailEt;
    @BindView(R.id.password)EditText mPasswordEt;
    @BindView(R.id.login)Button mLoginBtn;
    @BindView(R.id.register)Button mRegisterBtn;
    @BindView(R.id.relative)RelativeLayout mParent;

    private FirebaseAuth mAuth;
    private AlertDialog dialog;
    private MySharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        sp = MySharedPreferences.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        dialog = Utils.getAlertDialog(this,"Signing In..");

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDataAndLogin();
            }
        });
    }

    private void checkDataAndLogin(){
        String email = mEmailEt.getText().toString().trim();
        String password = mPasswordEt.getText().toString().trim();

        if (email.isEmpty()||password.isEmpty()){
            Utils.showSnackbar(mParent,"Invalid Credentials");
        }
        else {
            checkNetworkAndLogin(email,password);
        }
    }

    private void checkNetworkAndLogin(String email, String password) {
        if (Utils.isNetworkAvailable()){
            doLogin(email,password);
        }
        else {
            Utils.showSnackbar(mParent,"No Internet Connection Available");
        }
    }

    private void doLogin(String email, String password) {
        dialog.show();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                dialog.dismiss();
                if (task.isSuccessful()){
                    sp.setLogin("2");
                    sp.setUserID(mAuth.getUid());
                    Toast.makeText(getApplicationContext(),"Login Success",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else {
                    Utils.showSnackbar(mParent,task.getException().getLocalizedMessage());
                }
            }
        });

    }
}

