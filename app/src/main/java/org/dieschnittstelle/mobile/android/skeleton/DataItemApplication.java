package org.dieschnittstelle.mobile.android.skeleton;

import android.app.Application;

import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.RetroFitDataItemCRUDOperationsImpl;

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
