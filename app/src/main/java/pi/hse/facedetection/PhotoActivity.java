package pi.hse.facedetection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class PhotoActivity extends Activity  {
//gui
    SurfaceView sv;
    private FaceOverlayView mFaceView;
    Button confirmBtn;
    Button takepicBtn;
    Camera camera;
    TextView title_field;
    SurfaceHolder holder;

    //for image uploading
    InputStream inputStream;
    Bitmap bitmap;
    ByteArrayOutputStream stream;
    byte[] byte_arr;
    String image_str;
    ArrayList<NameValuePair> nameValuePairs;

    HolderCallback holderCallback;


    File photo1, photo2, photo3, photoToCheck;
    File[] arr;

    boolean takephoto=true;
    boolean isRegistering = false;
    AlertDialog.Builder ad;
    String regName;
    private Camera.Face[] mFaces;
    int countPics = 0;

    final int CAMERA_ID = 1;
    final boolean FULL_SCREEN = true;
    private final int IDD_DIALOG_FAIL = 0;
    private final int IDD_DIALOG_SIGNUP_SUCCESS=1;
    private int mOrientation;
    private int mOrientationCompensation;
    private OrientationEventListener mOrientationEventListener;
    private int mDisplayRotation;
    private int mDisplayOrientation;





    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if( (faces.length == 1 )&& ( faces[0].score >= 50)  ) {
                takepicBtn.setEnabled(true);
                mFaceView.setFaces(faces);
            }
            else {
                takepicBtn.setEnabled(false);
            }



        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       requestWindowFeature(Window.FEATURE_NO_TITLE);
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
               WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        mFaceView = new FaceOverlayView(this);
        sv = (SurfaceView)findViewById(R.id.sv);
        addContentView(mFaceView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        takepicBtn = (Button) findViewById(R.id.takepic_btn);
        title_field = (TextView)findViewById(R.id.title_view);
        takepicBtn.setEnabled(false);

        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();
        holder.addCallback(holderCallback);

        Intent intent = getIntent();
        if (intent != null ) {
            regName = intent.getStringExtra("name");
            if (regName != null) {
                isRegistering = true;
                /*photo1 = new File(PhotoActivity.this.getFilesDir(), regName + "photo1.jpg");
                photo2 = new File(PhotoActivity.this.getFilesDir(), regName + "photo2.jpg");
                photo3 = new File(PhotoActivity.this.getFilesDir(), regName + "photo3.jpg"); */
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
                                        countPics=0;
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


    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);
        //setPreviewSize(FULL_SCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    class HolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setFaceDetectionListener(faceDetectionListener);
                camera.startFaceDetection();
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(CAMERA_ID);
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }

    void setPreviewSize(boolean fullScreen) {

        // получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        // определяем размеры превью камеры
        Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран (второй вариант из урока)
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью (третий вариант из урока)
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        sv.getLayoutParams().height = (int) (rectPreview.bottom);
        sv.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }

    public void onClickPicture(View view) {

        if (isRegistering == false) {
            if (takephoto == true) {
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
            } else {

                ResettingCamera();
            }
        }

        else {

            if (takephoto == true) {
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


    public void sendToServer(String image) {
        bitmap = BitmapFactory.decodeFile(image + ".jpeg");
        stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream); //compress to which format you want.
        byte[] byte_arr = stream.toByteArray();
        String image_str = Base64.encodeBytes(byte_arr);
        nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("image", image_str));
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                try{
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://10.0.2.2/Upload_image_ANDROID/upload_image.php");
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    String the_string_response = convertResponseToString(response);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                        }
                    });

                }catch(Exception e){
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                        }
                    });
                    System.out.println("Error in http connection "+e.toString());
                }
            }
        });
        t.start();
}
    public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException{

        String res = "";
        StringBuffer buffer = new StringBuffer();
        inputStream = response.getEntity().getContent();
        int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..
        if (contentLength < 0){
        }
        else{
            byte[] data = new byte[512];
            int len = 0;
            try
            {
                while (-1 != (len = inputStream.read(data)) )
                {
                    buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer…..
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                inputStream.close(); // closing the stream…..
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            res = buffer.toString();     // converting stringbuffer to string…..
        }
        return res;
    }



    public void onConfirmClicked(View view) {
        sendToServer("phototocheck.jpeg");
        boolean ServerResponse = false;
        //Getting server response or checking it right here dunno
        if (isRegistering == false) {
            if (ServerResponse == true) {
                Intent intent = new Intent(this, LogOnActivity.class);
                startActivity(intent);
            } else {

                showDialog(IDD_DIALOG_FAIL);
                ResettingCamera();
            }

        }

        else {
            countPics++;
            if (countPics == 3) {

                //send to server
                //success!
                showDialog(IDD_DIALOG_SIGNUP_SUCCESS);

            }
            else {
                ResettingCamera();

            }



        }

    }

    public void ResettingCamera() {

        camera.startPreview();
        takepicBtn.setText("Pic");
        confirmBtn.setVisibility(View.GONE);
        takephoto = true;
    }




}

