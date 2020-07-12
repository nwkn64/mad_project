package org.dieschnittstelle.mobile.android.skeleton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.room.Room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import javax.annotation.Nullable;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityMainListitemBinding;
import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;
import org.dieschnittstelle.mobile.android.skeleton.model.FireBaseCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.RoomDataItemCRUDOperationsImpl;
import org.dieschnittstelle.mobile.android.tasks.CreateDataItemTask;
import org.dieschnittstelle.mobile.android.tasks.DeleteAllDataItemsTask;
import org.dieschnittstelle.mobile.android.tasks.ReadAllDataItemsTask;
import org.dieschnittstelle.mobile.android.tasks.UpdateDataItemTaskWithFuture;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final int CALL_DETAIL_VIEW_FOR_NEW_ITEM = 0;
    public static final int CALL_DETAIL_VIEW_FOR_EXISTING_ITEM = 1;

    private ViewGroup listView;
    private ArrayAdapter<DataItem> listViewAdapter;
    //neue Liste die sortiert werden kann
    private List<DataItem> itemsList = new ArrayList<>();
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private boolean sorting_fav_date = true;

    private IDataItemCRUDOperations crudOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.initialiseView();

    }

    private void initialiseView() {


        Bundle extras = this.getIntent().getExtras();
        String id = extras.getString("crudOperations");

        if (id.equals("0")) {
            this.crudOperations = new FireBaseCRUDOperations();

        } else {
            this.crudOperations = new RoomDataItemCRUDOperationsImpl(this);

        }


        this.listView = this.findViewById(R.id.listView);
        this.fab = this.findViewById(R.id.fab);
        this.progressBar = this.findViewById(R.id.progressBar);

        this.listViewAdapter = new ArrayAdapter<DataItem>(this, R.layout.activity_main__listitem, R.id.itemName, itemsList) {
            @NonNull
            @Override
            public View getView(int position, @androidx.annotation.Nullable View convertView, @NonNull ViewGroup parent) {

                ActivityMainListitemBinding binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_main__listitem, null, false);

                DataItem item = getItem(position);

                binding.setItem(item);
                binding.setController(MainActivity.this);
                return binding.getRoot();
            }
        };


        ((ListView) listView).setAdapter(this.listViewAdapter);

        ((ListView) listView).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DataItem item = listViewAdapter.getItem(i);
                onListItemSelected(item);
            }
        });

        fab.setOnClickListener((view) -> {
            this.onAddNewListItem();
        });


        synchronize(true);

    }

    private void synchronize(Boolean initialLoad) {
        System.out.println(crudOperations.getClass().isAssignableFrom(new FireBaseCRUDOperations().getClass()));
        if (crudOperations.getClass().isAssignableFrom(new FireBaseCRUDOperations().getClass())) {
            RoomDataItemCRUDOperationsImpl roomCrud = new RoomDataItemCRUDOperationsImpl(this);
            new ReadAllDataItemsTask(progressBar,
                    roomCrud,
                    items -> {
                        System.out.println(items.size());
                        if (items.size() > 0) {
                            crudOperations.deleteAllDataItems();
                            this.listViewAdapter.clear();

                            items.forEach(obj -> {
                                new CreateDataItemTask(
                                        crudOperations,
                                        progressBar,
                                        created -> {
                                            this.listViewAdapter.add(created);
                                            sorteListAndFocusItem(null);
                                            //Damit neues Item sortiert werden kann
                                        }).execute(obj);
                            });

                        } else {
                            new ReadAllDataItemsTask(progressBar,
                                    crudOperations,
                                    fireBaseItems -> {
                                        fireBaseItems.forEach(dat -> {

                                            System.out.println(dat);

                                            new CreateDataItemTask(
                                                    roomCrud,
                                                    progressBar,
                                                    created -> {
                                                        this.listViewAdapter.add(created);
                                                        sorteListAndFocusItem(null);
                                                        //Damit neues Item sortiert werden kann
                                                    }).execute(dat);
                                        });
                                        listViewAdapter.addAll(fireBaseItems);
                                        //ruft nach starten direkt die Sortierung auf

                                    }
                            ).execute();
                        }


                    }).execute();
        } else {
            if(initialLoad){
                new ReadAllDataItemsTask(progressBar,
                        crudOperations,
                        items -> {
                            listViewAdapter.addAll(items);
                            //ruft nach starten direkt die Sortierung auf

                            sorteListAndFocusItem(null);
                        }
                ).execute();
            }

        }


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteFirebase:
            deleteFirebase();
            return true;
            case R.id.deleteRoom:
                deleteRoom();
                return true;
            case R.id.synchronize:
                synchronize(false);
                return true;
            case R.id.FavouriteDate:
                sorting_fav_date = true;
                sorteListAndFocusItem (null);
                return true;
            case R.id.DateFavourite:
                sorting_fav_date = false;
                sorteListAndFocusItem (null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void deleteFirebase() {
        FireBaseCRUDOperations fireBaseCrud = new FireBaseCRUDOperations();

        new DeleteAllDataItemsTask(this, fireBaseCrud)
                .execute().thenAccept(updated -> {
            new ReadAllDataItemsTask(progressBar,
                    fireBaseCrud,
                    items -> {
                        listViewAdapter.addAll(items);
                        //ruft nach starten direkt die Sortierung auf
                    }
            ).execute();
            Toast.makeText(this, "Deleted all tasks from Firebase",
                    Toast.LENGTH_SHORT).show();
        });

        if (crudOperations.getClass().isAssignableFrom(new FireBaseCRUDOperations().getClass())) {
            this.listViewAdapter.clear();
        }
        this.listViewAdapter.notifyDataSetChanged();


    }


    private void deleteRoom() {
        RoomDataItemCRUDOperationsImpl roomCrud = new RoomDataItemCRUDOperationsImpl(this);

        new DeleteAllDataItemsTask(this, roomCrud)
                .execute().thenAccept(updated -> {
            Toast.makeText(this, "Deleted all tasks from Room",
                    Toast.LENGTH_SHORT).show();
        });

        if (crudOperations.getClass().isAssignableFrom(new RoomDataItemCRUDOperationsImpl(this).getClass())) {
            this.listViewAdapter.clear();
        }

        this.listViewAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_sort, menu);
        return true;
    }

    //Sortierung
    private void sorteListAndFocusItem(DataItem item) {

        // instead of sorting the itemsList, we call sort on the listViewAdapter, to only change
        // the displayed order, not the actual order.

        if(sorting_fav_date)
        {
            this.listViewAdapter.sort(Comparator.
                    comparing(DataItem::isChecked)
                    .thenComparing(DataItem::isFavourite)
                    .thenComparing(DataItem::getDateTime)
                    .thenComparing(DataItem::getTimeTime)
                    .thenComparing(DataItem::getName));

        }else{
            this.listViewAdapter.sort(Comparator.
                    comparing(DataItem::isChecked)
                    .thenComparing(DataItem::getDateTime)
                    .thenComparing(DataItem::getTimeTime)
                    .thenComparing(DataItem::isFavourite)
                    .thenComparing(DataItem::getName));
        }

        this.listViewAdapter.notifyDataSetChanged();


//Überprüft ob ein Item übergeben wurde
        if (item != null) {
            ((ListView) this.listView).smoothScrollToPosition(this.listViewAdapter.getPosition(item));
        }

    }


    private void onListItemSelected(DataItem item) {
        System.out.println("miaumiau?!");
        Intent callDetailViewIntent = new Intent(this, DetailviewActivity.class);
        callDetailViewIntent.putExtra(DetailviewActivity.ARG_ITEM, item);
        startActivityForResult(callDetailViewIntent, CALL_DETAIL_VIEW_FOR_EXISTING_ITEM);
    }

    private void onAddNewListItem() {
        Intent callDetailViewIntent = new Intent(this, DetailviewActivity.class);
        startActivityForResult(callDetailViewIntent, CALL_DETAIL_VIEW_FOR_NEW_ITEM);
    }

    private void createItemAndAddItToList(DataItem item) {

        item.setChecked(false);
        new CreateDataItemTask(
                crudOperations,
                progressBar,
                created -> {
                    System.out.println(created.getName());
                    this.listViewAdapter.add(created);
                    this.listViewAdapter.notifyDataSetChanged();
                    //Damit neues Item sortiert werden kann
                }).execute(item);


    }

    private void updateItemAndUpdateList(DataItem item) {

        new UpdateDataItemTaskWithFuture(this, this.crudOperations)
                .execute(item)
                .thenAccept(updated -> {
                    handleResultFromUpdateTask(item, updated);
                });
    }

    private void handleResultFromUpdateTask(DataItem changedItem, boolean updated) {
        if (updated) {
            int existingItemInList = this.listViewAdapter.getPosition(changedItem);
            if (existingItemInList > -1) {
                DataItem existingItem = this.listViewAdapter.getItem(existingItemInList);
                existingItem.setName(changedItem.getName());
                existingItem.setChecked(changedItem.isChecked());
                existingItem.setFavourite(changedItem.isFavourite());
                existingItem.setDescription(changedItem.getDescription());
                existingItem.setContacts(changedItem.getContacts());
                existingItem.setDate(changedItem.getDate());
                existingItem.setDateTime(changedItem.getDateTime());
                existingItem.setTime(changedItem.getTime());
                existingItem.setTimeTime(changedItem.getTimeTime());
                this.listViewAdapter.notifyDataSetChanged();
                //Item das geupdated wurde soll sortiert werden
                this.sorteListAndFocusItem(existingItem);

            } else {
                showFeedbackMessage("Updated" + changedItem.getName() + "Updated Item cannot be found");

            }
        } else {
            showFeedbackMessage("Updated" + changedItem.getName() + "Item could not be updated in database");

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        System.out.println(resultCode);
        System.out.println(requestCode);
        if (requestCode == CALL_DETAIL_VIEW_FOR_NEW_ITEM) {
            if (resultCode == Activity.RESULT_OK) {
                DataItem item = (DataItem) data.getSerializableExtra(DetailviewActivity.ARG_ITEM);
                createItemAndAddItToList(item);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showFeedbackMessage(" item name was canceled");
            } else {
                showFeedbackMessage("no item name received, what's wrong?");
            }
        } else if (requestCode == CALL_DETAIL_VIEW_FOR_EXISTING_ITEM) {
            if (resultCode == Activity.RESULT_OK) {
                DataItem item = (DataItem) data.getSerializableExtra(DetailviewActivity.ARG_ITEM);
                updateItemAndUpdateList(item);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);

        }
    }


    private void showFeedbackMessage(String msg) {
        Snackbar.make(findViewById(R.id.listView), msg, BaseTransientBottomBar.LENGTH_LONG).show();

    }

    public void onListItemChanged(DataItem item) {
        showFeedbackMessage("Updated Item " + item.getName() + "with status: " + item.isChecked());
        new UpdateDataItemTaskWithFuture(this, this.crudOperations)
                .execute(item)
                .thenAccept((updated) -> {
                    showFeedbackMessage("Item: " + item.getName() + " has been updated");
                    //
                    this.sorteListAndFocusItem(item); //feedbackmessage auch nach sortierung
                });
    }
}
