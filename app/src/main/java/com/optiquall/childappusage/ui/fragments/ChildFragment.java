package com.optiquall.childappusage.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.optiquall.childappusage.R;
import com.optiquall.childappusage.app.AppPreference;
import com.optiquall.childappusage.app.MySingleton;
import com.optiquall.childappusage.ui.ChildRegisterActivity;
import com.optiquall.childappusage.ui.MainActivity;
import com.optiquall.childappusage.util.SessionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import static com.optiquall.childappusage.app.MyApplication.BASE_URL;
import static com.optiquall.childappusage.util.AppUtil.hideKeyboard;
import static com.optiquall.childappusage.util.Utils.isValid;

public class ChildFragment extends Fragment {
    public static final String userEmail = "";
    public static final String TAG = "LOGIN";
    EditText Email, Password;
    Button LogInButton, RegisterButton, parentDashButton;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseUser mUser;
    String email, password;
    ProgressDialog dialog;
    View view;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private AppPreference appPreference;
    private String KEY_CHILD_EMAIL = "username";
    private String KEY_PASSWORD = "password";
    private String register_child_url = BASE_URL + "/child_register.php";
    private String KEY_STATUS = "status";


    public ChildFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.child_login_fragment, container, false);

        appPreference = new AppPreference(getActivity());
        LogInButton = view.findViewById(R.id.buttonLogin);
        RegisterButton = view.findViewById(R.id.buttonRegister);
        parentDashButton = view.findViewById(R.id.parentDashButton);
        Email = view.findViewById(R.id.editEmail);
        Password = view.findViewById(R.id.editPassword);
        dialog = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

//        if (session.isLoggedIn()) {
//            sendToDash();
//        }

        Log.e(TAG, "onCreateView: getChildEmail "+appPreference.getChildEmail().length() );
        if(appPreference.getChildEmail() .length()>0){
            sendToDash();
        }
        // LogInButton.setOnClickListener((View.OnClickListener) this);
        //RegisterButton.setOnClickListener((View.OnClickListener) this);
        //Adding click listener to log in button.
        LogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Calling EditText is empty or no method.
                userSign();
            }
        });


        // Adding click listener to register button.
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Opening new user registration activity using intent on button click.
                Intent intent = new Intent(getActivity(), ChildRegisterActivity.class);
                startActivity(intent);

            }
        });


        Email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    LogInButton.performClick();
                    return true;
                }
                return false;
            }
        });


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        //removeAuthSateListner is used  in onStart function just for checking purposes,it helps in logging you out.
        mAuth.removeAuthStateListener(mAuthListner);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            mAuth.removeAuthStateListener(mAuthListner);
        }

    }

    private void userSign() {
        try {
            hideKeyboard(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        email = Email.getText().toString().trim();
        password = Password.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Enter the correct Email", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Enter the correct password", Toast.LENGTH_SHORT).show();
            return;
        } else if (!isValid(email)) {
            Toast.makeText(getActivity(), "Enter valid email.", Toast.LENGTH_SHORT).show();
            return;
        }
        dialog.setMessage("Loging in please wait...");
        dialog.setIndeterminate(true);
        dialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    dialog.dismiss();

                    try {
                        Log.e(TAG, "onComplete: " + task.getException());
                        //Toast.makeText(getActivity(), "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    dialog.dismiss();
                    checkIfEmailVerified();
                }
            }
        });
    }

    //This function helps in verifying whether the email is verified or not.
    private void checkIfEmailVerified() {
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        boolean emailVerified = users.isEmailVerified();
        if (!emailVerified) {
            Toast.makeText(getActivity(), "Please activate your account by clicking activation link sent on your mail.", Toast.LENGTH_SHORT).show();
            mAuth.signOut();

        } else {
            registerUser(email, password);

        }
    }


    private void registerUser(final String email, String password) {

        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_CHILD_EMAIL, email);
            request.put(KEY_PASSWORD, password);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, register_child_url, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        appPreference.setChildEmail(email);
                        sendToDash();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(getActivity()).addToRequestQueue(jsArrayRequest);
    }

    private void sendToDash() {

        Intent intent = new Intent(getActivity(), MainActivity.class);
        // Sending Email to Dashboard Activity using intent.
        intent.putExtra(userEmail, email);
        startActivity(intent);
        getActivity().finish();
    }


}
