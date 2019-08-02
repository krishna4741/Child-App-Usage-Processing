package com.optiquall.childappusage.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optiquall.childappusage.R;
import com.optiquall.childappusage.app.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import static com.optiquall.childappusage.app.MyApplication.BASE_URL;
import static com.optiquall.childappusage.util.AppUtil.hideKeyboard;
import static com.optiquall.childappusage.util.Utils.isValid;

public class ChildRegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    EditText name, email, password;
    Button mRegisterbtn;
    TextView mLoginPageBack;
    FirebaseAuth mAuth;
    DatabaseReference mdatabase;
    String Name, Email, Password;
    ProgressDialog mDialog;
    private String saveFcmUrl = BASE_URL + "/saveChild.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Child Registration");
        }


        name = findViewById(R.id.editName);
        email = findViewById(R.id.editEmail);
        password = findViewById(R.id.editPassword);
        mRegisterbtn = findViewById(R.id.buttonRegister);
        mLoginPageBack = findViewById(R.id.buttonLogin);
        // for authentication using FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();
        mRegisterbtn.setOnClickListener(this);
        mLoginPageBack.setOnClickListener(this);
        mDialog = new ProgressDialog(this);
        mdatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    @Override
    public void onClick(View v) {
        hideKeyboard(ChildRegisterActivity.this);
        if (v == mRegisterbtn) {
            UserRegister();
        } else if (v == mLoginPageBack) {

            Bundle bundle = new Bundle();
            Intent intent = new Intent(ChildRegisterActivity.this, LoginTabActivity.class);
            bundle.putString("TabNumber", "0");
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }

    private void UserRegister() {
        Name = name.getText().toString().trim();
        Email = email.getText().toString().trim();
        Password = password.getText().toString().trim();

        if (TextUtils.isEmpty(Name)) {
            Toast.makeText(ChildRegisterActivity.this, "Enter name.", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(Email)) {
            Toast.makeText(ChildRegisterActivity.this, "Enter email.", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(Password)) {
            Toast.makeText(ChildRegisterActivity.this, "Enter password.", Toast.LENGTH_SHORT).show();
            return;
        } else if (Password.length() < 6) {
            Toast.makeText(ChildRegisterActivity.this, "Password must be greater then 6 digit", Toast.LENGTH_SHORT).show();
            return;
        } else if(!isValid(Email)){
            Toast.makeText(ChildRegisterActivity.this, "Enter valid email.", Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog.setMessage("Creating User please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mAuth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mDialog.dismiss();
                    saveUserDetails(Email, Password);
                    sendEmailVerification();

                    // OnAuth(task.getResult().getUser());
                    mAuth.signOut();
                } else {
                    mDialog.dismiss();
                    Toast.makeText(ChildRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void saveUserDetails(String email, String password) {
        //TODO save user here

        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_EMAIL, email);
            request.put(KEY_PASSWORD, password);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, saveFcmUrl, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //handle success responce here
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Display error message whenever an error occurs
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }


    //Email verification code using FirebaseUser object and using isSucccessful()function.
    private void sendEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChildRegisterActivity.this, "Check your Email for verification", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Bundle bundle = new Bundle();
        Intent intent = new Intent(ChildRegisterActivity.this, LoginTabActivity.class);
        bundle.putString("TabNumber", "0");
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }


}
