package com.orb.textscanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TableExtractActivity extends AppCompatActivity {


    public static String TEMP_PHOTO_FILE_NAME = "photo_";
    public static String TEMP_EXCEL_FILE_NAME = "output_";


    private static final int REQUEST_CODE_TAKE_PICTURE = 9991;

    private static String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private final static int REQUEST_PERMISSION_CAMERA = 10002;
    private final static int REQUEST_PERMISSION_STORAGE = 10003;
    private static int[] permission_request_code = {REQUEST_PERMISSION_CAMERA, REQUEST_PERMISSION_STORAGE};


    ImageView imageView;
    ImageButton captureButton;
    ImageButton convertButton;
    ImageButton aboutButton;
    TextView resultTextView;


    Bitmap imageBitmap;

    String imgDir;
    String imgFileName;

    String excelDir = "";
    String excelFileName = "";


    Uri mImageCaptureUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_extract);

        imageView = findViewById(R.id.mImageView);
        captureButton = findViewById(R.id.scanButton);
        convertButton = findViewById(R.id.convertButton);
        aboutButton = findViewById(R.id.aboutButton);
        resultTextView = findViewById(R.id.resultTextView);


        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasPermissions(permissions)) {
                    openCamera();

                } else {
                    requestPermissions(permissions);
                }

            }
        });


        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                convertImageToText();
            }
        });


        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TableExtractActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });


    }


    private void convertImageToText() {
        if (imageBitmap != null) {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionTextRecognizer textRecognizer =
                    FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    generateExcelFile(firebaseVisionText);
                    processTxt(firebaseVisionText.getText());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        } else {
            Toast.makeText(this, "No Photo Captured.", Toast.LENGTH_SHORT).show();
        }

    }

    private void processTxt(String string) {
        Intent intent = new Intent(TableExtractActivity.this, ResultActivity.class);
        intent.putExtra("result", string);
        intent.putExtra("exceldir", excelDir);
        intent.putExtra("excelfilename", excelFileName);
        startActivity(intent);
    }


    private void generateExcelFile(FirebaseVisionText firebaseVisionText) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("MarkSheet");

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(this, "No Text Recognized", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            Row myRow=sheet.createRow(i);
            for (int j = 0; j < lines.size(); j++) {
                String cell = "";
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) { ;
                    cell += elements.get(k).getText();
                }
                Cell myCell=myRow.createCell(j);
                myCell.setCellValue(cell);
            }
        }










//        for(FirebaseVisionText.TextBlock block:blocks){
//            Row myRow=sheet.createRow(row);

//            List<FirebaseVisionText.Line> lines=block.getLines();
//            for(FirebaseVisionText.Line line:lines){
//                List<FirebaseVisionText.Element> elements=line.getElements();
//
//
//                for(FirebaseVisionText.Element element:elements){
//
//                    myRow = sheet.getRow();
//                if (myRow == null) {
//                    myRow = sheet.createRow(rowNum);
//                    myCell = myRow.getCell(columnNum);
//                    if (myCell == null)
//                        myRow.createCell(columnNum);
//                }
//                if (myCell != null) {
//                    myCell.setCellValue(v);
//                }
//
//                }
//            }
//        }



        File excelFile = new File(excelDir, excelFileName);

        try {
            FileOutputStream fileOut = new FileOutputStream(excelFile);
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



//        Row myRow;
//        Cell myCell = null;
//
//        String v = "";
//
//        StringBuilder output = new StringBuilder();
//        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
//        if (blocks.size() == 0) {
//            Toast.makeText(this, "No Text Recognized", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        for (int columnNum = 0; columnNum < blocks.size(); columnNum++) {
//            List<FirebaseVisionText.Line> lines = blocks.get(columnNum).getLines();
//            for (int rowNum = 0; rowNum < lines.size(); rowNum++) {
//                List<FirebaseVisionText.Element> elements = lines.get(rowNum).getElements();
//                v = "";
//                for (int k = 0; k < elements.size(); k++) {
//                    //output.append(elements.get(k).getText());
//                    v += " " + elements.get(k).getText();
//
//
//                }
//                myRow = sheet.getRow(rowNum);
//                if (myRow == null) {
//                    myRow = sheet.createRow(rowNum);
//                    myCell = myRow.getCell(columnNum);
//                    if (myCell == null)
//                        myRow.createCell(columnNum);
//                }
//                if (myCell != null) {
//                    myCell.setCellValue(v);
//                }
//                //output.append("\n");
//            }
//        }



    }

    public void processRecognizedTextBlock(FirebaseVisionText text) {

        StringBuilder output = new StringBuilder();
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(this, "No Text Recognized", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    output.append(elements.get(k).getText());
                }
                output.append("\n");
            }
        }

        Toast.makeText(this, output, Toast.LENGTH_SHORT).show();
    }


    public File initTempFile() {
        File mFileTemp;
        String state = Environment.getExternalStorageState();

        String current_millis = String.valueOf(System.currentTimeMillis());

        imgFileName = TEMP_PHOTO_FILE_NAME
                + current_millis + ".jpg";
        excelFileName = TEMP_EXCEL_FILE_NAME + current_millis + ".xls";


        if (Environment.MEDIA_MOUNTED.equals(state)) {

            imgDir = Environment.getExternalStorageDirectory() + File.separator
                    + getResources().getString(R.string.app_foldername) + File.separator
                    + getResources().getString(R.string.pictures_folder);

            excelDir = Environment.getExternalStorageDirectory() + File.separator
                    + getResources().getString(R.string.app_foldername) + File.separator
                    + getResources().getString(R.string.excel_folder);

            mFileTemp = new File(imgDir
                    , imgFileName);
            mFileTemp.getParentFile().mkdirs();
            File xlsFile = new File(excelDir, excelFileName);
            xlsFile.getParentFile().mkdirs();

        } else {
            imgDir = getFilesDir() + File.separator
                    + getResources().getString(R.string.app_foldername)
                    + File.separator + getResources().getString(R.string.pictures_folder);

            excelDir = getFilesDir() + File.separator
                    + getResources().getString(R.string.app_foldername)
                    + File.separator + getResources().getString(R.string.excel_folder);


            mFileTemp = new File(imgDir
                    , imgFileName);

            File xlsFile = new File(excelDir, excelFileName);
            xlsFile.getParentFile().mkdirs();


            mFileTemp.getParentFile().mkdirs();
        }
        return mFileTemp;

    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            mImageCaptureUri = null;
            mImageCaptureUri = FileProvider.getUriForFile(TableExtractActivity.this,
                    "com.orb.textscanner.provider", initTempFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
        } catch (Exception e) {
            Log.d("error", "cannot take picture", e);
        }
    }


    private float getRotation() {
        try {
            ExifInterface ei = new ExifInterface(new File(imgDir, imgFileName).getPath());
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


    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }


    private void requestPermissions(String[] permissions) {

        int permission_no = 0;
        for (String permission : permissions) {

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(TableExtractActivity.this,
                        permission)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(TableExtractActivity.this,
                            new String[]{permission}, permission_request_code[permission_no]);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else {
                Log.d("info", "All permission has already been granted");
                // Permission has already been granted
            }

            permission_no += 1;
        }


    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(TableExtractActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(TableExtractActivity.this,
                    new String[]{permission},
                    requestCode);
        } else {
            Toast.makeText(TableExtractActivity.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                        mImageCaptureUri);

                Matrix matrix = new Matrix();
                matrix.postRotate(getRotation());
                imageBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                imageView.setImageBitmap(imageBitmap);

            } catch (IOException e) {
                Log.v("error camera", e.getMessage());
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(TableExtractActivity.this,
                        "Camera Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(TableExtractActivity.this,
                        "Camera Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(TableExtractActivity.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(TableExtractActivity.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
