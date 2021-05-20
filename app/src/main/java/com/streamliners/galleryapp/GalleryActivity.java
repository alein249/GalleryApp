package com.streamliners.galleryapp;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.List;


public class GalleryActivity extends AppCompatActivity {

    ActivityGalleryBinding b;
    SharedPreferences preferences;
    List<Item> items = new ArrayList<>();
    private boolean isDialogBoxShowed = false;

    ItemCardBinding bindingToRemove;
    private List<String> urls = new ArrayList<>();
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (!items.isEmpty())
            b.noItemTV.setVisibility(View.GONE);

        preferences = getPreferences(MODE_PRIVATE);
        inflateDataFromSharedPreferences();
    }

    //Actions menu method

    /**
     * Gives Add Image Option in menu
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    /**
     * Shows add image dialog on clicking icon in menu
     * @param item
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_image){
            showAddImageDialog();
            return true;
        }
        return false;
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
                        inflateViewForItem(item);
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
     * Adds Image Card to Linear Layout
     * @param item
     */
    private void inflateViewAt(Item item,int index) {

        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
        //Bind Data
        Glide.with(this)
                .asBitmap()
                .load(item.imageUrl)
                .into(binding.imageView);

        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //Add it to the list

        b.list.addView(binding.getRoot(),index);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * Adds Image Card to Linear Layout
     * @param item
     */

    private void inflateViewForItem(Item item) {
        //Inflate Layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());
        //Bind Data
        Glide.with(this)
                .asBitmap()
                .load(item.imageUrl)
                .into(binding.imageView);

        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);
        urls.add(item.imageUrl);

        //Add it to the list

        b.list.addView(binding.getRoot());
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * OverRide onPause method to save shared preferences
     */
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor myEdit = preferences.edit();

        int numOfImg = items.size();
        myEdit.putInt(Constants.NUMOFIMG, numOfImg).apply();

        int counter = 0;
        for (Item item : items){
            myEdit.putInt(Constants.COLOR + counter, item.color)
                    .putString(Constants.LABEL + counter, item.label)
                    .putString(Constants.IMAGE + counter, urls.get(counter))
                    .apply();
            counter++;
        }
        myEdit.commit();
    }

    /**
     * Inflate data from shared preferences
     */
    private void inflateDataFromSharedPreferences(){
        int itemCount = preferences.getInt(Constants.NUMOFIMG,0);
        if (itemCount!=0) b.noItemTV.setVisibility(View.GONE);
        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++){

            Item item = new Item(preferences.getString(Constants.IMAGE + i,"")
                    ,preferences.getInt(Constants.COLOR + i,0)
                    ,preferences.getString(Constants.LABEL + i,""));

            items.add(item);
            inflateViewForItem(item);
        }
    }


}

