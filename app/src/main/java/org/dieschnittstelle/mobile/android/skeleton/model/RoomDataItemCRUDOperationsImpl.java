package org.dieschnittstelle.mobile.android.skeleton.model;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import java.util.List;

public class RoomDataItemCRUDOperationsImpl implements IDataItemCRUDOperations {

    @Dao
    public static interface RoomDataItemDao {
        @Query("select * from dataitem")
        public List<DataItem> readAll();

        @Insert
        public long create(DataItem item);

        @Update
        public int aktualisiere(DataItem item);

        @Query("DELETE FROM dataitem")
        public int deleteAllDataItems();

       /* @Delete
        public void deleteDataItem(long id);

        */
    }


    @Database(entities = {DataItem.class}, version = 1)
    public abstract static class DataItemDataBase extends RoomDatabase {
        public abstract RoomDataItemDao getDao();
    }

    private DataItemDataBase db;

    public RoomDataItemCRUDOperationsImpl(Context context) {
        db = Room.databaseBuilder(context, DataItemDataBase.class, "dataitems.db").build();
    }

    @Override
    public DataItem createDataItem(DataItem item) {
        // item.beforePersist();
        long id = db.getDao().create(item);
        item.setId(id);
        return null;
    }

    @Override
    public List<DataItem> readAllDataItems() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {

        }
        return db.getDao()
                .readAll();

    }

    @Override
    public DataItem readDataItem() {
        return null;
    }

    @Override
    public boolean updateDataItem(DataItem item) {
        if (db.getDao().aktualisiere(item) > 0) {
            return true;
        }
        return false;
    }

    @Override

    public boolean deleteDataItem(long id) {
      //  db.getDao().deleteDataItem(id);
        return true;
    }

    @Override
    public void deleteAllDataItems() {
        db.getDao().deleteAllDataItems();
    }
}
