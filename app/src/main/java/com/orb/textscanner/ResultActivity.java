package com.orb.textscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ResultActivity extends AppCompatActivity {

    Button excelViewButton;

    String excelDir="";
    String excelFileName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        TextView resultTextView=findViewById(R.id.resultTextView);
        excelViewButton=findViewById(R.id.excelViewButton);



        String resultData="";




        Intent intent=getIntent();
        if(intent.hasExtra("result")){
            resultData=intent.getStringExtra("result");
            //Toast.makeText(this, resultData, Toast.LENGTH_SHORT).show();
        }

        if(intent.hasExtra("exceldir")){
            excelDir=intent.getStringExtra("exceldir");
        }

        if(intent.hasExtra("excelfilename")){
            excelFileName=intent.getStringExtra("excelfilename");

        }

        resultTextView.setText(resultData);



        excelViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // openFileWithDefaultApp();
                viewFile();
            }
        });

    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }


    private void openFileWithDefaultApp(){
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        Intent newIntent = new Intent(Intent.ACTION_VIEW);

        File file=new File(excelDir,excelFileName);
        String mimeType = myMime.getMimeTypeFromExtension(fileExt(file.getPath()).substring(1));
        newIntent.setDataAndType(Uri.fromFile(file),mimeType);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            this.startActivity(newIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No handler for this type of file.", Toast.LENGTH_LONG).show();
        }
    }

    private void viewFile(){
        Intent myIntent = new Intent(Intent.ACTION_VIEW);
        myIntent.setData(FileProvider.getUriForFile(this,"com.orb.textscanner.provider",new File(excelDir,excelFileName)));
        Intent j = Intent.createChooser(myIntent, "Choose an application to open with:");
        startActivity(j);
    }
}
