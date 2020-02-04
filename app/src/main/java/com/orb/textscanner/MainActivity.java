package com.orb.textscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    final static int REQUEST_CODE_PERMISSIONS = 42;

    final static String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    TextureView textureView;
    TextView label;

    int rotation=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView=findViewById(R.id.view_finder);
        label=findViewById(R.id.label);




        textureView.post(this::startCamera);

       textureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
           @Override
           public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
               updateTransform();
           }
       });



        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        //Find the center
        float centerX = (float)textureView.getWidth() / 2f;
        float centerY = textureView.getHeight() / 2f;

        //Get correct rotation
//        rotation = (textureView.getDisplay().getRotation()){
//            Surface.ROTATION_0 -> 0
//            Surface.ROTATION_90 -> 90
//            Surface.ROTATION_180 -> 180
//            Surface.ROTATION_270 -> 270
        rotation=0;
        matrix.postRotate((float)-rotation, centerX, centerY);

        textureView.setTransform(matrix);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
      if(requestCode==REQUEST_CODE_PERMISSIONS){

          if(allPermissionsGranted()){
            startCamera();
          }else{
              Toast.makeText(this, "Permission Not Granted.", Toast.LENGTH_SHORT).show();

              finish();
          }
      }
    }



    private void startCamera() {

        PreviewConfig previewConfig = new PreviewConfig.Builder().build();

        Preview preview =new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(@NonNull Preview.PreviewOutput output) {
                textureView.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });


        CameraX.bindToLifecycle( this, preview);
    }



    private boolean allPermissionsGranted(){


        for(String permission:REQUIRED_PERMISSIONS){

            if(ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }


        return true;
    }
}
