package com.youbanganda;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import com.youbanganda.tf.Classifier;
import com.youbanganda.tf.TensorFlowObjectDetectionAPIModel;
import com.youbanganda.tf.TensorFlowUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements Camera.PreviewCallback {

    private Paint paint;
    private SurfaceView GLSurfaceView1;
    private boolean isProcessingFrame = false;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.8f;

    private ImageView iv;
    private static final String TF_OD_API_MODEL_FILE =
            "file:///android_asset/frozen_inference_graph_v1.pb";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";

    private static final int TF_OD_API_INPUT_SIZE = 300;

    private Camera back_camera;

    private Handler handler = new Handler();
    private Classifier detector;
    private Map<Integer, Integer> soundMap;

    //声音
    private SoundPool soundPool;
    //是否在播放
    private boolean soundPoolIsPlay;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        if (Build.VERSION.SDK_INT > 22) {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            } else {

            }
        }


        GLSurfaceView1 = (SurfaceView) findViewById(R.id.GLSurfaceView1);
        iv = findViewById(R.id.iv);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1.0f);
        paint.setColor(Color.RED);
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(3).build();
        soundMap = new HashMap<>();
        soundMap.put(1, soundPool.load(MainActivity.this, R.raw.anquanjiashi, 1));
        soundMap.put(3, soundPool.load(MainActivity.this, R.raw.pilaojiashi, 1));

        try {
            initSubCamera();
        } catch (Exception e) {

        }


        try {
            detector = TensorFlowObjectDetectionAPIModel.create(
                    getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE, TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void initSubCamera() {

        back_camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        Camera.Parameters parameters = back_camera.getParameters();
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
//        for (int i = 0; i < list.size(); i++) {
//            Toast.makeText(CameraerActivity.this, "width:" + list.get(i).width + "  height:" + list.get(i).height, Toast.LENGTH_SHORT).show();
//        }
        parameters.setPreviewSize(640, 400);
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewFpsRange(20, 20);
        back_camera.setParameters(parameters);
        back_camera.setPreviewCallback(this);
        try {
            GLSurfaceView1.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        back_camera.setPreviewDisplay(GLSurfaceView1.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    back_camera.startPreview();

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        back_camera.setPreviewCallback(subCallback);
    }


    @Override
    protected void onDestroy() {
        if (GLSurfaceView1 != null)
            GLSurfaceView1.getHolder().getSurface().release();
        if (back_camera != null)
            back_camera.release();
        super.onDestroy();

    }


    ByteArrayOutputStream baos;
    byte[] rawImage;
    Bitmap bitmap;

    //是否发生不安全驾驶（不包括闭眼）
    private boolean isBuAnquan;
    Bitmap cropCopyBitmap;

    int[] shibiejieguo;
    //是否有被识别出的类
    private static boolean[] isHaveClass = new boolean[7];
    //识别出的类次数
    private static int[] classOnce = new int[7];
    //是否闭过眼睛
    private boolean isCloseEyes;
    //小图标
    Bitmap bit = null;

    //出现过几次眼睛（超过2次 就不显示眼睛了）
    private int isEyeNumber;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.e("data", data.length + "");
        if (isProcessingFrame) {
            return;
        }
        isProcessingFrame = true;

//        camera.setOneShotPreviewCallback(null);
        //处理data
        Camera.Size previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(
                data,
                ImageFormat.NV21,
                previewSize.width,
                previewSize.height,
                null);
        baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);// 80--JPG图片的质量[0-100],100最高
//        yuvimage.compressToJpeg(new Rect(0, 0, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE), 100, baos);// 80--JPG图片的质量[0-100],100最高
        rawImage = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将rawImage转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);


        final long startTime = SystemClock.uptimeMillis();
        final List<Classifier.Recognition> results = detector.recognizeImage(Bitmap.createScaledBitmap(bitmap, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, true));
        long lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
        Log.e("time", lastProcessingTimeMs + "");


        shibiejieguo = new int[results.size()];
        cropCopyBitmap = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888);

        final Canvas canvas = new Canvas(cropCopyBitmap);
        for (int i = 0; i < results.size(); i++) {
            final RectF location = results.get(i).getLocation();
            if (location != null && results.get(i).getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                Log.e("face", results.get(i).getTitle() + "   " + results.get(i).getConfidence());
//                final Paint paint = new Paint();
//                paint.setStyle(Paint.Style.STROKE);
//                paint.setStrokeWidth(2.0f);

                int number = TensorFlowUtils.ClassNumber(results.get(i).getTitle());


                if (number != -1) {
                    if (number == 1 && !isCloseEyes) {
                        isCloseEyes = true;
                    }

//                    if (number != 1) {
//                        Log.e("111111122222", "   " + number);
//                        play("2001");
//                    }

                    classOnce[number - 1]++;
                    isHaveClass[number - 1] = true;
                    for (int j = 0; j < classOnce.length; j++) {
                        if (classOnce[j] > 3) {
//                            Log.e("111111122222", "111111111");
                            classOnce[j] = 0;
                            //这里访问网络
                            if (j + 1 == 1)
                                play("2003");
//                            else
//                                play("2001");
//                            handler.postDelayed(() -> {
//                                OkGo.<String>post(BaseApplication.netConnect + "/report_car_driver_status")
//                                        .params("type", 1)
//                                        .params("id", vehicleId)
//                                        .params("status", number)
//                                        .execute(new StringCallback() {
//                                            @Override
//                                            public void onSuccess(Response<String> response) {
//
//                                            }
//                                        });
//                            }, report_vehicle_exception_delay);
                        }
                    }
                }


                if (results.get(i).getTitle().equals("openeyes"))
                    shibiejieguo[i] = 1;
                else if (results.get(i).getTitle().equals("closeeyes"))
                    shibiejieguo[i] = 2;
                else if (results.get(i).getTitle().equals("phone"))
                    shibiejieguo[i] = 3;
                else if (results.get(i).getTitle().equals("smoke"))
                    shibiejieguo[i] = 4;
//                else
////                    paint.setColor(Color.WHITE);
//                    shibiejieguo[i] = 5;
//                canvas.drawRect(location, paint);

            }
        }


        isCloseEyes = false;
        for (int i = 0; i < isHaveClass.length; i++) {
            if (isHaveClass[i]) {
                isHaveClass[i] = false;
            } else {
                classOnce[i] = 0;
            }
        }


        Arrays.sort(shibiejieguo);
        for (int i = 0; i < shibiejieguo.length; i++) {
            if (shibiejieguo[i] == 1 || shibiejieguo[i] == 2) {
                isEyeNumber++;
            }
            if (isEyeNumber > 2 && (shibiejieguo[i] == 1 || shibiejieguo[i] == 2)) {
                shibiejieguo[i] = 0;
            }
        }
        isEyeNumber = 0;
        int x = TF_OD_API_INPUT_SIZE - 25;
        for (int i = 0; i < shibiejieguo.length; i++) {
            if (shibiejieguo[i] != 0) {

                if (shibiejieguo[i] == 1)
//                    paint.setColor(Color.GREEN);
                    bit = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.open);
                else if (shibiejieguo[i] == 2)
//                    paint.setColor(Color.RED);
                    bit = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.close);
                else if (shibiejieguo[i] == 3) {
//                    paint.setColor(0xFFFF9900);
                    bit = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.photo);
                    isBuAnquan = true;
                } else if (shibiejieguo[i] == 4) {
//                    paint.setColor(Color.YELLOW);
                    bit = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.mipmap.somke);
                    isBuAnquan = true;
                }

//                else if (shibiejieguo[i] == 5)
//                    paint.setColor(Color.WHITE);
//                canvas.drawCircle(x, 20, 10, paint);

                if (bit != null && !bit.isRecycled()) {
                    canvas.drawBitmap(bit, x, 20, paint);
                    x -= 15;
//                    bit.isRecycled();
                    if (isBuAnquan) {
                        isBuAnquan = false;
                        play("2001");
                    }
                }
            }

        }

        Log.e("jieguoji", Arrays.toString(shibiejieguo));

        Matrix m = new Matrix();
        m.postScale(-1, 1);
//        iv.setImageBitmap(cropCopyBitmap);
        iv.setImageBitmap(Bitmap.createBitmap(cropCopyBitmap, 0, 0
                , cropCopyBitmap.getWidth(), cropCopyBitmap.getHeight()
                , m, true));
        isProcessingFrame = false;
    }


    private void play(String str) {

        switch (str) {
            case "2001":
                if (!soundPoolIsPlay) {
                    soundPoolIsPlay = true;
                    soundPool.autoPause();
                    soundPool.play(soundMap.get(1), 1, 1, 0, 0, 1);
                    handler.postDelayed(() -> {
                        soundPoolIsPlay = false;

                    }, 3000);
                }
                break;
            case "2002":
//                        if(!soundPoolIsPlay) {
//                        soundPool.autoPause();
//                        soundPool.play(soundMap.get(2), 1, 1, 0, 0, 1);
//                        }
                break;
            case "2003":
                if (!soundPoolIsPlay) {
                    soundPoolIsPlay = true;
                    soundPool.autoPause();
                    soundPool.play(soundMap.get(3), 1, 1, 0, 0, 1);
                    handler.postDelayed(() -> {
                        soundPoolIsPlay = false;
                    }, 3000);
                }
                break;
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                Log.e("abc", permissions[i]);
            }
        }
    }
}
