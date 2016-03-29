package hse_pi.facedetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    SurfaceView surfaceView;
    private Button confirmBtn;
    private Button takePicBtn;
    private TextView title_field;

    private File[] arr;
    private File photoToCheck;
    private boolean isRegistering = false;

    private Camera camera;
    private final int IDD_DIALOG_FAIL = 0;
    private final int IDD_DIALOG_SIGNUP_SUCCESS = 1;

    private boolean takePhoto = true;
    private int countPics = 0;
    private boolean isRegOk;
    private boolean isLogged;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            boolean isImmersiveModeEnabled =
                    ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        }
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (Build.VERSION.SDK_INT >= 18) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
        }
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        setContentView(R.layout.activity_camera);
        surfaceView = (SurfaceView) findViewById(R.id.ca_cameraSV);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        takePicBtn = (Button) findViewById(R.id.takePicBtn);
        takePicBtn.setEnabled(false);
        takePicBtn.setBackgroundColor(getResources().getColor(R.color.colorRed));
        title_field = (TextView) findViewById(R.id.titleTxt);
        Intent intent = getIntent();
        if (intent != null) {
            String regName = intent.getStringExtra("name");
            if (regName != null) {
                isRegistering = true;
                arr = new File[]{
                        new File(CameraActivity.this.getExternalFilesDir(null),
                                regName + "photo1.jpg"),
                        new File(CameraActivity.this.getExternalFilesDir(null),
                                regName + "photo2.jpg"),
                        new File(CameraActivity.this.getExternalFilesDir(null),
                                regName + "photo3.jpg")};
                title_field.setText(R.string.step3);
                title_field.append(", " + regName);
            } else {
                photoToCheck = new File
                        (CameraActivity.this.getExternalFilesDir(null), "photoToCheck.jpg");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        int CAMERA_ID = 1;
        camera = Camera.open(CAMERA_ID);
        camera.setFaceDetectionListener(faceDetectionListener);
        camera.startFaceDetection();
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width / (float) previewSize.height;
        int previewSurfaceWidth = surfaceView.getWidth();
        int previewSurfaceHeight = surfaceView.getHeight();
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            camera.setDisplayOrientation(90);
            lp.height = previewSurfaceHeight;
            lp.width = (int) ((float) previewSurfaceHeight / aspect);
        } else {
            camera.setDisplayOrientation(0);
            lp.width = previewSurfaceWidth;
            lp.height = (int) ((float) previewSurfaceWidth / aspect);
        }
        surfaceView.setLayoutParams(lp);
        camera.startPreview();
    }

    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if ((faces.length == 1) && (faces[0].score >= 50)) {
                takePicBtn.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                takePicBtn.setEnabled(true);
            } else {
                takePicBtn.setEnabled(false);
                takePicBtn.setBackgroundColor(getResources().getColor(R.color.colorRed));
            }
        }

    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e) {
            //ignore...
        }

        configureCamera(width, height);
        setDisplayOrientation();
        camera.startPreview();
    }

    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = camera.getParameters();
        setOptimalPreviewSize(parameters, width, height);
        Camera.Size bestSize;
        List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }
        List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
        for (Integer previewFormat : supportedPreviewFormats) {
            if (previewFormat == ImageFormat.YV12) {
                parameters.setPreviewFormat(previewFormat);
            }
        }
        parameters.setPreviewSize(bestSize.width, bestSize.height);
        parameters.setPictureSize(bestSize.width, bestSize.height);
        camera.setParameters(parameters);
    }

    private void setOptimalPreviewSize(Camera.Parameters cameraParameters, int width, int height) {
        List<Camera.Size> previewSizes = cameraParameters.getSupportedPreviewSizes();
        float targetRatio = (float) width / height;
        Camera.Size previewSize = getOptimalPreviewSize(this, previewSizes, targetRatio);
        cameraParameters.setPreviewSize(previewSize.width, previewSize.height);
    }

    public static Camera.Size getOptimalPreviewSize(Activity currentActivity,
                                                    List<Camera.Size> sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.001;
        if (sizes == null) return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        Point point = getDefaultDisplaySize(currentActivity, new Point());
        int targetHeight = Math.min(point.x, point.y);
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @SuppressWarnings("deprecation")
    private static Point getDefaultDisplaySize(Activity activity, Point size) {
        Display d = activity.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            d.getSize(size);
        } else {
            size.set(d.getWidth(), d.getHeight());
        }
        return size;
    }

    private void setDisplayOrientation() {
        int mDisplayRotation = getDisplayRotation(CameraActivity.this);
        int mDisplayOrientation = getDisplayOrientation(mDisplayRotation, 0);
        camera.setDisplayOrientation(mDisplayOrientation);
    }

    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static int getDisplayOrientation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.setPreviewCallback(null);
        camera.setFaceDetectionListener(null);
        camera.setErrorCallback(null);
        camera.release();
        camera = null;
    }

    public void onClickPicture(View view) {
        if (!isRegistering) {
            if (takePhoto) {
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
                                takePicBtn.setText(R.string.retake);
                                takePicBtn.setBackgroundColor(getResources().getColor(R.color.colorRed));
                                takePhoto = false;
                            }
                        }
                );

            } else {
                ResettingCamera();
            }
        } else {
            if (takePhoto) {
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
                                takePicBtn.setText(R.string.retake);
                                takePicBtn.setBackgroundColor(getResources().getColor(R.color.colorRed));
                                takePhoto = false;
                            }
                        }
                );
            } else {
                ResettingCamera();
            }
        }
    }

    public void sendToRegister(final File files[], final String server) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(server);
                    MultipartEntity mpEntity = new MultipartEntity();
                    for (int i = 0; i < 3; i++) {
                        mpEntity.addPart("regphoto" + i, new FileBody(files[i], "image/jpeg"));
                    }
                    httppost.setEntity(mpEntity);
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);
                    isRegOk = the_string_response.equals("ok");
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
        try {
            t.join();
        } catch (InterruptedException e) {
            //Ignore
        }
    }

    public void sendToServer(final String image, final String server) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isLogged = false;
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(server);
                    File file = new File(Environment.getExternalStorageDirectory().toString()
                            + "/android/data/hse_pi.facedetection/files/" + image + ".jpg");
                    MultipartEntity mpEntity = new MultipartEntity();
                    mpEntity.addPart(image, new FileBody(file, "image/jpeg"));
                    httppost.setEntity(mpEntity);
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);
                    if (!the_string_response.equals("false")){
                        email = the_string_response;
                        isLogged = true;
                    }
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
        try {
            t.join();
        } catch (InterruptedException e) {
            //Ignore
        }
    }

    public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException {
        String res = "";
        StringBuilder buffer = new StringBuilder();
        InputStream inputStream = response.getEntity().getContent();
        int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..
        if (contentLength > 0) {
            byte[] data = new byte[512];
            int len;
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
        if (!isRegistering) {
            sendToServer("phototocheck", "http://dashulya.myftp.org:1234/check");
            boolean serverResponse = isLogged;
            System.out.println("response is:" + serverResponse);
            //Getting server response or checking it right here dunno
            if (serverResponse) {
                Intent intent = new Intent(this, LogOnActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                showDialog(IDD_DIALOG_FAIL);
            }
        } else {
            countPics++;
            if (countPics == 3) {
                //send to server
                //success!
                sendToRegister(arr, "http://dashulya.myftp.org:1234/register");
                if (isRegOk) {
                    showDialog(IDD_DIALOG_SIGNUP_SUCCESS);
                }
            } else {
                ResettingCamera();
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case IDD_DIALOG_FAIL:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage
                        ("Your face is not recognized. Would you like to try again or sign up?")
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
                                        Intent intent = new Intent(CameraActivity.this, SignUpActivity.class);
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
                                        title_field.setText(R.string.info);
                                        ResettingCamera();
                                    }
                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(CameraActivity.this, StartActivity.class);
                                        startActivity(intent);
                                    }
                                });
                return builder2.create();
            default:
                return null;
        }
    }

    public void ResettingCamera() {
        camera.startPreview();
        takePicBtn.setText(R.string.pic);
        takePicBtn.setBackgroundColor(getResources().getColor(R.color.colorGreen));
        confirmBtn.setVisibility(View.GONE);
        takePhoto = true;
        takePicBtn.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File dir = new File("/android/data/hse_pi.facedetection/files/");
        if (!dir.exists())
            return;
        for(File f : dir.listFiles())
            f.delete();
    }
}