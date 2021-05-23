package com.streamliners.galleryapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ChipLabelBinding;
import com.streamliners.galleryapp.databinding.DialogFromGalleryBinding;
import com.streamliners.galleryapp.models.Item;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddFromGalleryDialog {
    private Context context;
    private AddFromGalleryDialog.onCompleteListener listener;
    private DialogFromGalleryBinding b;
    private LayoutInflater inflater;

    private boolean isCustomLabel;
    public Bitmap bitmap;
    private AlertDialog dialog;
    Set<Integer> colors;
    private String imageUrl;


    /**
     * Show Edit Image Dialog Box
     * @param context
     * @param imageUrl
     * @param listener
     */
    public void show(Context context,String imageUrl, AddFromGalleryDialog.onCompleteListener listener){
        this.listener = listener;
        this.imageUrl = imageUrl;
        this.context = context;
        if (context instanceof GalleryActivity){
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogFromGalleryBinding.inflate(inflater);
        }
        else {
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }


        //Create and Show Dialog
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .show();

        fetchImage(imageUrl);

        handleShareImageEvent();

        addButtonHandle();
    }

    private void extractLabels() {
        InputImage inputImage = InputImage.fromBitmap(bitmap,0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> imageLabels) {
                        List<String> strings = new ArrayList<>();
                        for (ImageLabel label : imageLabels){
                            strings.add(label.getText());
                        }
                        inflateColorChips(colors);
                        inflateLabelChips(strings);
                        b.gImageView.setImageBitmap(bitmap);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        listener.onError(e.toString());
                    }
                });
    }

    private void extractPaletteFromBitmap() {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                colors = getColorsFromPalette(palette);
                extractLabels();
            }
        });
    }

    private void addButtonHandle() {
        b.gImageView.setImageBitmap(bitmap);
        b.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.gColorChips.getCheckedChipId(),
                        labelChipId = b.gLabelChips.getCheckedChipId();

                if (colorChipId == -1 || labelChipId == -1) {
                    Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
                    return;
                }
                String label;
                if (isCustomLabel){
                    label = b.edCustomLabelEt.getText().toString().trim();
                    if (label.isEmpty()){
                        Toast.makeText(context,"Please enter custom label",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else {
                    label = ((Chip) b.gLabelChips.findViewById(labelChipId)).getText().toString();
                }
                int color = ((Chip) b.gColorChips.findViewById(colorChipId)).getChipBackgroundColor().getDefaultColor();

                //Send Callback
                Item item = new Item(imageUrl, color, label);
                listener.onAddCompleted(item);
                dialog.dismiss();
            }
        });
    }

    private Set<Integer> getColorsFromPalette (Palette palette) {
        Set<Integer> colors = new HashSet<>();
        colors.add(palette.getVibrantColor(0));
        colors.add(palette.getLightVibrantColor(0));
        colors.add(palette.getDarkVibrantColor(0));

        colors.add(palette.getMutedColor(0));
        colors.add(palette.getLightMutedColor(0));
        colors.add(palette.getDarkMutedColor(0));

        colors.add(palette.getVibrantColor(0));
        colors.remove(0);

        return colors;
    }

    /**
     * inflates color chips to main root
     */
    private void inflateColorChips(Set<Integer> colors){
        //Inflate color chips to edit dialog
        for (int color: colors){
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.gColorChips.addView(binding.getRoot());
        }

    }


    /**
     * Inflates label chips to main root
     */
    private void inflateLabelChips(List<String> labels){


        //Inflate label chips to edit dialog
        for (String label: labels){
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.gLabelChips.addView(binding.getRoot());
        }
        handleCustomLabelInput();
    }

    void fetchImage(String url) {

        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {

                    }
                });
    }

    /**
     * Takes Custom Label Input
     */
    private void handleCustomLabelInput(){
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.gLabelChips.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                b.gCustomLabelInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;
            }
        });
    }

    /**
     * This function will share the image with another apps.
     */
    private void handleShareImageEvent() {

        b.shareImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Glide.with(context)
                            .asBitmap()
                            .load(imageUrl)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    // Calling the intent to share the bitmap
                                    Bitmap icon = resource;
                                    Intent share = new Intent(Intent.ACTION_SEND);
                                    share.setType("image/jpeg");

                                    ContentValues values = new ContentValues();
                                    values.put(MediaStore.Images.Media.TITLE, "title");
                                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                    Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            values);


                                    OutputStream outputStream;
                                    try {
                                        outputStream = context.getContentResolver().openOutputStream(uri);
                                        icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                        outputStream.close();
                                    } catch (Exception e) {
                                        System.err.println(e.toString());
                                    }

                                    share.putExtra(Intent.EXTRA_STREAM, uri);
                                    context.startActivity(Intent.createChooser(share, "Share Image"));
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });

                } catch (Exception e) {
                    Log.e("Error on sharing", e + " ");
                    Toast.makeText(context, "App not Installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * callbacks for edit completion
     */
    interface onCompleteListener{
        void onAddCompleted(Item item);
        void onError(String error);
    }
}
