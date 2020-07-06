package org.dieschnittstelle.mobile.android.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;
import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DeleteAllDataItemsTask{

    private IDataItemCRUDOperations crudOperations;
    private Activity owner;

    public DeleteAllDataItemsTask(Activity owner, IDataItemCRUDOperations crudOperations) {
        this.owner = owner;
        this.crudOperations = crudOperations;
    }

    public CompletableFuture<Void> execute() {
        CompletableFuture<Void> resultFuture = new CompletableFuture<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                crudOperations.deleteAllDataItems();
                owner.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultFuture.complete(null);

                    }
                });
            }
        }).start();
        return resultFuture;
    }

}
