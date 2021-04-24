package com.margsapp.imagetotext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class CameraActivity extends AppCompatActivity {


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private ImageView imageView;
    private Bitmap imageBitmap;
    AppCompatButton extract,gettext;
    Animation scaleup,scaledown;
    TextView textView;
    ProgressBar loader;
    private final ArrayList<String> textlist = new ArrayList<>();



    private GraphicOverlay mGraphicOverlay;
    private static final int RESULTS_TO_SHOW = 3;

    private Integer mImageMaxWidth;

    private Integer mImageMaxHeight;


    private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float>
                                o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        gettext = findViewById(R.id.gettext);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        imageView = findViewById(R.id.image);
        loader = findViewById(R.id.loader);
        textView = findViewById(R.id.textView);
        extract = findViewById(R.id.extract);
        extract.setVisibility(View.GONE);

        gettext.setVisibility(View.VISIBLE);
        gettext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preloading();

            }
        });
        scaleup = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_up);
        scaledown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down);

        extract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            loading3();
        }, 160);
    }

    private void preloading(){
        gettext.setAnimation(scaleup);
        new Handler().postDelayed(() -> {

            gettext.setVisibility(View.GONE);
            loader.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            loading();
        }, 160);
    }

    private void loading() {
        textView.setText("Deconstructing image");
        new Handler().postDelayed(this::loading2, 1600);

    }

    private void loading2() {
        new Handler().postDelayed(() -> {
            textView.setText("Analysing Image");
            loading2_1();
        }, 2600);

    }

    private void loading2_1() {

        new Handler().postDelayed(() -> {
            recognizeText();
            textView.setText("Getting Text");
            loader.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);

            extract.setVisibility(View.VISIBLE);

        }, 2600);

    }



    private void loading3() {
        textView.setText("Extracting Text");
        new Handler().postDelayed(() -> {



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

            textView.setText("Done");

            newActivityLoader();
        }, 1000);
    }

    private void newActivityLoader() {
        new Handler().postDelayed(() -> {

            startActivity(new Intent(CameraActivity.this,FinalText.class).putStringArrayListExtra("text",textlist));
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

    @SuppressLint("QueryPermissionsNeeded")
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
            assert data != null;
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) imageBitmap.getWidth() / (float) targetWidth,
                            (float) imageBitmap.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            imageBitmap,
                            (int) (imageBitmap.getWidth() / scaleFactor),
                            (int) (imageBitmap.getHeight() / scaleFactor),
                            true);

            imageView.setImageBitmap(resizedBitmap);
            imageBitmap = resizedBitmap;


        }
    }


    private void recognizeText() {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
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


        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);

                }
            }
        }



        for(Text.TextBlock textBlock : visionText.getTextBlocks()){
            textlist.add(textBlock.getText());
        }
    }


    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = imageView.getWidth();
        }

        return mImageMaxWidth;
    }

    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight =
                    imageView.getHeight();
        }

        return mImageMaxHeight;
    }

    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }
}