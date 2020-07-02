package org.dieschnittstelle.mobile.android.skeleton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import javax.annotation.Nullable;

import org.dieschnittstelle.mobile.android.skeleton.databinding.ActivityMainListitemBinding;
import org.dieschnittstelle.mobile.android.skeleton.model.DataItem;
import org.dieschnittstelle.mobile.android.skeleton.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.skeleton.model.RoomDataItemCRUDOperationsImpl;
import org.dieschnittstelle.mobile.android.tasks.CreateDataItemTask;
import org.dieschnittstelle.mobile.android.tasks.ReadAllDataItemsTask;
import org.dieschnittstelle.mobile.android.tasks.UpdateDataItemTaskWithFuture;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final int CALL_DETAIL_VIEW_FOR_NEW_ITEM = 0;
    public static final int CALL_DETAIL_VIEW_FOR_EXISTING_ITEM = 0;

    private ViewGroup listView;
    private ArrayAdapter<DataItem> listViewAdapter;
    //neue Liste die sortiert werden kann
    private List<DataItem> itemsList =new ArrayList<>();
    private FloatingActionButton fab;
    private ProgressBar progressBar;

    private IDataItemCRUDOperations crudOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((DataItemApplication) getApplication()).verifyWebAvailable(available -> {
            this.initialiseView();
        });
    }

    private void initialiseView() {
        this.crudOperations = new RoomDataItemCRUDOperationsImpl(this);


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


        new ReadAllDataItemsTask(progressBar,
                crudOperations,
                items -> {
                    listViewAdapter.addAll(items);
                    //ruft nach starten direkt die Sortierung auf
                    sorteListAndFocusItem(null);
                }
        ).execute();

    }
//Sortierung
    private void sorteListAndFocusItem(DataItem item){
    this.itemsList.sort(Comparator.
                    comparing(DataItem::isChecked) //falsche Methode?
                    .thenComparing(DataItem::getName));
                    this.listViewAdapter.notifyDataSetChanged();

//Überprüft ob ein Item übergeben wurde
      if (item != null){
           ((ListView)this.listView).smoothScrollToPosition(this.listViewAdapter.getPosition(item));
       }

    }

    private void onListItemSelected(DataItem item) {
        Intent callDetailViewIntent = new Intent(this, DetailviewActivity.class);
        callDetailViewIntent.putExtra(DetailviewActivity.ARG_ITEM, item);
        startActivityForResult(callDetailViewIntent, CALL_DETAIL_VIEW_FOR_EXISTING_ITEM);
    }

    private void onAddNewListItem() {
        Intent callDetailViewIntentForReturnValue = new Intent(this, DetailviewActivity.class);
        startActivityForResult(callDetailViewIntentForReturnValue, CALL_DETAIL_VIEW_FOR_NEW_ITEM);
    }

    private void createItemAndAddItToList(DataItem item) {
        new CreateDataItemTask(
                crudOperations,
                progressBar,
                created -> {
                    this.listViewAdapter.add(created);
                    //Damit neues Item sortiert werden kann
                    this.sorteListAndFocusItem(created);
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
                existingItem.setChecked(changedItem.getChecked());
                //existingItem.setFavourite(changedItem.getFavourite());
                existingItem.setDescription(changedItem.getDescription());
                existingItem .setContacts(changedItem.getContacts());
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
        showFeedbackMessage("Updated Item " + item.getName() + "with status: " + item.getChecked());
        new UpdateDataItemTaskWithFuture(this, this.crudOperations)
                .execute(item)
                .thenAccept((updated) -> {
                    showFeedbackMessage("Item: " + item.getName() + "has been updated");
                    //
                    this.sorteListAndFocusItem(item); //feedbackmessage auch nach sortierung
                });
    }
}
