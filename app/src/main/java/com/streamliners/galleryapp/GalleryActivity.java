package com.streamliners.galleryapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class GalleryActivity extends AppCompatActivity {

    ActivityGalleryBinding b;
    private static final int RESULT_LOAD_IMAGE = 0;
    SharedPreferences preferences;
    List<Item> items = new ArrayList<>();
    private boolean isDialogBoxShowed = false;
    Gson gson = new Gson();
    private boolean isSorted;

    ItemCardBinding bindingToRemove;
    private List<String> urls = new ArrayList<>();
    private String imageUrl;
    ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if(savedInstanceState != null){
            savedInstance(savedInstanceState);
        }else{
            sharedPreferences();
        }
    }

    /**
     * This method will prevent loss of data when the app is completely stopped.
     */

    private void sharedPreferences() {
        b.noItemTV.setVisibility(View.GONE);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String json = preferences.getString(Constants.ITEMS, null);
        items = gson.fromJson(json, new TypeToken<List<Item>>(){}.getType());
        if(items != null){
            setUpRecyclerView();
        }
        else {
            items = new ArrayList<>();
        }
    }

    /**
     * This method contains the saved instance data and it will prevent loss of data when the screen
     *  is rotated.
     * @param savedInstanceState
     */

    private void savedInstance(Bundle savedInstanceState){
        b.noItemTV.setVisibility(View.GONE);
        String json = savedInstanceState.getString(Constants.ITEMS, null);
        items = gson.fromJson(json, new TypeToken<List<Item>>(){}.getType());
        if(items != null){
            setUpRecyclerView();
        }
        else {
            items = new ArrayList<>();
        }
    }


    //Actions menu method

    /**
     * Gives Add Image Option in menu
     *
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        MenuItem item = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    Item deletedItem = null;
    List<Item> archivedItem = new ArrayList<>();

    // Callback for swipe action and move action

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(items, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAdapterPosition();

            switch (direction){
                case ItemTouchHelper.LEFT:
                    deletedItem = items.get(position);
                    items.remove(position);
                    adapter.notifyItemRemoved(position);
                    Snackbar.make(b.list, "Item Deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    items.add(position, deletedItem);
                                    adapter.notifyItemInserted(position);
                                }
                            }).show();
                    break;
                case ItemTouchHelper.RIGHT:
                    Item archivedItemName = items.get(position);
                    archivedItem.add(archivedItemName);
                    items.remove(position);
                    adapter.notifyItemRemoved(position);
                    Snackbar.make(b.list, "Item Archived", Snackbar.LENGTH_LONG)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    archivedItem.remove(archivedItem.lastIndexOf(archivedItemName));
                                    items.add(position, archivedItemName);
                                    adapter.notifyItemInserted(position);
                                }
                            }).show();

                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(GalleryActivity.this, R.color.teal_200))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor( GalleryActivity.this, R.color.purple_500))
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_archive_24)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    /**
     * Shows add image dialog on clicking icon in menu
     *
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_image) {
            showAddImageDialog();
            return true;
        }
        if (item.getItemId() == R.id.addFromGallery) {
            addFromGallery();
            return true;
        }
        if (item.getItemId() == R.id.action_sort){
            sortList();
            return true;
        }
        return false;
    }

    /**
     * This function will sort the list alphabetically.
     */

    private void sortList() {
        if(!isSorted){
            isSorted = true;
            List<Item> sortedItems = new ArrayList<>(items);
            Collections.sort(sortedItems, (p1,p2) -> p1.label.compareTo(p2.label));
            if(adapter != null){
                adapter.itemList = sortedItems;
                adapter.showSortedItems();
                b.list.setAdapter(adapter);
            }
        }else{
            isSorted = false;
        }
    }


    /**
     * Shows Image Dialog Box
     */

    private void showAddImageDialog() {
        if (this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            isDialogBoxShowed = true;
            // To set the screen orientation in portrait mode only
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        items.add(item);
                        // inflateViewForItem(item);
                        setUpRecyclerView();
                        b.noItemTV.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String error) {
                        new MaterialAlertDialogBuilder(GalleryActivity.this)
                                .setTitle("Error")
                                .setMessage(error)
                                .show();
                    }
                });
    }

    /**
     * This method will call adapter of ItemAdapter to add the card into the recycler view.
     *
     */

    private void setUpRecyclerView() {
        adapter = new ItemAdapter(this, items);
        b.list.setLayoutManager(new LinearLayoutManager(this));
        b.list.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(b.list);
    }

//    /**
//     * Adds Image Card to Linear Layout
//     * @param item
//     */
//    private void inflateViewAt(Item item,int index) {
//
//        //Inflate Layout
//        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
//        //Bind Data
//        Glide.with(this)
//                .asBitmap()
//                .load(item.imageUrl)
//                .into(binding.imageView);
//
//        binding.title.setText(item.label);
//        binding.title.setBackgroundColor(item.color);
//
//        //Add it to the list
//
//        b.list.addView(binding.getRoot(),index);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//    }
//
//    /**
//     * Adds Image Card to Linear Layout
//     * @param item
//     */
//
//    private void inflateViewForItem(Item item) {
//        //Inflate Layout
//        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
//        //Bind Data
//        Glide.with(this)
//                .asBitmap()
//                .load(item.imageUrl)
//                .into(binding.imageView);
//
//        binding.title.setText(item.label);
//        binding.title.setBackgroundColor(item.color);
//        urls.add(item.imageUrl);
//
//        //Add it to the list
//
//        b.list.addView(binding.getRoot());
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//    }

    /**
     * This method will save the item card so that when the screen is rotated the data is not lost.
     */

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String json = gson.toJson(items);
        outState.putString(Constants.ITEMS, json);
    }

    /**
     * OverRide onPause method to save shared preferences
     */
    @Override
    protected void onPause() {
        super.onPause();
        String json = gson.toJson(items);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        preferences.edit()
                .putString(Constants.ITEMS, json)
                .apply();
    }

//    /**
//     * Inflate data from shared preferences
//     */
//    private void inflateDataFromSharedPreferences() {
//        int itemCount = preferences.getInt(Constants.NUMOFIMG, 0);
//        if (itemCount != 0) b.noItemTV.setVisibility(View.GONE);
//        // Inflate all items from shared preferences
//        for (int i = 0; i < itemCount; i++) {
//
//            Item item = new Item(preferences.getString(Constants.IMAGE + i, "")
//                    , preferences.getInt(Constants.COLOR + i, 0)
//                    , preferences.getString(Constants.LABEL + i, ""));
//
//            items.add(item);
//            setUpRecyclerView();
//        }
//    }

    /**
     * Send Intent to get image from gallery
     */
    private void addFromGallery() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    /**
     * Fetch image from gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            String uri = selectedImage.toString();

            new AddFromGalleryDialog().show(this, uri, new AddFromGalleryDialog.onCompleteListener() {
                @Override
                public void onAddCompleted(Item item) {
                    items.add(item);
                    setUpRecyclerView();
                    b.noItemTV.setVisibility(View.GONE);
                }

                @Override
                public void onError(String error) {
                    new MaterialAlertDialogBuilder(GalleryActivity.this)
                            .setTitle("Error")
                            .setMessage(error)
                            .show();
                }
            });
        }
    }


}

