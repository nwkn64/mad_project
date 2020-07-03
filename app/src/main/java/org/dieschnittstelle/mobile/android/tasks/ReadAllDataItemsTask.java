package org.dieschnittstelle.mobile.android.tasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;
import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;

import java.util.List;
import java.util.function.Consumer;

public class ReadAllDataItemsTask extends AsyncTask<Void, Void, List<DataItem>> {
    @SuppressLint("StaticFieldLeak")
    private ProgressBar progressBar;
    private IDataItemCRUDOperations crudOperations;
    private Consumer<List<DataItem>> onDoneConsumer;

    public ReadAllDataItemsTask(ProgressBar progressBar, IDataItemCRUDOperations crudOperations, Consumer<List<DataItem>> onDoneConsumer) {
        this.progressBar = progressBar;
        this.crudOperations = crudOperations;
        this.onDoneConsumer = onDoneConsumer;
    }

    @Override
    protected void onPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected List<DataItem> doInBackground(Void... voids) {
      return crudOperations.readAllDataItems();
    }

    @Override
    protected void onPostExecute(List<DataItem> dataItems) {
        onDoneConsumer.accept(dataItems);
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }

    }
}
