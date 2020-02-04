package com.orb.textscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TextScanActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;


    private static final int CAMERA_PHOTO = 111;
    private static final Uri CONTENT_URI = Uri.parse("");
    private Uri imageToUploadUri;

    public final int REQUEST_SELECT_PICTURE = 0x01;
    public final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public static String TEMP_PHOTO_FILE_NAME ="photo_";
    Uri mImageCaptureUri;
    File mFileTemp;




    ImageView mImageView;
    ImageButton cameraBtn;
    ImageButton detectBtn;
    Bitmap imageBitmap;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_scan);

        mImageView = findViewById(R.id.mImageView);
        cameraBtn = findViewById(R.id.cameraButton);
        detectBtn = findViewById(R.id.detectButton);
        textView = findViewById(R.id.textView);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initTempFile();
                openCameraN();
            }
        });
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectImg();
            }
        });
    }



    public void initTempFile(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory() + File.separator
                    + getResources().getString(R.string.app_foldername) + File.separator
                    + getResources().getString(R.string.pictures_folder)
                    , TEMP_PHOTO_FILE_NAME
                    + System.currentTimeMillis() + ".jpg");
            mFileTemp.getParentFile().mkdirs();
        } else {
            mFileTemp = new File(getFilesDir() + File.separator
                    + getResources().getString(R.string.app_foldername)
                    + File.separator + getResources().getString(R.string.pictures_folder)
                    , TEMP_PHOTO_FILE_NAME + System.currentTimeMillis() + ".jpg");
            mFileTemp.getParentFile().mkdirs();
        }
    }


    public void openCameraN(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            mImageCaptureUri = null;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = FileProvider.getUriForFile(TextScanActivity.this,"com.orb.textscanner.provider",mFileTemp);
            } else {
               mImageCaptureUri = CONTENT_URI;
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
        } catch (Exception e) {
            Log.d("error", "cannot take picture", e);
        }
    }

    private void detectImg() {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer textRecognizer =
                FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTxt(firebaseVisionText.getText());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    private float getRotation() {
        try {
            ExifInterface ei = new ExifInterface(mFileTemp.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90f;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180f;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270f;
                default:
                    return 0f;
            }
        } catch (Exception e) {
            Log.e("Add Recipe", "getRotation", e);
            return 0f;
        }
    }



    private void captureCameraImage() {
        Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        File f = new File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg");
        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

        //imageToUploadUri = Uri.fromFile(f);
        Uri uri = FileProvider.getUriForFile(TextScanActivity.this,"com.orb.textscanner.provider",f);

        imageToUploadUri=uri;
        startActivityForResult(chooserIntent, CAMERA_PHOTO);
    }



//    private void captureCameraImage() {
//        Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        File f = new File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg");
//        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
//
//        //imageToUploadUri = Uri.fromFile(f);
//        Uri uri = FileProvider.getUriForFile(TextScanActivity.this,"com.orb.textscanner.provider",f);
//
//        imageToUploadUri=uri;
//        startActivityForResult(chooserIntent, CAMERA_PHOTO);
//    }


    public void processTxt(String text){

        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void processRecognizedTextBlock(FirebaseVisionText text){

        StringBuilder output= new StringBuilder();
        List<FirebaseVisionText.TextBlock> blocks=text.getTextBlocks();
        if(blocks.size()==0){
            Toast.makeText(this, "No Text Recognized", Toast.LENGTH_SHORT).show();
        return;
        }

        for(int i=0;i<blocks.size();i++){
            List<FirebaseVisionText.Line> lines=blocks.get(i).getLines();
            for(int j=0;j<lines.size();j++){
                List<FirebaseVisionText.Element> elements=lines.get(j).getElements();
                for(int k=0;k<elements.size();k++){
                    output.append(elements.get(k).getText());
                }
                output.append("\n");
            }
        }

        Toast.makeText(this, output, Toast.LENGTH_SHORT).show();
    }






    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
        }


        if (requestCode == CAMERA_PHOTO && resultCode == AppCompatActivity.RESULT_OK) {
            if(imageToUploadUri != null){
                Uri selectedImage = imageToUploadUri;
                getContentResolver().notifyChange(selectedImage, null);
                Bitmap reducedSizeBitmap = getBitmap(imageToUploadUri.getPath());
                if(reducedSizeBitmap != null){
                    mImageView.setImageBitmap(reducedSizeBitmap);
                }else{
                    Toast.makeText(this,"Error while capturing Image",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this,"Error while capturing Image",Toast.LENGTH_LONG).show();
            }
        }


       if(requestCode==REQUEST_CODE_TAKE_PICTURE){
        try{
            Bitmap bitmappicture = MediaStore.Images.Media.getBitmap(getContentResolver() ,
                    mImageCaptureUri);

            Matrix matrix = new Matrix();
            matrix.postRotate(getRotation());



            imageBitmap=Bitmap.createBitmap(bitmappicture,0,0,bitmappicture.getWidth(),bitmappicture.getHeight(),matrix,false);
            mImageView.setImageBitmap(imageBitmap);

        }catch (IOException e){
            Log.v("error camera",e.getMessage());
        }
    }

        super.onActivityResult(requestCode,resultCode,data);
    }

    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " +
                    b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("", e.getMessage(), e);
            return null;
        }
    }
}
