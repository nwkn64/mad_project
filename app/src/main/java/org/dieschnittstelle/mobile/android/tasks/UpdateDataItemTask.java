package org.dieschnittstelle.mobile.android.tasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;
import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;

import java.util.function.Consumer;

public class UpdateDataItemTask extends AsyncTask<DataItem, Void, Boolean> {


    private IDataItemCRUDOperations crudOperations;
private Consumer<Boolean> onDoneConsumer;

    public UpdateDataItemTask(IDataItemCRUDOperations crudOperations, Consumer<Boolean> onDoneConsumer) {
        this.crudOperations = crudOperations;
        this.onDoneConsumer = onDoneConsumer;
    }

    @Override
    protected Boolean doInBackground(DataItem... dataItems) {
        return crudOperations.updateDataItem(dataItems[0]);
    }


    @Override
    protected void onPostExecute(Boolean result) {
        onDoneConsumer.accept(result);
    }
}
