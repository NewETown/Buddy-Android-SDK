package com.buddy.buddysampleapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddy.buddysampleapp.util.AppImage;
import com.buddy.buddysampleapp.util.AppImageAdapter;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyFile;
import com.buddy.sdk.BuddyResult;
import com.buddy.sdk.models.Picture;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileActivity extends Activity {

    ArrayList<AppImage> mFileIdList;
    ListView mFileList;
    TextView mNoPicturesText;
    AppImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        mFileIdList = new ArrayList<AppImage>();

        mNoPicturesText = (TextView)findViewById(R.id.text_no_pictures);

        // Initialize the button UI elements
        Button btnUploadFile = (Button)findViewById(R.id.btn_submit_file);
        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        mFileList = (ListView)findViewById(R.id.list_available_files);
        mAdapter = new AppImageAdapter(getApplicationContext(), R.layout.list, mFileIdList);
        mFileList.setAdapter(mAdapter);
        mFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String fileId = mFileIdList.get(i).getId();
                downloadFile(fileId);
            }
        });

        Button btnGetFileList = (Button)findViewById(R.id.btn_get_file_list);
        btnGetFileList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFileList();
            }
        });

        // Populate the file list when the activity starts up
        getFileList();
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

        // Grab the Buddy logo from resources
        Bitmap logo = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.logo);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        logo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        InputStream is = new ByteArrayInputStream(bytes);

        // Grab the description from the user
        final EditText fileDescription = (EditText)findViewById(R.id.edit_enter_caption);

        // Set up the toasts
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        Map<String, Object> parameters = new HashMap<String,Object>();
        parameters.put("caption", fileDescription.getText().toString());
        parameters.put("data", new BuddyFile(is, "image/png"));
        Buddy.<Picture>post("/pictures", parameters, new BuddyCallback<Picture>(Picture.class){
            @Override
            public void completed(BuddyResult<Picture> result) {
                if (result.getIsSuccess()) {
                    Toast toast = Toast.makeText(context, "Upload successful!", duration);
                    toast.show();
                    fileDescription.setText("");
                    // Add any new files to the list without querying again
                    Log.w("PICTURE_ID", result.getResult().id);
                    mFileIdList.add(new AppImage(result.getResult().id.replace("\"",""),
                                                 result.getResult().caption));
                    mAdapter.notifyDataSetChanged();
                } else {
                    Toast toast = Toast.makeText(context, "Error: " + result.getError(), duration);
                    toast.show();
                }
            }
        });
    }

    public void downloadFile(String fileId) {
        // Get the UI elements we will use
        final TextView fileCaption = (TextView)findViewById(R.id.text_file_caption);
        final String fId = fileId;

        // Set up the toasts
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        Buddy.get("/pictures/"+fId+"/file", null, new BuddyCallback<BuddyFile>(BuddyFile.class) {
            @Override
            public void completed(BuddyResult<BuddyFile> result) {
                if (result.getIsSuccess()) {
                    Buddy.get("/pictures/"+fId, null, new BuddyCallback<Picture>(Picture.class) {
                        @Override
                        public void completed(BuddyResult<Picture> resultPicture) {
                            if (resultPicture.getIsSuccess()) {
                                // Display a toast, and display the caption
                                fileCaption.setText("Caption: " + resultPicture.getResult().caption);
                                // At this point you could get the image data from resultFile then store or present it on the user's device
                                Toast toast = Toast.makeText(context, "File download success!", duration);
                                toast.show();
                                // This would be a good place to store the image in the gallery or some other local storage!
                            } else {
                                Toast toast = Toast.makeText(context, String.format("Error: %i", resultPicture.getErrorCode()), duration);
                                toast.show();
                            }
                        }
                    });
                } else {
                    Toast toast = Toast.makeText(context, String.format("Error: %i", result.getErrorCode()), duration);
                    toast.show();
                }
            }
        });
    }

    private void getFileList() {
        // Load the list of files for the current Buddy application

        // Clear out the list
        mFileIdList.clear();

        // Set up the toasts
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;

        // Call get on '/pictures' to retrieve all uploaded files
        Buddy.get("/pictures", null, new BuddyCallback<JsonObject>(JsonObject.class) {

            @Override
            public void completed(BuddyResult<JsonObject> result) {
                if (result.getIsSuccess()) {
                    // Add the images to the result array
                    JsonObject obj = result.getResult();
                    JsonArray resultArray = obj.getAsJsonArray("pageResults");
                    // Add items to the list
                    if (resultArray.size() == 0) {
                        mFileList.setVisibility(View.GONE);
                        // No pictures? Let the user know!
                        mNoPicturesText.setVisibility(View.VISIBLE);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        for (int i = 0; i < resultArray.size(); i++) {
                            JsonObject picture = (JsonObject)resultArray.get(i);
                            mFileIdList.add(new AppImage(picture.get("id").toString().replace("\"", ""),
                                                         picture.get("caption").getAsString())
                                                        );
                        }
                        mNoPicturesText.setVisibility(View.GONE);
                        mFileList.setVisibility(View.VISIBLE);
                        mAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast toast = Toast.makeText(context, String.format("Error: %i", result.getErrorCode()), duration);
                    toast.show();
                }
            }
        });
    }
}
