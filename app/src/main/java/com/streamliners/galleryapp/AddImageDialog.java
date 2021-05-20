package com.streamliners.galleryapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ChipLabelBinding;
import com.streamliners.galleryapp.databinding.DialogAddImageBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.List;
import java.util.Set;

public class AddImageDialog implements ItemHelper.OnCompleteListener {

    private Context context;
    private OnCompleteListener listener;
    private DialogAddImageBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private Bitmap image;
    private AlertDialog dialog;
    private String imageUrl;

    /**
     * Inflate Dialogs Layout
     * @param context
     * @param listener
     */

    void show(Context context, OnCompleteListener listener){
        this.context = context;
        this.listener = listener;


        //Inflate dialogs layout
        if(context instanceof GalleryActivity){
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogAddImageBinding.inflate(inflater);
        } else{
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }

        //Create & show dialog
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .show();

        //Handle events
        handleDimensionsInput();
        hideErrorForET();
    }

    //utility

    /**
     * Hide Error of text field on text change
     */

    private void hideErrorForET() {
        b.width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.width.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //Step 1 : Input dimensions

    /**
     * Takes Input of Height and Width and fetches image
     */

    private void handleDimensionsInput() {
        b.fetchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get strings from edit text
                String widthStr = b.width.getText().toString().trim()
                        , heightStr = b.height.getText().toString().trim();

                //Guard code
                if(widthStr.isEmpty() && heightStr.isEmpty()){
                    b.width.setError("Please enter at least one dimension!");
                    return;
                }

                //Update UI
                b.inputDimensionsRoot.setVisibility(View.GONE);
                b.progressIndicatorRoot.setVisibility(View.VISIBLE);
                hideKeyboard();

                //Square image
                if(widthStr.isEmpty()){
                    int height = Integer.parseInt(heightStr);
                    fetchRandomImage(height);
                }else if(heightStr.isEmpty()){
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width);
                }

                //Rectangular image
                else{
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width, height);
                }
            }
        });
    }

    /**
     * Hide Keyboard after taking input of width and height
     */

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(b.widthInput.getWindowToken(), 0);
    }

    //Step 2 : Fetch Random Image

    //Rectangular image

    /**
     * Fetch Rectangular Image
     * @param width
     * @param height
     */
    private void fetchRandomImage(int width, int height) {
        new ItemHelper()
                .fetchData(width, height, context, this);

    }

    //Square image

    /**
     * Fetch Square Image
     * @param x
     */
    private void fetchRandomImage(int x) {
        new ItemHelper()
                .fetchData(x, context, this);
    }

    //Step 3 : Show data

    /**
     * Show Data on Main Root to Add Image to Gallery
     * @param url
     * @param colors
     * @param labels
     */

    private void showData(String url, Set<Integer> colors, List<String> labels) {
        this.imageUrl = url;
        inflateColorChips(colors);
        inflateLabelChips(labels);
        handleCustomLabelInput();
        handleAddImageEvent();
        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(b.imageView);

        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelInput.setVisibility(View.GONE);
    }

    /**
     * Takes Custom Label Input
     */

    private void handleCustomLabelInput() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.labelChips.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                b.customLabelInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;
            }
        });
    }

    //Label Chips

    /**
     * Inflates label chips to main root
     * @param labels
     */
    private void inflateLabelChips(List<String> labels) {
        for(String label : labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.labelChips.addView(binding.getRoot());
        }
    }

    //Color Chips

    /**
     * inflates color chips to main root
     * @param colors
     */
    private void inflateColorChips(Set<Integer> colors) {
        for(int color : colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colorChips.addView(binding.getRoot());
        }
    }

    /**
     * Handle actions when user clicks on add image button
     */

    private void handleAddImageEvent() {
        b.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.colorChips.getCheckedChipId()
                        , labelChipId = b.labelChips.getCheckedChipId();

                //Guard code
                if(colorChipId == -1 || labelChipId == -1){
                    Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
                    return;
                }
                String label;

                //Get color & label
                if(isCustomLabel){
                    label = b.customLabelEt.getText().toString().trim();
                    if(label.isEmpty()){
                        Toast.makeText(context, "Please enter custom label", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else{
                    label = ((Chip) b.labelChips.findViewById(labelChipId))
                            .getText().toString();
                }
                int color = ((Chip) b.colorChips.findViewById(colorChipId))
                        .getChipBackgroundColor().getDefaultColor();

                //Send callback
                listener.onImageAdded(new Item(imageUrl, color, label));
                dialog.dismiss();
            }
        });
    }

    //ItemHelper callbacks

    @Override
    public void onFetched(String url, Set<Integer> colors, List<String> labels) {
        showData(url, colors, labels);
    }

    @Override
    public void onError(String error) {
        dialog.dismiss();
        listener.onError(error);

    }

    /**
     * callbacks for add image completion
     */

    interface OnCompleteListener{
        void onImageAdded(Item item);
        void onError(String error);
    }

}
