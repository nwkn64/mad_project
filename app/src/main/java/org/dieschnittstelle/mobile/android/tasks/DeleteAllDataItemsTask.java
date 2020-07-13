package org.dieschnittstelle.mobile.android.tasks;

import androidx.appcompat.app.AppCompatActivity;

import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;

import java.util.concurrent.CompletableFuture;

public class DeleteAllDataItemsTask{

    private IDataItemCRUDOperations crudOperations;
    private AppCompatActivity owner;

    public DeleteAllDataItemsTask(AppCompatActivity owner, IDataItemCRUDOperations crudOperations) {
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
