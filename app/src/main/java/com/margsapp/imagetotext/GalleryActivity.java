package com.margsapp.imagetotext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    ImageView imageView;

    ProgressBar loader;

    TextView textView;

    AppCompatButton extract;

    String txt;
    static final int GALLERY_PICK = 1;
    private Bitmap imageBitmap;

    Animation scaleup,scaledown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        imageView = findViewById(R.id.image);
        loader = findViewById(R.id.loader);
        textView = findViewById(R.id.textView);
        extract = findViewById(R.id.extract);
        extract.setVisibility(View.VISIBLE);
        scaleup = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up);
        scaledown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);


        openGallery();

        extract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                remove();

            }
        });


    }

    private void remove(){
        extract.startAnimation(scaleup);
        // extract.startAnimation(scaledown);
        new Handler().postDelayed(() -> {

            loader.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            extract.setVisibility(View.GONE);
            loading();
        }, 160);
    }

    private void loading() {
        new Handler().postDelayed(() -> {

            textView.setText("Deconstructing image");

            loading2();
        }, 1600);

    }

    private void loading2() {
        new Handler().postDelayed(() -> {

            textView.setText("Analysing Image");

            loading3();
        }, 2600);

    }

    private void loading3() {

        new Handler().postDelayed(() -> {

            textView.setText("Extracting Text");

            loading4();
        }, 3600);
    }

    private void loading4() {

        new Handler().postDelayed(() -> {

            textView.setText("Finishing up");

            loading5();
        }, 3000);
    }

    private void loading5() {
        new Handler().postDelayed(() -> {
            InputImage image = InputImage.fromBitmap(imageBitmap, 0);
            recognizeText(image);
            textView.setText("Done");

            newActivityLoader();
        }, 1000);
    }

    private void newActivityLoader() {
        new Handler().postDelayed(() -> {

            startActivity(new Intent(GalleryActivity.this,FinalText.class).putExtra("txt",txt));
        }, 1000);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(imageBitmap);
        }
    }


    private void recognizeText(InputImage image) {

        // [START get_detector_default]
        TextRecognizer recognizer = TextRecognition.getClient();
        // [END get_detector_default]

        // [START run_detector]
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                processTextRecognitionResult(visionText);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
        // [END run_detector]
    }

    private void processTextRecognitionResult(Text visionText) {
        List<Text.TextBlock> blocks = visionText.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(getApplicationContext(),"No text found",Toast.LENGTH_SHORT).show();
            return;
        }

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            txt = block.getText();

        }
    }
}