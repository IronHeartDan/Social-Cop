package com.danapps.social_cop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogUser extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private EditText input_text, input_pass, input_confirm_pass;
    private Button btn_action;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_user);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(android.R.color.transparent));
        window.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.cop_bg));

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LogUser.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        input_text = findViewById(R.id.input_text);
        input_pass = findViewById(R.id.input_pass);
        input_confirm_pass = findViewById(R.id.input_confirm_pass);
        btn_action = findViewById(R.id.btn_action);
        loading = findViewById(R.id.loading);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change:
                if (v.getTag().toString().equals("0")) {
                    input_confirm_pass.setVisibility(View.VISIBLE);
                    input_pass.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    TextView textView = (TextView) v;
                    textView.setTag("1");
                    textView.setText("Log-In Instead");
                    btn_action.setTag("1");
                    btn_action.setText("SignUp");
                } else {
                    input_confirm_pass.setVisibility(View.GONE);
                    input_pass.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    TextView textView = (TextView) v;
                    textView.setTag("0");
                    textView.setText("Sign-Up Instead");
                    btn_action.setTag("0");
                    btn_action.setText("LogIn");
                }
                break;

            case R.id.btn_action:
                loading.setVisibility(View.VISIBLE);
                if (v.getTag().toString().equals("0")) {
                    String text, pass;
                    text = input_text.getText().toString().trim();
                    pass = input_pass.getText().toString().trim();
                    if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(pass)) {
                        mAuth.signInWithEmailAndPassword(text, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(LogUser.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    loading.setVisibility(View.GONE);
                                    Toast.makeText(LogUser.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        loading.setVisibility(View.GONE);
                        Toast.makeText(this, "Provide Input", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String text, pass, c_pass;
                    text = input_text.getText().toString().trim();
                    pass = input_pass.getText().toString().trim();
                    c_pass = input_confirm_pass.getText().toString().trim();
                    if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(c_pass)) {
                        if (pass.equals(c_pass)) {
                            mAuth.createUserWithEmailAndPassword(text, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(LogUser.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        loading.setVisibility(View.GONE);
                                        Toast.makeText(LogUser.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            loading.setVisibility(View.GONE);
                            Toast.makeText(this, "Passwords Don't Match", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        loading.setVisibility(View.GONE);
                        Toast.makeText(this, "Provide Input", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}