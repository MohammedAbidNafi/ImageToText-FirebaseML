package com.margsapp.imagetotext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
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

import java.util.List;

public class CameraActivity extends AppCompatActivity {


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private ImageView imageView;
    private Bitmap imageBitmap;
    AppCompatButton extract;
    Animation scaleup,scaledown;
    TextView textView;
    ProgressBar loader;

    String txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.image);
        loader = findViewById(R.id.loader);
        textView = findViewById(R.id.textView);
        extract = findViewById(R.id.extract);
        extract.setVisibility(View.VISIBLE);
        scaleup = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up);
        scaledown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);

        extract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                remove();

            }
        });


        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

            openCamera();

        }else {
            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
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

            startActivity(new Intent(CameraActivity.this,FinalText.class).putExtra("txt",txt));
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getApplicationContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void openCamera() {
        Intent takepictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takepictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takepictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
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