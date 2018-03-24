package com.example.frantisekff.appqr_string;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

/**
 * Created by frantisek.ff on 15. 11. 2017.
 */

public class ScanBarcodeActivity extends Activity {
    final int REQUEST_CAMERA = 0;
    SurfaceView cameraView;
    BarcodeDetector barcode;
    CameraSource cameraSource;
    SurfaceHolder holder;
    TextView textResult;
    Button torch;



    private Camera camera = null;
    boolean flashmode=false;
 //   private GraphicOverlay mGraphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);



        Button resetBtn= (Button) findViewById(R.id.reset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textResult.setText("");


            }
        });

        torch = (Button) findViewById(R.id.flash);
        final boolean hasCameraFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        cameraView = (SurfaceView) findViewById(R.id.camera_preview);
        cameraView.setZOrderMediaOverlay(true);
        holder = cameraView.getHolder();

   //   mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        barcode = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();


      /*  BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay);


        barcode.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());
*/



        //vyska a sirka obrazovky
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;


        //či je detektor pripraveny a (napr. či su stiahnute všetky potrebne subory, ktore sa mali
        if (!barcode.isOperational()) {
            Toast.makeText(getApplicationContext(), "Sorry, Couldn't setup the detector", Toast.LENGTH_LONG).show();
            this.finish();
        }
        //CameraSource.Builder:  Builder for configuring and creating an associated camera source.
        cameraSource = new CameraSource.Builder(this, barcode)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(width, height)
                .build();




        torch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasCameraFlash) {
                        flashOnButton();
                } else {
                    Toast.makeText(ScanBarcodeActivity.this, "No flash available on your device",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


     //   Toast.makeText(getApplicationContext(), barcode.toString() , Toast.LENGTH_LONG).show();


        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {


                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                //ak nie je povoleny fotoaparat tak poziadam o povolenie
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(getApplicationContext(), "Camera permissions is needed to show camera preview", Toast.LENGTH_SHORT).show();
                    }

                    // po vykonani requestPermision sa v funkcii onRequestPermissionsResult skontroluje premenna REQUEST_CAMERA
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                }
            }


            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        barcode.setProcessor(new Detector.Processor<Barcode>() {

            // release() - Shuts down and releases associated processor resources.
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                textResult = findViewById(R.id.result);

                //detections.getDetectedItems() - Returns a collection of the detected items that were identified in the frame.
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() > 0)
                    textResult.post(new Runnable() {

                        @Override
                        public void run() {
                       
                            //zapnutie scrollovania
                            textResult.setMovementMethod(new ScrollingMovementMethod());

                            //nacitanie dat z Qr codu do stringu
                            String inputFromCode = barcodes.valueAt(0).rawValue.toString();


                            System.out.print("test");
                            try {
                                Data data = fillData(barcodes);
                                Map<String, Object>  get_header = (Map<String, Object>) data.getResponse().get("header");
                                   Map<String, Object>  get_content = (Map<String, Object>) data.getResponse().get("content");
                                   String url = (String) get_content.get("url");

                                int is_url_set = (int) get_header.get("url");

                                if(is_url_set == 1 ){
                                    if (isValidURL(url)) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        startActivity(browserIntent);
                                    }
                                }

                            }catch (Exception e){}



                            textResult.setText(inputFromCode);
                            cameraSource.release();


                            // Toast.makeText(getApplicationContext(), inputFromCode , Toast.LENGTH_LONG).show();

                        }
                    });


            }
        });


    }

    public Data fillData( SparseArray<Barcode> barcodes){


        ObjectMapper objectMapper = new ObjectMapper();
    //    Data response = null;
        Map<String, Object> response = null;
        try {
           response = new ObjectMapper().readValue(barcodes.valueAt(0).rawValue.toString(), new TypeReference< Map<String, Object> >(){});


//            response = new ObjectMapper().readValue(barcodes.valueAt(0).rawValue.toString(), Map.class);

           // Response respon = new ObjectMapper().readValue(barcodes.valueAt(0).rawValue.toString(),Response.class);
            //response = new ObjectMapper().readValue(barcodes.valueAt(0).rawValue.toString(), Map.class);
           // String jsonInString = "{\"age\":33,\"messages\":[\"msg 1\",\"msg 2\"],\"name\":\"mkyong\"}";
           // User user1 = new ObjectMapper().readValue(jsonInString, User.class);

           // final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
            //final MyPojo pojo = mapper.convertValue(response, MyPojo.class);

          //  Class<?> clazz = Class.forName(className);
          //  Constructor<?> ctor = clazz.getConstructor(String.class);
          //  Object object = ctor.newInstance(new Object[] { ctorArgument });


        } catch (IOException e) {
            e.printStackTrace();
           // Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }

        Point[] points = new Point[4];
        for (int i = 0; i < 4; i++) {
            int x = barcodes.valueAt(0).cornerPoints[i].x;
            int y = barcodes.valueAt(0).cornerPoints[i].y;
            points[i] = new Point(x, y);

        }
        int num_of_chars =  barcodes.valueAt(0).rawValue.length();

        Data data = new Data(points, num_of_chars, response);

            return data;
        }

        public JSONObject stringToJSON(String input){
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(input);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObj;
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                try {
                    cameraSource.start(cameraView.getHolder());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Permission was not granted", Toast.LENGTH_SHORT).show();
            }


        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }

    }

    private void flashOnButton() {
        camera=getCamera(cameraSource);
        if (camera != null) {
            try {
                Camera.Parameters param = camera.getParameters();
                param.setFlashMode(!flashmode?Camera.Parameters.FLASH_MODE_TORCH :Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(param);
                flashmode = !flashmode;
                if(flashmode){
                //    showToast("Flash Switched ON");
                    Toast.makeText(this, "Flash Switched On", Toast.LENGTH_SHORT).show();
                }
                else {
                   // showToast("Flash Switched Off");

                    Toast.makeText(this, "Flash Switched Off", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }
    public static boolean isValidURL(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception exception)
        {
            return false;
        }
    }



}


/* Example of JSON
* {"widget": {
    "debug": "on",
    "window": {
        "title": "Sample Konfabulator Widget",
        "name": "main_window",
        "width": 500,
        "height": 500
    },
    "image": {
        "src": "Images/Sun.png",
        "name": "sun1",
        "hOffset": 250,
        "vOffset": 250,
        "alignment": "center"
    },
    "text": {
        "data": "Click Here",
        "size": 36,
        "style": "bold",
        "name": "text1",
        "hOffset": 250,
        "vOffset": 100,
        "alignment": "center",
        "onMouseUp": "sun1.opacity = (sun1.opacity / 100) * 90;"
    }
}}
*
*
* */