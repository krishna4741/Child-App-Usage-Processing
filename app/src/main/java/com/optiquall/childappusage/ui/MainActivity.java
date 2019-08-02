package com.optiquall.childappusage.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optiquall.childappusage.R;
import com.optiquall.childappusage.app.AppPreference;
import com.optiquall.childappusage.app.MySingleton;
import com.optiquall.childappusage.data.AppItem;
import com.optiquall.childappusage.data.DataManager;
import com.optiquall.childappusage.db.DbIgnoreExecutor;
import com.optiquall.childappusage.service.AlarmService;
import com.optiquall.childappusage.service.AppService;
import com.optiquall.childappusage.service.CheckedBlockedAppListService;
import com.optiquall.childappusage.util.AppUtil;
import com.optiquall.childappusage.util.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.optiquall.childappusage.app.MyApplication.BASE_URL;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String KEY_EMAIL = "email";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";

    public static Activity mactivity;
    //for firebase client
    DatabaseReference mDatabase;
    String userId;
    private LinearLayout mSort;
    private Switch mSwitch;
    private TextView mSwitchText;
    private RecyclerView mList;
    private MyAdapter mAdapter;
    private AlertDialog mDialog;
    private SwipeRefreshLayout mSwipe;
    private TextView mSortName;
    private long mTotal;
    private int mDay;
    private PackageManager mPackageManager;
    //for firebase auth
    private FirebaseAuth mAuth;
    private String formatted_email, email;
    private AppPreference appPreference;
    private String saveFcmUrl = BASE_URL + "/saveFcmUrl.php";
    private ProgressDialog pDialog;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // https://guides.codepath.com/android/Shared-Element-Activity-Transition
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade(Fade.OUT));
        setContentView(R.layout.activity_main);
        mPackageManager = getPackageManager();

        appPreference = new AppPreference(this);
        mSort = findViewById(R.id.sort_group);
        mSortName = findViewById(R.id.sort_name);
        mSwitch = findViewById(R.id.enable_switch);
        mSwitchText = findViewById(R.id.enable_text);
        mAdapter = new MyAdapter();

        mList = findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mList.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider, getTheme()));
        mList.addItemDecoration(dividerItemDecoration);
        mList.setAdapter(mAdapter);

        initLayout();
        initEvents();
        initSpinner();
        initSort();
        checkForBlockedAppList();
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            process();
            startService(new Intent(this, AlarmService.class));
        }
    }

    private void checkForBlockedAppList() {


        currentUser = mAuth.getCurrentUser();
        email = currentUser.getEmail();
        saveFcmTokenToServer(email, appPreference.getFCMTOKEN());
        formatted_email = EncodeString(email);
        Log.e(TAG, "updateData: currentUser: formatted_email " + formatted_email);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String todaysDate = df.format(c);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();


        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child("users").child(formatted_email).hasChild("blocked_apps")) {
                    //if (snapshot.hasChild("users")) {
                    if (!isMyServiceRunning(CheckedBlockedAppListService.class)) {
                        Intent serviceIntent = new Intent(MainActivity.this, CheckedBlockedAppListService.class);
                        serviceIntent.putExtra("email", formatted_email);
                        startService(serviceIntent);
                    }
                } else {
                    Log.e(TAG, "onDataChange1: not exist - stop service ");
                    if (isMyServiceRunning(CheckedBlockedAppListService.class)) {
                        stopService(new Intent(MainActivity.this, CheckedBlockedAppListService.class));
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



/*        for (int i = 0; i < appItems.size(); i++) {
            try {
                mDatabase.child(formatted_email).child("app_usage_statistics").child(todaysDate).child(appItems.get(i).mName).setValue(appItems.get(i));
            } catch (Exception e) {
                Log.e(TAG, "onPostExecute: " + e);
            }
        }*/

    }

    private void displayLoader() {
        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Please wait...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    private void saveFcmTokenToServer(String email, String fcmtoken) {
        displayLoader();
        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_EMAIL, email);
            request.put(KEY_FCM_TOKEN, fcmtoken);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, saveFcmUrl, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();
                        try {
                            Toast.makeText(getApplicationContext(),
                                    response.getString(KEY_MESSAGE), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.dismiss();

                        //Display error message whenever an error occurs
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsArrayRequest);
    }


    private void initLayout() {

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();
// Creating new user node, which returns the unique key value
// new user node would be /users/$userid/
        userId = mDatabase.push().getKey();
        Log.e(TAG, "onPreExecute: userId: " + userId);
// creating user object
// pushing user to 'users' node using the userId
        mactivity = MainActivity.this;
        mSwipe = findViewById(R.id.swipe_refresh);
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            mSwitchText.setText(R.string.enable_apps_monitoring);
            mSwitch.setVisibility(View.GONE);
            mSort.setVisibility(View.VISIBLE);
            mSwipe.setEnabled(true);
        } else {
            mSwitchText.setText(R.string.enable_apps_monitor);
            mSwitch.setVisibility(View.VISIBLE);
            mSort.setVisibility(View.GONE);
            mSwitch.setChecked(false);
            mSwipe.setEnabled(false);
        }
    }

    private void initSort() {
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            mSort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    triggerSort();
                }
            });
        }
    }

    private void triggerSort() {
        mDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.sort)
                .setSingleChoiceItems(R.array.sort, PreferenceManager.getInstance().getInt(PreferenceManager.PREF_LIST_SORT), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PreferenceManager.getInstance().putInt(PreferenceManager.PREF_LIST_SORT, i);
                        process();
                        mDialog.dismiss();
                    }
                })
                .create();
        mDialog.show();
    }

    private void initSpinner() {
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            Spinner spinner = findViewById(R.id.spinner);
            spinner.setVisibility(View.VISIBLE);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.duration, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mDay != i) {
                        int[] values = getResources().getIntArray(R.array.duration_int);
                        mDay = values[i];
                        process();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void initEvents() {
        if (!DataManager.getInstance().hasPermission(getApplicationContext())) {
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        try {

                            Log.e(TAG, "onCheckedChanged: true running");
                            Intent intent = new Intent(MainActivity.this, AppService.class);
                            intent.putExtra(AppService.SERVICE_ACTION, AppService.SERVICE_ACTION_CHECK);
                            startService(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                process();
                startBlockAppService();
            }
        });
    }

    private void startBlockAppService() {
        if (!isMyServiceRunning(CheckedBlockedAppListService.class)) {
            Intent serviceIntent = new Intent(MainActivity.this, CheckedBlockedAppListService.class);
            serviceIntent.putExtra("email", formatted_email);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!DataManager.getInstance().hasPermission(getApplicationContext())) {
            mSwitch.setChecked(false);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DataManager.getInstance().hasPermission(this)) {
            mSwipe.setEnabled(true);
            mSort.setVisibility(View.VISIBLE);
            mSwitch.setVisibility(View.GONE);
            initSpinner();
            initSort();
            process();
        }
    }

    private void process() {
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            mList.setVisibility(View.INVISIBLE);
            int sortInt = PreferenceManager.getInstance().getInt(PreferenceManager.PREF_LIST_SORT);
            mSortName.setText(getSortName(sortInt));
            new MyAsyncTask().execute(sortInt, mDay);
        }
    }

    private String getSortName(int sortInt) {
        return getResources().getStringArray(R.array.sort)[sortInt];
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AppItem info = mAdapter.getItemInfoByPosition(item.getOrder());
        switch (item.getItemId()) {
            case R.id.ignore:
                DbIgnoreExecutor.getInstance().insertItem(info);
                process();
                Toast.makeText(this, R.string.ignore_success, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.open:
                startActivity(mPackageManager.getLaunchIntentForPackage(info.mPackageName));
                return true;
            case R.id.more:
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + info.mPackageName));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), 1);

                return true;
            case R.id.sort:
                triggerSort();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(">>>>>>>>", "result code " + requestCode + " " + resultCode);
        if (resultCode > 0) process();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) mDialog.dismiss();
    }

    public String EncodeString(String string) {
        return string.replace(".", ",");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.finishAffinity();
        } else{
            this.finish();
            System.exit(0);
        }

    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


        private List<AppItem> mData;

        MyAdapter() {
            super();
            mData = new ArrayList<>();
        }

        public String DecodeString(String string) {
            return string.replace(",", ".");
        }

        void updateData(List<AppItem> appItems) {
            mData = appItems;
            notifyDataSetChanged();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            Log.e(TAG, "updateData: size: " + appItems.size());
            Log.e(TAG, "updateData: currentUser: " + currentUser.getEmail());
            //updating firebase here
            //formatted email
            String formatted_email = EncodeString(currentUser.getEmail());
            Log.e(TAG, "updateData: currentUser: formatted_email " + formatted_email);
            Date c = Calendar.getInstance().getTime();
            System.out.println("Current time => " + c);

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String todaysDate = df.format(c);
            for (int i = 0; i < appItems.size(); i++) {
                try {
                    mDatabase.child(formatted_email).child("app_usage_statistics").child(todaysDate).child(appItems.get(i).mName).setValue(appItems.get(i));
                } catch (Exception e) {
                    Log.e(TAG, "onPostExecute: " + e);
                }
            }
        }

        AppItem getItemInfoByPosition(int position) {
            if (mData.size() > position) {
                return mData.get(position);
            }
            return null;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            AppItem item = getItemInfoByPosition(position);
            holder.mName.setText(item.mName);
            holder.mUsage.setText(AppUtil.formatMilliSeconds(item.mUsageTime));
            holder.mTime.setText(String.format(Locale.getDefault(),
                    "%s · %d %s · %s",
                    new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date(item.mEventTime)),
                    item.mCount,
                    getResources().getString(R.string.times_only), AppUtil.humanReadableByteCount(item.mMobile))
            );
            if (mTotal > 0) {
                holder.mProgress.setProgress((int) (item.mUsageTime * 100 / mTotal));
            } else {
                holder.mProgress.setProgress(0);
            }


            Glide.with(MainActivity.this)
                    .load(AppUtil.getPackageIcon(MainActivity.this, item.mPackageName))
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.mIcon);
            holder.setOnClickListener(item);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

            private TextView mName;
            private TextView mUsage;
            private TextView mTime;
            private ImageView mIcon;
            private ProgressBar mProgress;

            MyViewHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.app_name);
                mUsage = itemView.findViewById(R.id.app_usage);
                mTime = itemView.findViewById(R.id.app_time);
                mIcon = itemView.findViewById(R.id.app_image);
                mProgress = itemView.findViewById(R.id.progressBar);
                itemView.setOnCreateContextMenuListener(this);
            }

            @SuppressLint("RestrictedApi")
            void setOnClickListener(final AppItem item) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_PACKAGE_NAME, item.mPackageName);
                        intent.putExtra(DetailActivity.EXTRA_DAY, mDay);
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(MainActivity.this, mIcon, "profile");
                        startActivityForResult(intent, 1, options.toBundle());
                    }
                });
            }

            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                int position = getAdapterPosition();
                AppItem item = getItemInfoByPosition(position);
                contextMenu.setHeaderTitle(item.mName);
                contextMenu.add(Menu.NONE, R.id.open, position, getResources().getString(R.string.open));
                if (item.mCanOpen) {
                    contextMenu.add(Menu.NONE, R.id.more, position, getResources().getString(R.string.app_info));
                }
                contextMenu.add(Menu.NONE, R.id.ignore, position, getResources().getString(R.string.ignore));
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends AsyncTask<Integer, Void, List<AppItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipe.setRefreshing(true);
        }

        @Override
        protected List<AppItem> doInBackground(Integer... integers) {
            Log.e(TAG, "doInBackground: integers[0], integers[1] " + integers[0] + "  " + integers[1]);
            return DataManager.getInstance().getApps(getApplicationContext(), integers[0], integers[1]);
        }

        @Override
        protected void onPostExecute(List<AppItem> appItems) {

            mList.setVisibility(View.VISIBLE);
            mTotal = 0;
            for (AppItem item : appItems) {
                if (item.mUsageTime <= 0) continue;
                mTotal += item.mUsageTime;
                item.mCanOpen = mPackageManager.getLaunchIntentForPackage(item.mPackageName) != null;
            }
            mSwitchText.setText(String.format(getResources().getString(R.string.total), AppUtil.formatMilliSeconds(mTotal)));
            mSwipe.setRefreshing(false);
            mAdapter.updateData(appItems);
        }
    }
}
