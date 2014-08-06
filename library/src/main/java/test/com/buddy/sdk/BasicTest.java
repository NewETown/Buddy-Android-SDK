
package test.com.buddy.sdk;


import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.buddy.sdk.Buddy;
import com.buddy.sdk.BuddyCallback;
import com.buddy.sdk.BuddyClient;
import com.buddy.sdk.BuddyClientOptions;
import com.buddy.sdk.models.Picture;
import com.buddy.sdk.models.User;
import com.buddy.sdk.BuddyFile;
import com.buddy.sdk.BuddyResult;
import junit.framework.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BasicTest extends InstrumentationTestCase {

    private static final String TargetUrl = null;
    private static final String AppId = "\your app id";
    private static final String AppKey = "\your app key";

    private BuddyClient getClient() {
        return getClient(AppId, AppKey, true);
    }

    private BuddyClient getClient(String appid, String appkey, boolean syncMode) {

        BuddyClientOptions options = new BuddyClientOptions();
        options.synchronousMode = syncMode;
        options.serviceRoot = TargetUrl;

        BuddyClient client = Buddy.init(null, appid == null ? "appid" : appid, appkey == null ? "appkey" : appkey, options);

        return client;
    }

    public void testPingSuccess() throws Exception {
        BuddyClient client = getClient();

        Future<BuddyResult<String>> handle = client.get("/service/ping", null, String.class);

        BuddyResult<String> result = handle.get();

        assertNotNull(result);
        assertNull(result.getError());
        assertEquals("Pong", result.getResult());
    }

    public void testPingFail() throws Exception {

        BuddyClient client = getClient(null, null, true);

        Future<BuddyResult<String>> handle = client.get("/service/ping", null, String.class);

        BuddyResult<String> result = handle.get();
        assertNotNull(result);
        assertEquals("AuthAppCredentialsInvalid", result.getError());
        assertEquals(null, result.getResult());

    }


    public void testCreateUser() throws Exception {
        BuddyClient client = getClient();

        String newUser = String.format(Locale.getDefault(), "%s-%d", "testuser-", new Date().getTime());

        Future<BuddyResult<User>> handle = client.createUser(newUser, "password", null, null, null, null, null, null, null);

        BuddyResult<User> result = handle.get();
        assertNotNull(result);
        assertNull(result.getError());
        assertEquals(newUser, result.getResult().userName);

        Future<BuddyResult<User>> handle2 = client.getCurrentUser(null);

        result = handle2.get();
        assertEquals(newUser, result.getResult().userName);

    }

    public void testLoginUser() throws Exception {

        BuddyClient client = getClient();

        Future<BuddyResult<User>> handle = client.loginUser("shawn", "password", null);

        BuddyResult<User> result = handle.get();
        assertNotNull(result);
        assertNull(result.getError());
        assertEquals("shawn", result.getResult().userName);


    }

    public void testPatchDevice() throws Exception {


        BuddyClient client = getClient();


        Future<BuddyResult<Boolean>> handle = client.setPushToken("BPA91bF0Th8nRgxfhdENtWLyFWmAh9jZ3DZzIUtXvb7Z2yXpH-7B2H59BlDNy7ZigxcJS1V5rezbUFAyZreIQWaQz3MfJ61CmfDwK-cH9-1DaOQl3Kx0iptGWjZr1e5AxbYFMeHzFjI-kGCr6nrLUNeCEFkNXgnX101p0v-TmKDGguN6JXqWMAc", null);
        BuddyResult<Boolean> result = handle.get();

        assertNotNull(result);
        assertNull(result.getError());
        assertTrue(result.getResult());

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

    public void testRunInThread() throws Exception {

        final BuddyClient client = getClient(AppId, AppKey, false);

        AsyncTask<Void,Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Future promise = client.getCurrentUser(null);
                try {
                    promise.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return true;
            }
        };

        task.execute();

    }


    public void testUploadFile() throws Exception{

        BuddyClient client = getClient();

        Future handle = client.loginUser("shawn", "password", null);

        handle.get();

        Map<String, Object> parameters = new HashMap<String,Object>();


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

        parameters.put("caption", "From Android");
        parameters.put("data", new BuddyFile(is, "image/png"));


        Future<BuddyResult<Picture>> handle2 = client.<Picture>post("/pictures", parameters, Picture.class);

        handle2.get();

        assertNotNull(handle2.get().getResult());
        assertEquals("From Android", handle2.get().getResult().caption);

        // now get the file.
        //
        parameters.clear();
        Future<BuddyResult<BuddyFile>> handle3 = client.<BuddyFile>get(String.format("/pictures/%s/file", handle2.get().getResult().id), null, BuddyFile.class);

        handle3.get();

        assertNotNull(handle3.get());

        BuddyFile file = handle3.get().getResult();

        assertNotNull(file);
        assertNotNull(file.getStream());
        assertEquals("image/png", file.getContentType());
        InputStream fileStream = file.getStream();

        assertEquals(bytes.length, fileStream.available());

        byte[] buffer = new byte[fileStream.available()];
        fileStream.read(buffer, 0, buffer.length);

        for (int i= 0; i < buffer.length; i++) {
            if (buffer[i] != bytes[i]) {
                Assert.fail("Bytes not equal at " + i);
            }

        }

    }
}