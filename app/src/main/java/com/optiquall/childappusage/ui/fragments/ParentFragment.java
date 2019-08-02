package com.optiquall.childappusage.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.optiquall.childappusage.R;
import com.optiquall.childappusage.app.AppPreference;
import com.optiquall.childappusage.app.MySingleton;
import com.optiquall.childappusage.ui.ParentRegisterActivity;
import com.optiquall.childappusage.ui.ParentRequestChildPermissionActivity;
import com.optiquall.childappusage.util.SessionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import static com.optiquall.childappusage.app.MyApplication.BASE_URL;
import static com.optiquall.childappusage.util.AppUtil.hideKeyboard;
import static com.optiquall.childappusage.util.Utils.isValid;

public class ParentFragment extends Fragment {
    public static final String userEmail = "";
    public static final String TAG = "LOGIN";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_USERNAME = "parentEmail";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EMPTY = "";
    EditText Email, Password;
    Button LogInButton, RegisterButton, parentDashButton;
    String email, password;
    ProgressDialog dialog;
    View view;
    private EditText etUsername;
    private EditText etPassword;
    private String parentEmail;
    private AppPreference appPreference;
    private ProgressDialog pDialog;
    private String login_url = BASE_URL + "/parent_login.php";



    public ParentFragment() {
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
        view = inflater.inflate(R.layout.parent_login_fragment, container, false);

        appPreference = new AppPreference(getActivity());
        LogInButton = view.findViewById(R.id.buttonLogin);
        RegisterButton = view.findViewById(R.id.buttonRegister);

        Email = view.findViewById(R.id.editEmail);
        Password = view.findViewById(R.id.editPassword);
        dialog = new ProgressDialog(getActivity());


//        if(session.isLoggedIn()){
//            loadDashboard();
//        }

        Log.e(TAG, "onCreateView: getParentEmail "+appPreference.getParentEmail().length() );
        Log.e(TAG, "onCreateView: getParentEmail "+appPreference.getParentEmail());
        if(appPreference.getParentEmail().length() > 0){
            loadDashboard();
        }

        LogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Calling EditText is empty or no method.
                try {
                    hideKeyboard(getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                parentEmail = Email.getText().toString().trim();
                password = Password.getText().toString().trim();
                if (isValid(parentEmail)) {
                    login();
                } else {
                    Toast.makeText(getActivity(), "Enter valid email.", Toast.LENGTH_SHORT).show();
                }

            }
        });


        // Adding click listener to register button.
        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Opening new user registration activity using intent on button click.
                Intent intent = new Intent(getActivity(), ParentRegisterActivity.class);
                startActivity(intent);
            }
        });


        Password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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


    private void displayLoader() {
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Logging In.. Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    private void loadDashboard() {
        Log.e(TAG, "loadDashboard: parentEmail: "+parentEmail );

        Intent i = new Intent(getActivity(), ParentRequestChildPermissionActivity.class);
        i.putExtra("parentEmail", parentEmail);
        startActivity(i);
        getActivity().finish();
    }

    private void login() {
        displayLoader();
        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_USERNAME, parentEmail);
            request.put(KEY_PASSWORD, password);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Log.e(TAG, "login: " + request.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, login_url, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();
                        try {
                            //Check if user got logged in successfully

                            if (response.getInt(KEY_STATUS) == 0) {
                                appPreference.setParentEmail(parentEmail);
                                loadDashboard();

                            } else {
                                Toast.makeText(getActivity(),
                                        response.getString(KEY_MESSAGE), Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();
                        Log.e(TAG, "onErrorResponse: " + error.getMessage());
                        //Display error message whenever an error occurs
                        Toast.makeText(getActivity(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(getActivity()).addToRequestQueue(jsArrayRequest);
    }


}

