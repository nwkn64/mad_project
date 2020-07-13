package org.dieschnittstelle.mobile.android.skeleton;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

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

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final int CALL_DETAIL_VIEW_FOR_NEW_ITEM = 0;
    public static final int CALL_DETAIL_VIEW_FOR_EXISTING_ITEM = 0;

    private ViewGroup listView;
    private ViewGroup mapsView;
    private GoogleMap map;

    private ArrayAdapter<DataItem> listViewAdapter;
    //neue Liste die sortiert werden kann
    private List<DataItem> itemsList = new ArrayList<>();
    private FloatingActionButton fab;
    private ProgressBar progressBar;
    private IDataItemCRUDOperations crudOperations;
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;

    private CameraPosition cameraPosition;
    private LatLng marker;
    private PlacesClient placesClient;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private Location lastKnownLocation;


    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.initialiseView();


    }

    @Override
    public void onMapReady(GoogleMap map) {

        this.map = map;

        itemsList.forEach(obj -> {
            if (obj.getGeoCoordinates() != null) {
                Double geoLat = Double.parseDouble(obj.getGeoCoordinates().split(",")[0]);
                Double geoLong = Double.parseDouble(obj.getGeoCoordinates().split(",")[1]);

                LatLng geoPos = new LatLng(geoLat, geoLong);
                map.addMarker(new MarkerOptions()
                        .position(geoPos)
                        .title(obj.getName()));
            }


        });


        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }


            @Override
            public View getInfoContents(Marker marker) {
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }


        });


        getLocationPermission();

        updateLocationUI();

        getDeviceLocation();


        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);


                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                map.clear();

                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                map.addMarker(markerOptions);
            }


        });


    }

    private void getDeviceLocation() {

        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d("MainActivity", "Current location is null. Using defaults.");
                            Log.e("MainActivity", "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
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
        this.listView.setVisibility(View.VISIBLE);
        this.mapsView = this.findViewById(R.id.mapsView);

        this.fab = this.findViewById(R.id.fab);
        this.progressBar = this.findViewById(R.id.progressBar);

        TabLayout tabView = this.findViewById(R.id.tabs);

        tabView.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                System.out.println(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        listView.setVisibility(View.VISIBLE);
                        mapsView.setVisibility(View.INVISIBLE);
                    case 1:

                        Places.initialize(getApplicationContext(), "AIzaSyB3rlEAF2-E_c8dxbMz3tN60h3Aw-1SEZ0");
                        placesClient = Places.createClient(MainActivity.this);

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);


                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MainActivity.this);


                        mapsView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }


        });
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
        if (crudOperations.getClass().isAssignableFrom(new FireBaseCRUDOperations().getClass())) {
            RoomDataItemCRUDOperationsImpl roomCrud = new RoomDataItemCRUDOperationsImpl(this);
            new ReadAllDataItemsTask(progressBar,
                    roomCrud,
                    items -> {
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
            if (initialLoad) {
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
            default:
                return super.onCreateOptionsMenu((Menu) item);
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
        this.itemsList.sort(Comparator.
                comparing(DataItem::isChecked) //falsche Methode?
                .thenComparing(DataItem::getName));
        this.listViewAdapter.notifyDataSetChanged();

//Überprüft ob ein Item übergeben wurde
        if (item != null) {
            ((ListView) this.listView).smoothScrollToPosition(this.listViewAdapter.getPosition(item));
        }

    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void onListItemSelected(DataItem item) {
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


        if (resultCode == 5) {
            DataItem item = (DataItem) data.getSerializableExtra(DetailviewActivity.ARG_ITEM);
            crudOperations.deleteDataItem(item.getId());
            this.listViewAdapter.remove(item);
            this.listViewAdapter.notifyDataSetChanged();
            //Item das geupdated wurde soll sortiert werden
            this.sorteListAndFocusItem(null);

        } else {
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
