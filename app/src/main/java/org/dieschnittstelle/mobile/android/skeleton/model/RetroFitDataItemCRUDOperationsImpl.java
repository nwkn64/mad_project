package org.dieschnittstelle.mobile.android.skeleton.model;


import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class RetroFitDataItemCRUDOperationsImpl implements IDataItemCRUDOperations {


    public static interface TodoWebAPI {

        @POST("api/todos")
        public Call<DataItem> createItem(@Body DataItem item);

        @GET("api/todos")
        public Call<List<DataItem>> readAllItems();

        @PUT("api/todos/{id}")
        public Call<DataItem> updateDataItem(@Path("id") long id, @Body DataItem item);

    }

    private TodoWebAPI webAPI;

    public RetroFitDataItemCRUDOperationsImpl() {
        try{
            Retrofit apiRoot = new Retrofit.Builder()
                    .baseUrl("http://192.168.0.101:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            webAPI = apiRoot.create(TodoWebAPI.class);
        } catch(Exception e){

        }


    }

    @Override
    public DataItem createDataItem(@Body DataItem item) {
        try {
            Log.i("RetrofitCRUD", "createItem() " + item);
            return webAPI.createItem(item).execute().body();
        } catch (Exception e) {
            Log.e("RetrofitCRUD", "got exception", e);
            return null;
        }
    }

    @Override
    public List<DataItem> readAllDataItems() {
        try {
            return webAPI.readAllItems().execute().body();
        } catch (Exception e) {
            Log.e("RetrofitCRUD", "got exception", e);
            return null;
        }
    }

    @Override
    public DataItem readDataItem() {
        return null;
    }

    @Override
    public boolean updateDataItem(DataItem item) {
        try {
            if (webAPI.readAllItems().execute().body() != null) {
                return true;
            } else{
                return false;
            }
        } catch (Exception e) {
            Log.e("RetrofitCRUD", "got exception", e);
            return false;
        }
    }

    @Override
    public boolean deleteDataItem(long id) {
        return false;
    }

    @Override
    public void deleteAllDataItems() {
    }
}
