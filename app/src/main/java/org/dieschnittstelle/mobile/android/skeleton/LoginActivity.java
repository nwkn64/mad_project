package org.dieschnittstelle.mobile.android.skeleton;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityLoginBinding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    public static final int CALL_MAIN_ACTIVITY = 0;


    ActivityLoginBinding binding;
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private static final String PASSWORD_PATTERN = "[0-9]{6}";
    private Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    private Matcher matcher;

    private static TextInputLayout usernameWrapper;
    private static TextInputLayout passwordWrapper;

    private ProgressBar progressBar;


    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference dbUser = mRootRef.child("user");


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        this.binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_login, null, false);

        this.progressBar = this.findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.INVISIBLE);


        Button loginBtn = findViewById(R.id.loginBtn);
        usernameWrapper = (TextInputLayout) findViewById(R.id.emailWrapper);
        passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);


        loginBtn.setOnClickListener((view) -> {
            this.onLogin();
        });

        usernameWrapper.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameWrapper.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }


        });

        passwordWrapper.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordWrapper.setError(null);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }


        });

        usernameWrapper.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!LoginActivity.this.validateEmail()) {
                        usernameWrapper.setError("Please enter a valid email address!");

                    } else {
                        usernameWrapper.setError(null);

                    }
                }
            }
        });

        passwordWrapper.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!LoginActivity.this.validatePassword()) {
                        passwordWrapper.setError("Please enter a valid password!");

                    } else {
                        passwordWrapper.setError(null);

                    }
                }

            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    public boolean validateEmail() {
        matcher = emailPattern.matcher(this.usernameWrapper.getEditText().getText());
        return matcher.matches();
    }

    public boolean validatePassword() {
        matcher = passwordPattern.matcher(this.passwordWrapper.getEditText().getText());
        return matcher.matches();
    }

    private void onLogin() {
        this.progressBar.setVisibility(View.VISIBLE);
           Thread newThread = new Thread(() -> {
            this.dbUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        System.out.println(LoginActivity.usernameWrapper.getEditText().getText().toString());

                        if (postSnapshot.child("email").getValue().toString().equals(LoginActivity.usernameWrapper.getEditText().getText().toString()) &&
                                postSnapshot.child("password").getValue().toString().equals(LoginActivity.passwordWrapper.getEditText().getText().toString())) {
                            Intent callDetailViewIntentForReturnValue = new Intent(LoginActivity.this, MainActivity.class);
                            startActivityForResult(callDetailViewIntentForReturnValue, CALL_MAIN_ACTIVITY);

                        }


                    }


                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        });
        try {
            Thread.sleep(2000);

        } catch (Exception e) {

        }

        newThread.start();




    }
}
