package com.buddy.buddysampleapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.buddy.buddysampleapp.R;
import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyFile;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.Picture;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;

public class FileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        // Initialize the button UI elements
        Button mUploadFile = (Button)findViewById(R.id.btn_submit_file);
        mUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        Button mDownloadFile = (Button)findViewById(R.id.btn_download_file);
        mDownloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void uploadFile() {

        // For this example we're going to draw an image ourselves
        // Typical users will want to upload pictures from their phone storage

        // generate a PNG for upload...
        Bitmap bitmap = Bitmap.createBitmap(30, 30, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        MyRoundCornerDrawable drawable = new MyRoundCornerDrawable(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        InputStream is = new ByteArrayInputStream(bytes);

        // Grab the description from the user
        final EditText mDescription = (EditText)findViewById(R.id.file_description);

        // Prep the file ID text
        final TextView mFileId = (TextView)findViewById(R.id.file_submit_text);

        // Set up the toasts
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        Map<String, Object> parameters = new HashMap<String,Object>();
        parameters.put("caption", mDescription.getText().toString());
        parameters.put("data", new BuddyFile(is, "image/png"));
        Buddy.<Picture>post("/pictures", parameters, new BuddyCallback<Picture>(Picture.class){
            @Override
            public void completed(BuddyResult<Picture> result) {
                if (result.getIsSuccess()) {
                    Toast toast = Toast.makeText(context, "Upload successful!", duration);
                    toast.show();
                    mFileId.setText("File ID: " + result.getResult().id);
                    mDescription.setText("");
                } else {
                    Toast toast = Toast.makeText(context, "Error uploading picture", duration);
                    toast.show();
                    mFileId.setText("Upload error: " + result.getError());
                }
            }
        });
    }

    private class MyRoundCornerDrawable extends Drawable {

        private Paint paint;

        public MyRoundCornerDrawable(Bitmap bitmap) {
            BitmapShader shader;
            shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(shader);
        }

        @Override
        public void draw(Canvas canvas) {
            int height = getBounds().height();
            int width = getBounds().width();
            RectF rect = new RectF(0.0f, 0.0f, width, height);
            canvas.drawRoundRect(rect, 30, 30, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            paint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

    }

    public void downloadFile() {
        // Get the UI elements we will use
        final TextView mFileDescription = (TextView)findViewById(R.id.text_file_description);
        final EditText mFileId = (EditText)findViewById(R.id.file_id);

        // Set up the toasts
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        Buddy.get(String.format("/pictures/%s/file", mFileId.getText().toString()), null, new BuddyCallback<BuddyFile>(BuddyFile.class) {
            @Override
            public void completed(BuddyResult<BuddyFile> result) {
                final BuddyResult<BuddyFile> resultFile = result;

                if (result.getIsSuccess()) {
                    Buddy.get(String.format("/pictures/%s", mFileId.getText().toString()), null, new BuddyCallback<Picture>(Picture.class) {
                        @Override
                        public void completed(BuddyResult<Picture> resultPicture) {
                            if (resultPicture.getIsSuccess()) {
                                // Display a toast, and display the caption
                                mFileDescription.setText("Caption: " + resultPicture.getResult().caption);
                                // At this point you could get the image data from resultFile then store or present it on the user's device
                                Toast toast = Toast.makeText(context, "File download success!", duration);
                                toast.show();
                                // This would be a good place to store the image in the gallery or some other local storage!
                            } else {
                                Toast toast = Toast.makeText(context, "Failed to get file info!", duration);
                                toast.show();
                            }
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(context, "File download failed!", duration);
                    toast.show();
                }
            }
        });
    }
}
