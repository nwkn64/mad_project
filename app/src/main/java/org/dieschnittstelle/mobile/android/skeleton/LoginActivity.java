package org.dieschnittstelle.mobile.android.skeleton;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityLoginBinding;
import org.dieschnittstelle.mobile.android.skeleton.model.FireBaseCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.RoomDataItemCRUDOperationsImpl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    public static final int CALL_MAIN_ACTIVITY = 0;
    public static final int CRUD_FIREBASE = 0;
    public static final int CRUD_ROOM = 1;
    public static int counter = 0;

    ActivityLoginBinding binding;
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";
    private static final String PASSWORD_PATTERN = "[0-9]{6}";
    private Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    private Matcher matcher;
    private static TextInputLayout usernameWrapper;
    private static TextInputLayout passwordWrapper;
    private static DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private static DatabaseReference dbUser = mRootRef.child("user");
    private static ProgressBar progressBar;

    private static MaterialTextView errorMessage;
    private static IDataItemCRUDOperations crudOperations;


    private static Button loginBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dbUser.getDatabase();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);


        this.binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_login, null, false);

        this.progressBar = this.findViewById(R.id.progressBar);
        errorMessage = this.findViewById(R.id.errorwarning);

        loginBtn = findViewById(R.id.loginBtn);

        usernameWrapper = findViewById(R.id.emailWrapper);
        passwordWrapper = findViewById(R.id.passwordWrapper);
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
                errorMessage.setVisibility(View.INVISIBLE);
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
                errorMessage.setVisibility(View.INVISIBLE);

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


        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMetered = cm.isActiveNetworkMetered();

        if (isMetered) {

            crudOperations = new RoomDataItemCRUDOperationsImpl(this);

            Toast.makeText(this, "No connection to firebase, using local database", Toast.LENGTH_LONG).show();

            Intent callLoginView = new Intent(LoginActivity.this, MainActivity.class);

            callLoginView.putExtra("crudOperations", "1");
            startActivityForResult(callLoginView, CALL_MAIN_ACTIVITY);
        } else {
            crudOperations = new FireBaseCRUDOperations();


        }




    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    public boolean validateEmail() {
        matcher = emailPattern.matcher(usernameWrapper.getEditText().getText());
        return matcher.matches();
    }

    public boolean validatePassword() {
        matcher = passwordPattern.matcher(passwordWrapper.getEditText().getText());
        return matcher.matches();
    }

    private void onLogin() {


        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    Thread.sleep(2000);
                    dbUser.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {


                                if (postSnapshot.child("email").getValue().toString().equals(LoginActivity.usernameWrapper.getEditText().getText().toString()) &&

                                        postSnapshot.child("password").getValue().toString().equals(LoginActivity.passwordWrapper.getEditText().getText().toString())) {
                                    Intent callLoginView = new Intent(LoginActivity.this, MainActivity.class);
                                    callLoginView.putExtra("crudOperations", "0");
                                    startActivityForResult(callLoginView, CALL_MAIN_ACTIVITY);

                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);

                                    errorMessage.setVisibility(View.VISIBLE);

                                }


                            }


                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } catch (Exception e) {

                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                progressBar.setVisibility(View.INVISIBLE);

            }
        }.execute();


    }
}
