package com.margsapp.imagetotext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.util.Pair;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class FinalCameraActivity extends AppCompatActivity {

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

    private ImageView imageView;

    private static final String IMAGE_DIRECTORY = "/ImageToText";


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
        setContentView(R.layout.activity_final_camera);
        imageBitmap = CameraViewActivity.bitmap;

        imageView = findViewById(R.id.img);

        gettext = findViewById(R.id.gettext);
        mGraphicOverlay = findViewById(R.id.graphic_overlay);
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


        new Handler().postDelayed(this::setImage, 160);




    }


    private void setImage(){


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
            loading2_0_1();

        }, 2600);

    }

    private void loading2_0_1(){

        new Handler().postDelayed(() -> {

            loading2_1();
            recognizeText();
        }, 500);

    }

    private void loading2_1() {

        new Handler().postDelayed(() -> {

            textView.setText("Getting Text");
            loader.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);

            extract.setVisibility(View.VISIBLE);

        }, 2600);

    }



    private void loading3() {
        textView.setText("Extracting Text");
        new Handler().postDelayed(() -> {
            saveImage(CameraViewActivity.bitmap);
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

            startActivity(new Intent(FinalCameraActivity.this,FinalText.class).putStringArrayListExtra("text",textlist));
        }, 1000);
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
            extract.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),"No text found",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(FinalCameraActivity.this,MainActivity.class));

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

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists()) {
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs());
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Toast.makeText(getApplicationContext(),"Image Saved",Toast.LENGTH_SHORT).show();
        return "";

    }
}