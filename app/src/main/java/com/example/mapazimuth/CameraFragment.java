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

import java.util.List;

public class CameraFragment extends Fragment implements Camera.PreviewCallback{
    Context context;

    private Camera mCamera;
    Camera.Parameters cameraParms;
    boolean flipHorizontal;

    private CameraPreview mPreview;

    void setContext(Context context){
        this.context = context;
    }


    class ModifiedPreview extends View{
        Paint paint = new Paint(){{
            setStyle(Style.STROKE);
            setColor(Color.GREEN);
        }};
        Paint paintTxt = new Paint(){{
            setStyle(Style.STROKE);
            setColor(Color.BLUE);
            setTextSize(20);
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
            canvas.drawLine(20, 0, canvas.getWidth(), canvas.getHeight(), paint);
            canvas.drawText("AABBCC", canvas.getWidth()/2, canvas.getHeight()/2, paintTxt);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);


        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(context,this,false);

        FrameLayout preview = (FrameLayout) rootView.findViewById(R.id.camera_preview);

        preview.addView(new ModifiedPreview(context));
        preview.addView(mPreview, 0);

        setUpAndConfigureCamera();

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
        Camera.Size s = sizes.get(closest(sizes,800,480));
        cameraParms.setPreviewSize(s.width, s.height);

        cameraParms.setZoom(cameraParms.getMaxZoom());

        // Sony:
        // cameraParms.get("iso-values") auto,off,ISO_HJR,ISO100,ISO200,ISO400,ISO800,ISO1600
        //cameraParms.set("iso", "ISO1600");
        // param "sony-iso"="auto"

        // "vertical-view-angle"=42.5
        // "horizontal-view-angle"=54.8

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

    }
}