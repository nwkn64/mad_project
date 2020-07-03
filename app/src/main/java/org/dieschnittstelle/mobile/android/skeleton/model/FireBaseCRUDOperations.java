package org.dieschnittstelle.mobile.android.skeleton.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class FireBaseCRUDOperations implements IDataItemCRUDOperations {
    private static DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private static DatabaseReference dbTasks = mRootRef.child("tasks");


    @Override
    public DataItem createDataItem(DataItem item) {
        System.out.println("say miau?");
        item.setId(item.hashCode());
        dbTasks.child(String.valueOf(item.getId())).setValue(item);

        return null;
    }

    @Override
    public List<DataItem> readAllDataItems() {

        List<DataItem> dataItemArr = new ArrayList<>();
        CountDownLatch done = new CountDownLatch(1);

        dbTasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Result will be holded Here
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    DataItem element = dsp.getValue(DataItem.class);
                    dataItemArr.add(element);

                }
                done.countDown();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
        try {
            done.await(); //it will wait till the response is received from firebase.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return dataItemArr;

    }


    @Override
    public DataItem readDataItem() {
        return null;
    }

    @Override
    public boolean updateDataItem(DataItem item) {
        System.out.println(item.getId());

        System.out.println("hier?!?!");
        dbTasks.child(String.valueOf(item.getId())).setValue(item);

        return true;
    }

    @Override
    public boolean deleteDataItem(long id) {
        return false;
    }
}
