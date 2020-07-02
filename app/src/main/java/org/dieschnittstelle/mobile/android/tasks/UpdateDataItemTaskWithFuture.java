package org.dieschnittstelle.mobile.android.tasks;


import android.app.Activity;
import android.content.Context;

import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;
import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;

import java.util.concurrent.CompletableFuture;

public class UpdateDataItemTaskWithFuture {

    private IDataItemCRUDOperations crudOperations;
    private Activity owner;

    public UpdateDataItemTaskWithFuture(Activity owner, IDataItemCRUDOperations crudOperations) {
        this.owner = owner;
        this.crudOperations = crudOperations;
    }

    public CompletableFuture<Boolean> execute(DataItem item) {
        System.out.println(item);
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean updated = crudOperations.updateDataItem(item);
                owner.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultFuture.complete(updated);

                    }
                });
            }
        }).start();
        return resultFuture;
    }
}
