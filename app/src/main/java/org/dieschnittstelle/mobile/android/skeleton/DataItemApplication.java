package org.dieschnittstelle.mobile.android.skeleton;

import android.app.Application;
import android.os.AsyncTask;
import android.view.Menu;
import android.widget.Toast;
import android.app.Activity;

import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.RetroFitDataItemCRUDOperationsImpl;
import org.dieschnittstelle.mobile.android.skeleton.model.RoomDataItemCRUDOperationsImpl;
import org.dieschnittstelle.mobile.android.skeleton.model.RoomDataItemCRUDOperationsImplRoomDataItemDao_Impl;

import java.util.function.Consumer;

public class DataItemApplication extends Application {


    private IDataItemCRUDOperations crudOperations;

    @Override
    public void onCreate() {
        super.onCreate();

        crudOperations = new RetroFitDataItemCRUDOperationsImpl();

    }



    public IDataItemCRUDOperations getCrudOperations() {
        try {
            return crudOperations;

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }



}
