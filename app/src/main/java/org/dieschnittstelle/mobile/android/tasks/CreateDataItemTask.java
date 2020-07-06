package org.dieschnittstelle.mobile.android.tasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;
import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;

import java.util.function.Consumer;

public class CreateDataItemTask extends AsyncTask<DataItem, Void, DataItem> {

    private IDataItemCRUDOperations crudOperations;
    private ProgressBar progressBar;
    private Consumer<DataItem> onDoneConsumer;

    @Override
    protected void onPreExecute() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public CreateDataItemTask(IDataItemCRUDOperations crudOperations, ProgressBar progressBar, Consumer<DataItem> onDoneConsumer) {
        this.crudOperations = crudOperations;
        this.progressBar = progressBar;
        this.onDoneConsumer = onDoneConsumer;
    }

    @Override
    protected DataItem doInBackground(DataItem... dataItems) {
        System.out.println("bubu");
        System.out.println(dataItems[0].getName());

        System.out.println(dataItems[0].toString());
        crudOperations.createDataItem(dataItems[0]);
        return dataItems[0];
    }

    @Override
    protected void onPostExecute(DataItem dataItem) {
        onDoneConsumer.accept(dataItem);
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
            progressBar = null;
        }
    }
}
