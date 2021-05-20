package com.margsapp.imagetotext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

public class FinalText extends AppCompatActivity {

    CardView edit,savetxt;
    CardView copy,share,save;
    AppCompatEditText editText;
    String txt;
    Intent intent;

    ArrayList<String> textList;


    AppCompatButton done;
    RelativeLayout editLayout,savetxtLayout,copyLayout,shareLayout,saveLayout;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_text);


        editLayout = findViewById(R.id.editview);
        savetxtLayout = findViewById(R.id.savetxtview);
        copyLayout = findViewById(R.id.copyview);
        shareLayout = findViewById(R.id.shareview);
        saveLayout = findViewById(R.id.saveview);
        done = findViewById(R.id.done);
        copy = findViewById(R.id.copy);
        intent = getIntent();

        textList = intent.getStringArrayListExtra("text");
        edit = findViewById(R.id.edit);
        share = findViewById(R.id.share);
        save = findViewById(R.id.save);
        savetxt = findViewById(R.id.savetxt);
        savetxt.setVisibility(View.GONE);
        edit.setVisibility(View.VISIBLE);
        editText = findViewById(R.id.appCompatEditText);
        editText.setEnabled(false);


        for (int i=0; i<textList.size();i++){
            editText.append(textList.get(i));
            editText.append("\n");
        }


        edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_UP){
                    editLayout.setBackgroundColor(getResources().getColor(R.color.cardback));
                }else if (event.getAction()== MotionEvent.ACTION_DOWN){
                    editLayout.setBackgroundColor(getResources().getColor(R.color.cardbackonclick));
                }

                return false;
            }
        });

        savetxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_UP){
                    savetxtLayout.setBackgroundColor(getResources().getColor(R.color.cardback));
                }else if (event.getAction()== MotionEvent.ACTION_DOWN){
                    savetxtLayout.setBackgroundColor(getResources().getColor(R.color.cardbackonclick));
                }

                return false;
            }
        });

        copy.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_UP){
                    copyLayout.setBackgroundColor(getResources().getColor(R.color.cardback));
                }else if (event.getAction()== MotionEvent.ACTION_DOWN){
                    copyLayout.setBackgroundColor(getResources().getColor(R.color.cardbackonclick));
                }

                return false;
            }
        });

        share.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_UP){
                    shareLayout.setBackgroundColor(getResources().getColor(R.color.cardback));
                }else if (event.getAction()== MotionEvent.ACTION_DOWN){
                    shareLayout.setBackgroundColor(getResources().getColor(R.color.cardbackonclick));
                }

                return false;
            }
        });

        save.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_UP){
                    saveLayout.setBackgroundColor(getResources().getColor(R.color.cardback));
                }else if (event.getAction()== MotionEvent.ACTION_DOWN){
                    saveLayout.setBackgroundColor(getResources().getColor(R.color.cardbackonclick));
                }

                return false;
            }
        });



        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FinalText.this,MainActivity.class));
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt = editText.getText().toString();
                if(!txt.equals("")){
                    setClipboard(getApplicationContext(),txt);
                }else {
                    Toast.makeText(getApplicationContext(),"Our AI did not find any text",Toast.LENGTH_SHORT).show();
                }

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt = editText.getText().toString();
                if(!txt.equals("")){
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                    String sAux = txt;

                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "choose one"));
                }else {
                    Toast.makeText(getApplicationContext(),"Our AI did not find any text",Toast.LENGTH_SHORT).show();
                }

            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setEnabled(true);

                edit.setVisibility(View.GONE);
                savetxt.setVisibility(View.VISIBLE);
            }
        });

        savetxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setEnabled(false);
                savetxt.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt = editText.getText().toString();
                Toast.makeText(getApplicationContext(),"Feature not available yet.",Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context,"Text has been copied to clipboard",Toast.LENGTH_SHORT).show();
    }

    public void onBackPressed(){

        startActivity(new Intent(FinalText.this,MainActivity.class));
    }
}