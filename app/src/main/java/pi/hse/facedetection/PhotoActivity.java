package pi.hse.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class PhotoActivity extends Activity implements SurfaceHolder.Callback, Camera.AutoFocusCallback {
    //gui
    SurfaceView sv;
    private FaceOverlayView mFaceView;
    Button confirmBtn;
    Button takepicBtn;
    Camera camera;
    TextView title_field;
    SurfaceHolder holder;
    static boolean isLogged;
    static boolean isRegOk;

    //for image uploading
    InputStream inputStream;
    Bitmap bitmap;
    ByteArrayOutputStream stream;
    byte[] byte_arr;
    String image_str;
    ArrayList<NameValuePair> nameValuePairs;
    File photo1, photo2, photo3, photoToCheck;
    File[] arr;

    boolean takephoto = true;
    boolean isRegistering = false;
    AlertDialog.Builder ad;
    String regName;
    private Camera.Face[] mFaces;
    int countPics = 0;

    final int CAMERA_ID = 1;
    private final int IDD_DIALOG_FAIL = 0;
    private final int IDD_DIALOG_SIGNUP_SUCCESS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mFaceView = new FaceOverlayView(this);
        sv = (SurfaceView) findViewById(R.id.sv);
        mFaceView.setSurfaceHeight(sv.getHeight());
        mFaceView.setSurfaceWidgh(sv.getWidth());
        addContentView(mFaceView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        takepicBtn = (Button) findViewById(R.id.takepic_btn);
        title_field = (TextView) findViewById(R.id.title_view);
        //takepicBtn.setEnabled(false);
        holder = sv.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Intent intent = getIntent();
        if (intent != null) {
            regName = intent.getStringExtra("name");
            if (regName != null) {
                isRegistering = true;
                arr = new File[]{new File(PhotoActivity.this.getExternalFilesDir(null), regName + "photo1.jpg"), new File(PhotoActivity.this.getExternalFilesDir(null), regName + "photo2.jpg"),
                        new File(PhotoActivity.this.getExternalFilesDir(null), regName + "photo3.jpg")};
                title_field.setText("Take 3 photos, " + regName);
            } else {
                photoToCheck = new File(PhotoActivity.this.getExternalFilesDir(null), "phototocheck.jpg");
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case IDD_DIALOG_FAIL:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Your face is not recognized. Would you like to try again or sign up?")
                        .setTitle("Login failed!")
                        .setCancelable(false)
                        .setPositiveButton("Try again",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        ResettingCamera();
                                    }
                                })
                        .setNegativeButton("sign up!",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(PhotoActivity.this, SignUpActivity.class);
                                        startActivity(intent);
                                    }
                                });
                return builder.create();
            case IDD_DIALOG_SIGNUP_SUCCESS:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setMessage("Photos are taken succesfully! Would you like to log in?")
                        .setTitle("Sign up")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        isRegistering = false;
                                        countPics = 0;
                                        title_field.setText("Take your photo to log in!");
                                        ResettingCamera();
                                    }
                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(PhotoActivity.this, StartWindowActivity.class);
                                        startActivity(intent);
                                    }
                                });
                return builder2.create();
            default:
                return null;
        }
    }
    protected void onStart() {
        super.onStart();
        camera = Camera.open(CAMERA_ID);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //camera = Camera.open(CAMERA_ID);
        ResettingCamera();

    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (camera != null) {
//            camera.setPreviewCallback(null);
//            camera.stopFaceDetection();
//            camera.stopPreview();
//            camera.release();
//            camera = null;
//        }
    }
    protected  void onStop() {
        super.onStop();
        if (camera != null) {
            //camera.setPreviewCallback(null);
            camera.stopFaceDetection();
            camera.stopPreview();
            camera.release();
            camera = null;
        }

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
         //   camera.setFaceDetectionListener(faceDetectionListener);
         //   camera.startFaceDetection();
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width / previewSize.height;
        int previewSurfaceWidth = sv.getWidth();
        int previewSurfaceHeight = sv.getHeight();
        ViewGroup.LayoutParams lp = sv.getLayoutParams();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(90);
            lp.height = previewSurfaceHeight;
            lp.width = (int) (previewSurfaceHeight / aspect);
            ;
        } else {
            camera.setDisplayOrientation(0);
            lp.width = previewSurfaceWidth;
            lp.height = (int) (previewSurfaceWidth / aspect);
        }
        sv.setLayoutParams(lp);
       // camera.startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if ((faces.length == 1) && (faces[0].score >= 50)) {
                takepicBtn.setEnabled(true);
                mFaceView.setFaces(faces);
            } else {
                //takepicBtn.setEnabled(false);
                mFaceView.setFaces(null);
            }
        }
    };


    @Override
    public void onAutoFocus(boolean paramBoolean, Camera paramCamera) {
        if (paramBoolean) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                       public void onPictureTaken(byte[] data, Camera camera) {
                            try {
                               FileOutputStream fos = new FileOutputStream(photoToCheck);
                                fos.write(data);
                               fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            confirmBtn.setVisibility(View.VISIBLE);
                            takepicBtn.setText("Retake");
                            takephoto = false;
                       }
                    }
            );
        }
    }

    public void onClickPicture(View view) {
        if (isRegistering == false) {
            if (takephoto == true) {
                //camera.autoFocus(this);
                camera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                    try {
                                        FileOutputStream fos = new FileOutputStream(photoToCheck);
                                        fos.write(data);
                                        fos.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                confirmBtn.setVisibility(View.VISIBLE);
                                takepicBtn.setText("Retake");
                                takephoto = false;
                            }
                        }
                );

            }
            else {
                ResettingCamera();
            }
        } else {
            if (takephoto == true) {
                //camera.autoFocus(this);
                camera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                    try {
                                        FileOutputStream fos = new FileOutputStream(arr[countPics]);
                                        fos.write(data);
                                        fos.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                confirmBtn.setVisibility(View.VISIBLE);
                                takepicBtn.setText("Retake");
                                takephoto = false;
                            }
                        }
                );
            } else {
                ResettingCamera();
            }
        }
    }

    public void sendToServer(final String image, final String server) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(server);
                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/android/data/pi.hse.facedetection/files/"+image +".jpg");
                    MultipartEntity mpEntity = new MultipartEntity();
                    mpEntity.addPart(image, new FileBody(file, "image/jpeg"));
                    httppost.setEntity(mpEntity);
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);
                    isLogged = false;
                    System.out.println("response here!!!");
                    System.out.println(the_string_response);
                    if (the_string_response.equals("true")){
                        isLogged = true;
                    }
                    System.out.println(the_string_response.equals("true"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    System.out.println("Error in http connection " + e.toString());
                }
            }
        });
        t.start();
        try{
            t.join();
        }
        catch(InterruptedException e){
        }
    }

    public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException {
        String res = "";
        StringBuffer buffer = new StringBuffer();
        inputStream = response.getEntity().getContent();
        int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..
        if (contentLength < 0) {
        } else {
            byte[] data = new byte[512];
            int len = 0;
            try {
                while (-1 != (len = inputStream.read(data))) {
                    buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer…..
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close(); // closing the stream…..
            } catch (IOException e) {
                e.printStackTrace();
            }
            res = buffer.toString();     // converting stringbuffer to string…..
        }
        return res;
    }

    public void onConfirmClicked(View view) {

        if (isRegistering == false) {
            sendToServer("phototocheck", "http://192.168.1.2:1234/check");
            // boolean ServerResponse = false;
            boolean serverResponse = isLogged;
            System.out.println("response is:" + serverResponse);
            //Getting server response or checking it right here dunno
            if (serverResponse == true) {
                Intent intent = new Intent(this, LogOnActivity.class);
                startActivity(intent);
            } else {
                showDialog(IDD_DIALOG_FAIL);
            }
        } else {
            countPics++;
            if (countPics == 3) {
                //send to server
                //success!
                sendToRegister(arr,"http://192.168.1.2:1234/register");
                if (isRegOk) {
                    showDialog(IDD_DIALOG_SIGNUP_SUCCESS);
                }
                else {
                    //showDialog(IDD_DIALOG_SIGNUP_ERROR)
                    //would you like to try again?
                }

            } else {
                ResettingCamera();
            }
        }
    }

    public void ResettingCamera() {
       // camera.stopFaceDetection();
        mFaceView.setFaces(null);
        camera.startPreview();
        takepicBtn.setText("Pic");
        confirmBtn.setVisibility(View.GONE);
        takephoto = true;
        //takepicBtn.setEnabled(false);
       // camera.startFaceDetection();
    }

    public void sendToRegister(final File files[], final String server) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(server);
                    MultipartEntity mpEntity = new MultipartEntity();
                    for (int i=0; i<3; i++) {
                        mpEntity.addPart("regphoto"+i, new FileBody(files[i], "image/jpeg"));
                    }
                    httppost.setEntity(mpEntity);
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);
                    isRegOk =false;
                    System.out.println("response here!!!");
                    System.out.println(the_string_response);
                    if (the_string_response.equals("ok")){
                        isRegOk =true;
                    }
                    //System.out.println(the_string_response.equals("true"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    System.out.println("Error in http connection " + e.toString());
                }
            }
        });
        t.start();
        try{
            t.join();
        }
        catch(InterruptedException e){
        }
    }
}

