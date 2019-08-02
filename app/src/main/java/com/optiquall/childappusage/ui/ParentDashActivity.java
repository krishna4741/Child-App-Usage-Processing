package com.optiquall.childappusage.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.ChangeTransform;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.optiquall.childappusage.data.AppItem;
import com.optiquall.childappusage.util.AppUtil;
import com.optiquall.childappusage.util.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentDashActivity extends AppCompatActivity {
    private static final String TAG = "ParentDashActivity";
    final List<AppItem> blocked_item = new ArrayList<>();
    ImageView search_email;
    TextInputEditText editChildEmail;
    TextInputLayout p_editChildEmail;
    List<AppItem> appItems;
    List<AppItem> selectedAppItems;
    int itemCount;
    GestureDetectorCompat gestureDetector;
    ActionMode actionMode;
    DatabaseReference mDatabase;
    private String email = "";
    private RecyclerView mListrecyclerView;
    private SwipeRefreshLayout mSwipe;
    private ParentDashActivity.MyAdapter mAdapter;
    private long mTotal = 0;
    private TextView mSwitchText;
    private LinearLayout mSort;
    private AlertDialog mDialog;
    private TextView mSortName;
    private View progressOverlay;
    private FloatingActionButton fab;
    private List<AppItem> addItemList;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setAllowReturnTransitionOverlap(true);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementExitTransition(new ChangeTransform());
        setContentView(R.layout.activity_parent_dash);
        email = getIntent().getStringExtra("email");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Block Applications");
            actionBar.setSubtitle("Select applications to block");

        }

        initViews();

        onClicks();

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        email = savedInstanceState.getString("email");

        search_email.performClick();
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restartd.
        savedInstanceState.putString("email", email);
        // etc.
        super.onSaveInstanceState(savedInstanceState);
    }


    private void onClicks() {

        if(email!=null){
            getRunningAppsFromEmail(email);
        }

        search_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // email = editChildEmail.getText().toString().trim();
                if (validEmail(email)) {
                    getRunningAppsFromEmail(email);
                } else {
                    //email error here
                    p_editChildEmail.setError("Enter valid email.");
                    editChildEmail.requestFocus();
                }
            }

        });

        editChildEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    search_email.performClick();
                    return true;
                }
                return false;
            }
        });
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                search_email.performClick();
            }
        });
        mSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerSort();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addItemList.size() > 0) {
                    handleBlockedAppList();
                } else {
                    Toast.makeText(ParentDashActivity.this, "No item in block list.", Toast.LENGTH_SHORT).show();
                    fab.hide();
                }

            }
        });
    }

    public String EncodeString(String string) {
        return string.replace(".", ",");
    }

    private void handleBlockedAppList() {
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        String userId = mDatabase.push().getKey();
        Log.e(TAG, "onPreExecute: userId: " + userId);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.e(TAG, "updateData: size: " + addItemList.size());
        Log.e(TAG, "updateData: currentUser: " + currentUser.getEmail());
        //updating firebase here
        //formatted email

        String formatted_email = EncodeString(email);
        Log.e(TAG, "updateData: currentUser: formatted_email " + formatted_email);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String todaysDate = df.format(c);
        for (int i = 0; i < addItemList.size(); i++) {
            try {
                mDatabase.child(formatted_email).child("blocked_apps").child(todaysDate).child(addItemList.get(i).mName).setValue(addItemList.get(i));
            } catch (Exception e) {
                Log.e(TAG, "onPostExecute: " + e);
            }
        }
        Toast.makeText(this, "Blocked app list updated successfully.", Toast.LENGTH_SHORT).show();
        search_email.performClick();
    }

    private void handleUI() {
        search_email.setEnabled(false);
        p_editChildEmail.setError(null);
        progressOverlay.setVisibility(View.VISIBLE);
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void triggerSort() {
        mDialog = new AlertDialog.Builder(ParentDashActivity.this)
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

    private void process() {

        mListrecyclerView.setVisibility(View.INVISIBLE);
        int sortInt = PreferenceManager.getInstance().getInt(PreferenceManager.PREF_LIST_SORT);
        mSortName.setText(getSortName(sortInt));
        new MyAsyncTask().execute(sortInt, 1);

    }

    private String getSortName(int sortInt) {
        return getResources().getStringArray(R.array.sort)[sortInt];
    }

    private boolean validEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private void initViews() {
        search_email = findViewById(R.id.search_email);

       // Toast.makeText(this, "email:" + email, Toast.LENGTH_SHORT).show();
        editChildEmail = findViewById(R.id.editChildEmail);
        editChildEmail.setText("epdev4be@gmail.com");
        p_editChildEmail = findViewById(R.id.p_editChildEmail);
        mSwipe = findViewById(R.id.swipe_refresh);
        mSwitchText = findViewById(R.id.enable_text);
        mSort = findViewById(R.id.sort_group);
        mSortName = findViewById(R.id.sort_name);
        progressOverlay = findViewById(R.id.progress_overlay);
        fab = findViewById(R.id.fab);
        selectedAppItems = new ArrayList<>();
        appItems = new ArrayList<AppItem>();

        //init recyclerview
        mAdapter = new MyAdapter();
        mListrecyclerView = findViewById(R.id.list);
        mListrecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mListrecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider, getTheme()));
        mListrecyclerView.addItemDecoration(dividerItemDecoration);
        mListrecyclerView.setAdapter(mAdapter);

    }

    private void getRunningAppsFromEmail(String email) {
        //convert email from . to , format
        final String formatted_email = email.replace(".", ",");
        handleUI();
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String todaysDate = df.format(c);

        appItems.clear();
        DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference collionRef = rootReference.child("users").child(formatted_email).child("app_usage_statistics").child(todaysDate);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressOverlay.setVisibility(View.GONE);
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    AppItem appItem = ds.getValue(AppItem.class);
                    appItems.add(appItem);
                }

                if (appItems.size() > 0) {
                    findViewById(R.id.enable).setVisibility(View.VISIBLE);
                    Collections.sort(appItems, new Comparator<AppItem>() {
                        @Override
                        public int compare(AppItem left, AppItem right) {
                            return (int) (right.mEventTime - left.mEventTime);
                        }
                    });

                    mListrecyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.no_rec).setVisibility(View.GONE);
                    mTotal = 0;
                    for (AppItem item : appItems) {
                        if (item.mUsageTime <= 0) continue;
                        mTotal += item.mUsageTime;
                    }
                    mSwipe.setRefreshing(false);
                    mAdapter.updateData(appItems);
                    search_email.setEnabled(true);
                    mSwitchText.setText(String.format("Screen On Time: " + getResources().getString(R.string.total), AppUtil.formatMilliSeconds(mTotal)));
                } else {
                    mSwipe.setRefreshing(false);
                    mAdapter.updateData(appItems);
                    findViewById(R.id.enable).setVisibility(View.GONE);
                    findViewById(R.id.no_rec).setVisibility(View.VISIBLE);
                    Toast.makeText(ParentDashActivity.this, "Records not updated yet.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        collionRef.addListenerForSingleValueEvent(valueEventListener);
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

            if (integers[0] == 0) {
                Collections.sort(appItems, new Comparator<AppItem>() {
                    @Override
                    public int compare(AppItem left, AppItem right) {
                        return (int) (right.mUsageTime - left.mUsageTime);
                    }
                });
            } else if (integers[0] == 1) {
                Collections.sort(appItems, new Comparator<AppItem>() {
                    @Override
                    public int compare(AppItem left, AppItem right) {
                        return (int) (right.mEventTime - left.mEventTime);
                    }
                });
            } else if (integers[0] == 2) {
                Collections.sort(appItems, new Comparator<AppItem>() {
                    @Override
                    public int compare(AppItem left, AppItem right) {
                        return right.mCount - left.mCount;
                    }
                });
            } else {
                Collections.sort(appItems, new Comparator<AppItem>() {
                    @Override
                    public int compare(AppItem left, AppItem right) {
                        return (int) (right.mMobile - left.mMobile);
                    }
                });
            }


            return appItems;
        }

        @Override
        protected void onPostExecute(List<AppItem> appItems) {

            mListrecyclerView.setVisibility(View.VISIBLE);
            mTotal = 0;
            for (AppItem item : appItems) {
                if (item.mUsageTime <= 0) continue;
                mTotal += item.mUsageTime;
            }
            mSwitchText.setText(String.format("Screen On Time: " + getResources().getString(R.string.total), AppUtil.formatMilliSeconds(mTotal)));
            mSwipe.setRefreshing(false);
            mAdapter.updateData(appItems);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<ParentDashActivity.MyAdapter.MyViewHolder> {

        private List<AppItem> mData;


        MyAdapter() {
            super();
            mData = new ArrayList<>();
            addItemList = new ArrayList<>();

        }

        void updateData(List<AppItem> appItems) {
            mData = appItems;
            notifyDataSetChanged();
        }

        public String EncodeString(String string) {
            return string.replace(".", ",");
        }

        public String DecodeString(String string) {
            return string.replace(",", ".");
        }


        AppItem getItemInfoByPosition(int position) {
            if (mData.size() > position) {
                return mData.get(position);
            }
            return null;
        }

        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            return new MyAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.MyViewHolder holder, int position) {
            holder.setIsRecyclable(true);
            final AppItem item = getItemInfoByPosition(position);
            holder.mName.setText(item.mName);
            holder.mUsage.setText(AppUtil.formatMilliSeconds(item.mUsageTime));
            holder.mTime.setText(String.format(Locale.getDefault(),
                    "%s · %d %s · %s",
                    new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date(item.mEventTime)),
                    item.mCount,
                    getResources().getString(R.string.times_only), AppUtil.humanReadableByteCount(item.mMobile))
            );
            final AppItem model = mData.get(position);
            holder.view.setBackgroundColor(model.isSelected() ? Color.CYAN : Color.WHITE);

            holder.list_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setSelected(true);
                    selectedAppItems.add(item);
                    item.setSelected(!item.isSelected());
                    holder.view.setBackgroundColor(item.isSelected() ? Color.CYAN : Color.WHITE);

                    if (item.isSelected()) {
                        addItemList.add(item);
                        Glide.with(ParentDashActivity.this)
                                .load(R.drawable.checked)
                                .transition(new DrawableTransitionOptions().crossFade())
                                .into(holder.mIcon);
                        Log.e("addItemList", "---------add" + item.toString());

                        fab.show();
                    } else {
                        Glide.with(ParentDashActivity.this)
                                .load(AppUtil.getPackageIcon(ParentDashActivity.this, item.mPackageName))
                                .transition(new DrawableTransitionOptions().crossFade())
                                .into(holder.mIcon);
                        addItemList.remove(item);

                        if (addItemList.size() > 0) {
                            if (!fab.isShown())
                                fab.show();
                        } else {
                            fab.hide();
                        }
                        Log.e("addItemList", "---------remove" + item.toString());
                    }


                    //holder.list_item.setBackgroundColor(Color.GRAY);
                }
            });

            if (mTotal > 0) {
                holder.mProgress.setProgress((int) (item.mUsageTime * 100 / mTotal));
            } else {
                holder.mProgress.setProgress(0);
            }


            Glide.with(ParentDashActivity.this)
                    .load(AppUtil.getPackageIcon(ParentDashActivity.this, item.mPackageName))
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.mIcon);

        }

        @Override
        public int getItemCount() {
            if (addItemList.size() > 0) {
                if (!fab.isShown())
                    fab.show();
            } else {
                if (fab.isShown())
                    fab.hide();
            }
            return mData == null ? 0 : mData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

            private TextView mName;
            private TextView mUsage;
            private TextView mTime;
            private ImageView mIcon;
            private ProgressBar mProgress;
            private RelativeLayout list_item;
            private View view;

            MyViewHolder(View itemView) {
                super(itemView);
                view = itemView;
                mName = itemView.findViewById(R.id.app_name);
                mUsage = itemView.findViewById(R.id.app_usage);
                mTime = itemView.findViewById(R.id.app_time);
                mIcon = itemView.findViewById(R.id.app_image);
                mProgress = itemView.findViewById(R.id.progressBar);
                list_item = itemView.findViewById(R.id.list_item);
                itemView.setOnCreateContextMenuListener(this);
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
