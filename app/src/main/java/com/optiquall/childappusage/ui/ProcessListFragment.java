package com.optiquall.childappusage.ui;

import android.app.ListFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.optiquall.childappusage.adapter.ProcessListAdapter;
import com.optiquall.childappusage.util.AndroidAppProcessLoader;

import java.util.List;

public class ProcessListFragment extends ListFragment implements AndroidAppProcessLoader.Listener {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        getListView().setFastScrollEnabled(true);
        new AndroidAppProcessLoader(getActivity(), this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onComplete(List<AndroidAppProcess> processes) {
        setListAdapter(new ProcessListAdapter(getActivity(), processes));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AndroidAppProcess process = (AndroidAppProcess) getListAdapter().getItem(position);
        ProcessInfoDialog dialog = new ProcessInfoDialog();
        Bundle args = new Bundle();
        args.putParcelable("process", process);
        dialog.setArguments(args);
        dialog.show(getActivity().getFragmentManager(), "ProcessInfoDialog");
    }

}
