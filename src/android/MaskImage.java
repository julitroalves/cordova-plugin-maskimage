package com.julitroalves;

import android.app.Activity;
import android.os.Environment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MaskImage extends CordovaPlugin {
    private static String TAG =  "MaskImage";
    
    private final String maskImageScaleFitAction = "makeMaskImageScaleFit";
    private final String maskImageAction = "makeMaskImage";
    /**
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (maskImageScaleFitAction.equals(action)) {
            return makeMaskImageScaleFit(args, callbackContext);
        } else if (maskImageAction.equals(action)) {
            return makeMaskImage(args, callbackContext);
        }

        return false;
    }

    public void onFileMaskedAlready(String path, CallbackContext callbackContext) {
        JSONArray data = new JSONArray();
        data.put(path);
        
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
        pluginResult.setKeepCallback(true);

        callbackContext.sendPluginResult(pluginResult);
    }

    // public void makeMaskImageScaleFit(ImageView mImageView, int mContent) {
    public boolean makeMaskImageScaleFit(JSONArray args, CallbackContext callbackContext) {
        try {
            String imageSrc = args.getString(0);
            String maskSrc = args.getString(1);

            Log.d(TAG, "Image Src: " + imageSrc);
            Log.d(TAG, "Mask Src: " + maskSrc);

            Bitmap image = BitmapFactory.decodeFile(imageSrc);
            Bitmap mask = BitmapFactory.decodeFile(maskSrc);

            if (image == null || mask == null) {
                return false;
            }

            // Bitmap image = readyLargeBitmap(imageSrc, (int) mask.getWidth(), (int) mask.getHeight());

            Log.d(TAG, "Image: " + image.getWidth() + "x" + image.getHeight());
            Log.d(TAG, "Mask: " + mask.getWidth() + "x" + mask.getHeight());

            // Get mask bitmap
            // Bitmap mask = BitmapFactory.decodeResource(getResources(), R.drawable.custom_shape);

            // Scale imageView and it's bitmap to the size of the mask
            // mImageView.getLayoutParams().width = mask.getWidth();
            // mImageView.getLayoutParams().height = mask.getHeight();
            
            // Get bitmap from ImageView and store into 'original'
            // mImageView.setDrawingCacheEnabled(true);
            // mImageView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            // mImageView.layout(0, 0, mImageView.getMeasuredWidth(), mImageView.getMeasuredHeight());
            // mImageView.buildDrawingCache(true);
            // Bitmap original = mImageView.getDrawingCache();
            
            // Scale that bitmap 
            Bitmap original_scaled = Bitmap.createScaledBitmap(image, mask.getWidth(), mask.getHeight(), false);
            // mImageView.setImageBitmap(original_scaled);
            // mImageView.setDrawingCacheEnabled(false);
            
            // Create result bitmap
            Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
            
            // Perform masking
            Canvas mCanvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mCanvas.drawBitmap(original_scaled, 0, 0, null);
            // mCanvas.drawBitmap(mask, 0, 0, paint);
            mCanvas.drawBitmap(mask, 0, 0, null);
            paint.setXfermode(null);
            
            // Set imageView to 'result' bitmap
            // mImageView.setImageBitmap(result);
            // mImageView.setScaleType(ScaleType.FIT_XY);

            // Make background transparent
            // mImageView.setBackgroundResource(android.R.color.transparent);

            File maskedPictureFile = createFileImage("_smasked", "png");
            saveBitmap2File(maskedPictureFile, result);

            onFileMaskedAlready(maskedPictureFile.getAbsolutePath(), callbackContext);

            return true;
        } catch(Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    public boolean makeMaskImage(JSONArray args, CallbackContext callbackContext) {
        try {
            String imageSrc = args.getString(0);
            String maskSrc = args.getString(1);

            Bitmap image = BitmapFactory.decodeFile(imageSrc);
            Bitmap mask = BitmapFactory.decodeFile(maskSrc);

            Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
            Canvas mCanvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mCanvas.drawBitmap(image, 0, 0, null);
            mCanvas.drawBitmap(mask, 0, 0, paint);
            paint.setXfermode(null);

            // mImageView.setImageBitmap(result);
            // mImageView.setScaleType(ScaleType.CENTER);
            // mImageView.setBackgroundResource(android.R.color.transparent);

            File maskedPictureFile = createFileImage("_masked", "png");
            saveBitmap2File(maskedPictureFile, result);

            onFileMaskedAlready(maskedPictureFile.getAbsolutePath(), callbackContext);

            return true;
        } catch(Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    private Bitmap readyLargeBitmap(String filePath, int dstWidth, int dstHeight) {
        try {

            InputStream in = new FileInputStream(filePath);

            BitmapFactory.Options options = new BitmapFactory.Options();
            
            // decode full image
            Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(roughBitmap, dstWidth, dstHeight, false);

            return resizedBitmap;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());

            return null;
        }
    }

    private File createFileImage(String suffix, String type) {
        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
          Log.d(TAG, "Can't create directory to save image.");
          
          return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + suffix + "." + type;

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);

        return pictureFile;
    }

    private boolean saveData2File(File image, byte[] data) {
        try {
                FileOutputStream fos = new FileOutputStream(image);
                fos.write(data);
                fos.close();

                return true;
        } catch(Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    private boolean saveBitmap2File(File image, Bitmap data) {
        try {
            FileOutputStream fos = new FileOutputStream(image);
            data.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private File getDir() {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    
        return new File(sdDir, "FotoLembrancas");
    }
}