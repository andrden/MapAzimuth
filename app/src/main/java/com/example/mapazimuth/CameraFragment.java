package com.example.mapazimuth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.security.Policy;
import java.util.List;

import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageUInt8;

public class CameraFragment extends Fragment implements Camera.PreviewCallback{
    Context context;
    View.OnTouchListener onTouchListener;

    private Camera mCamera;
    Camera.Parameters cameraParms;
    boolean flipHorizontal;

    private CameraPreview mPreview;
    ModifiedPreview modifiedPreview;

    float viewAngle;
    String desc = "desc....";
    String azimuth = "Az=";
    double azimuthVal;

    void setContext(Context context, View.OnTouchListener onTouchListener){
        this.context = context;
        this.onTouchListener = onTouchListener;
    }

    void makeUserOfNewAzimuth(double azimuth, String desc){
        this.azimuth = "Az="+(int)azimuth;
        this.desc = desc;
        this.azimuthVal = azimuth;
        modifiedPreview.postInvalidate();
    }



    class ModifiedPreview extends View{
        Paint paint = new Paint(){{
            setStyle(Style.STROKE);
            setColor(Color.GREEN);
        }};
        Paint paintTxt = new Paint(){{
            setStyle(Style.STROKE);
            setColor(Color.GREEN);
            setTextSize(25);
        }};
        Paint paintTxtAz = new Paint(){{
            setStyle(Style.STROKE);
            setColor(Color.GREEN);
            setTextSize(50);
        }};

        public ModifiedPreview(Context context) {
            super(context);

            // This call is necessary, or else the
            // draw method will not be called.
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(20, 20, 20, paint);
            canvas.drawLine(canvas.getWidth() / 2, canvas.getHeight() / 4, canvas.getWidth() / 2, canvas.getHeight(), paint);
            canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, paint);
            canvas.drawText(desc, canvas.getWidth() / 4, canvas.getHeight() * 2 / 3, paintTxt);
            canvas.drawText(azimuth, canvas.getWidth()/4, canvas.getHeight()/3, paintTxtAz);

            if( azimuthVal >= 0 ) {
                //int previewW = canvas.getWidth(); // actually must be preview width
                int previewW = CameraFragment.this.mPreview.getChildAt(0).getWidth();
                float degreePixels = previewW / viewAngle;
                int azCenter = (int)azimuthVal;
                int halfViewAngle = Math.round(viewAngle / 2);
                for( int i= - halfViewAngle; i<= + halfViewAngle; i++){
                    float azX = canvas.getWidth()/2 - Math.round((azimuthVal - azCenter - i) * degreePixels);
                    canvas.drawLine(azX, 5, azX, 15, paint);
                    int az = azCenter + i;
                    if( az>360 ) az -= 360;
                    if( az<0 ) az += 360;
                    canvas.drawText(""+az, azX-3, 35, paintTxt);
                }
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);


        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(context,this,false);

        FrameLayout preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);

        modifiedPreview = new ModifiedPreview(context);
        modifiedPreview.setOnTouchListener(onTouchListener);

        preview.addView(modifiedPreview);
        preview.addView(mPreview, 0);

        setUpAndConfigureCamera();

//        ConfigGeneralDetector config = new ConfigGeneralDetector();
//        config.maxFeatures = 150;
//        config.threshold = 40;
//        config.radius = 3;
//
//        PointTracker<ImageUInt8> tracker =
//                FactoryPointTracker.klt(new int[]{1, 2, 4}, config, 3, ImageUInt8.class, ImageSInt16.class);
//        PointProcessing pointProcessing = new PointProcessing(tracker);
//        pointProcessing.process();

        return rootView;
    }

    public void onPause(){
        super.onPause();

        // stop the camera preview and all processing
        if (mCamera != null){
            mPreview.setCamera(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    /**
     * Sets up the camera if it is not already setup.
     */
    void setUpAndConfigureCamera() {
        // Open and configure the camera
        mCamera = selectAndOpenCamera();

        cameraParms = mCamera.getParameters();

        // Select the preview size closest to 320x240
        // Smaller images are recommended because some computer vision operations are very expensive
        List<Camera.Size> sizes = cameraParms.getSupportedPreviewSizes();
        //Camera.Size s = sizes.get(closest(sizes,320,240));
        Camera.Size s = sizes.get(closest(sizes, 800, 480));
        cameraParms.setPreviewSize(s.width, s.height);

        int zoom = cameraParms.getMaxZoom();
        cameraParms.setZoom(zoom);
        if( cameraParms.getZoomRatios()!=null ) {
            viewAngle = cameraParms.getVerticalViewAngle()/*we are in portait*/ * 100 / cameraParms.getZoomRatios().get(zoom);
        }
        cameraParms.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);

                // Sony:
                // cameraParms.get("iso-values") auto,off,ISO_HJR,ISO100,ISO200,ISO400,ISO800,ISO1600
                //cameraParms.set("iso", "ISO1600");
                // param "sony-iso"="auto"

                // "vertical-view-angle"=42.5
                // "horizontal-view-angle"=54.8

        mCamera.cancelAutoFocus();
        mCamera.setParameters(cameraParms);

        Log.w("VideoActivity", "chosen preview size " + s.width + " x " + s.height);

//        videoProcessor = new VideoProcessor(s);
//
//        // start image processing thread
//        thread = new ThreadProcess();
//        thread.start();

        // Start the video feed by passing it to mPreview
        mPreview.setCamera(mCamera);
    }

    /**
     * Step through the camera list and select a camera.  It is also possible that there is no camera.
     * The camera hardware requirement in AndroidManifest.xml was turned off so that devices with just
     * a front facing camera can be found.  Newer SDK's handle this in a more sane way, but with older devices
     * you need this work around.
     */
    private Camera selectAndOpenCamera() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();

        int selected = -1;

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, info);

            if( info.facing == Camera.CameraInfo.CAMERA_FACING_BACK ) {
                selected = i;
                flipHorizontal = false;
                break;
            } else {
                // default to a front facing camera if a back facing one can't be found
                selected = i;
                flipHorizontal = true;
            }
        }

        if( selected == -1 ) {
            dialogNoCamera();
            return null; // won't ever be called
        } else {
            return Camera.open(selected);
        }
    }

    /**
     * Gracefully handle the situation where a camera could not be found
     */
    private void dialogNoCamera() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your device has no cameras!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Goes through the size list and selects the one which is the closest specified size
     */
    public static int closest( List<Camera.Size> sizes , int width , int height ) {
        int best = -1;
        int bestScore = Integer.MAX_VALUE;

        for( int i = 0; i < sizes.size(); i++ ) {
            Camera.Size s = sizes.get(i);

            int dx = s.width-width;
            int dy = s.height-height;

            int score = dx*dx + dy*dy;
            if( score < bestScore ) {
                best = i;
                bestScore = score;
            }
        }

        return best;
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
       // "".length();
    }
}