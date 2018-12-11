package wpi.epplevinsky.finalproject;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static Integer REQUEST_IMAGE_CAPTURE = 5;
    private String mCurrentPhotoPath;

    // Start: Variables for inference
    Bitmap image;
    long startTime;
    long endTime;

    int DIM_BATCH_SIZE = 1;
    int SIZE_X = 299;
    int SIZE_Y = 299;
    int DIM_PIXEL_SIZE = 3;
    int NUM_BYTES_PER_CHANNEL = 4;
    int IMAGE_MEAN = 128;
    float IMAGE_STD = 128;

    private MappedByteBuffer tfliteModel;
    ByteBuffer imgData = null; // Input image data
    private List<String> labelList;
    private float[][] labelProbArray = null;
    private String mostAccurateLabel;
    // End: Variables for inference

    private String hosturl = "http://35.243.243.163:54321/inception";
    private MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    ////////////////////////////////////////////////////////////////////////////////////
    // Code for Menu Starts Here
    ////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home_item) {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    // Code for Menu ends here

    ////////////////////////////////////////////////////////////////////////////////////
    // Code for Taking Picture Starts Here
    ////////////////////////////////////////////////////////////////////////////////////
    public void onTakePictureButton(View v) {
        File tempFileDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File tempFile = new File("./");
        try {
            tempFile = File.createTempFile("pic", ".jpg", tempFileDir);
            mCurrentPhotoPath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), "wpi.epplevinsky.finalproject.fileprovider", tempFile);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(takePictureIntent, 0);
        if(activities.size() != 0) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == this.REQUEST_IMAGE_CAPTURE) {
            ImageView mPictureView = (ImageView) findViewById(R.id.pictureView);
            TextView mLabelView = (TextView) findViewById(R.id.labelText);

            Bitmap mBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            Bitmap scaledBitMap = Bitmap.createScaledBitmap(mBitmap, 800, 800, false);
            image = scaledBitMap;

            mPictureView.setImageBitmap(image);
        }
    }
    // Code for taking picture ends

    /////////////////////////////////////////////////////////////////////////////////
    // Running ML inference code
    /////////////////////////////////////////////////////////////////////////////////
    private void runML(Bitmap bitmap) {

        try {
            /////////////////////////////////////////
            // Step 1: Setting up our model
            /////////////////////////////////////////
            tfliteModel = loadModelFile();
            Interpreter tflite = new Interpreter(tfliteModel);

            /////////////////////////////////////////
            // Step 2: Setting up our image data
            /////////////////////////////////////////
            // Step 2.2: Convert Bitmap to Direct ByteBuffer
            /////////////////////////////////////////
            imgData = ByteBuffer.allocateDirect(
                    DIM_BATCH_SIZE
                            * SIZE_X
                            * SIZE_Y
                            * DIM_PIXEL_SIZE
                            * NUM_BYTES_PER_CHANNEL);
            imgData.order(ByteOrder.nativeOrder());
            convertBitmapToByteBuffer(bitmap);

            /////////////////////////////////////////
            // Step 3: Setting up our response array
            /////////////////////////////////////////
            labelList = loadLabelList();
            labelProbArray = new float[DIM_BATCH_SIZE][getNumLabels()];
            /////////////////////////////////////////
            // Step 4: Running Inference
            /////////////////////////////////////////
            //startTime = SystemClock.uptimeMillis();
            tflite.run(imgData, labelProbArray);
            //endTime = SystemClock.uptimeMillis();
            /////////////////////////////////////////
            // Step 5: Processing Results
            /////////////////////////////////////////
            mostAccurateLabel = setHighestProbability();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads MappedByteBuffer for the model
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Provides path to model in assets directory
    private String getModelPath() {
        return "model/inception_v3.tflite";
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        int[] intValues = new int[SIZE_X * SIZE_Y];
        if (imgData == null) { return; }

        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < SIZE_X; ++i) {
            for (int j = 0; j < SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                addPixelValue(val);
            }
        }
    }
    protected void addPixelValue(int pixelValue) {
        imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }

    // Reads label list from Assets
    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(getAssets().open(getLabelPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    // Provides path to labels file in assets directory
    private String getLabelPath() {
        return "model/labels.txt";
    }

    private int getNumLabels() {
        return labelList.size();
    }

    private String setHighestProbability() {
        float tempHigh = 0.0f;
        int highestIndex = 0;
        for (int i = 0; i < getNumLabels(); i++) {
            if (labelProbArray[0][i] >= tempHigh) {
                tempHigh = labelProbArray[0][i];
                highestIndex = i;
            }
        }
        float highestProb = tempHigh * 100;
        String highestProbStr = String.format("%.2f", highestProb);

        return labelList.get(highestIndex) + ": " + highestProbStr + "%";
    }

    public void onClickButtonGo(View view) {
        new RunInferenceAsyncOnDevice().execute(mCurrentPhotoPath);
    }

    ////////////////////////////////////
    // Async for onDevice
    ////////////////////////////////////
    private class RunInferenceAsyncOnDevice extends AsyncTask<String, Float, Long> {
        long time;
        String filename;
        Bitmap mBitmap;

        protected void onPreExecute() {
            // Stuff to do before inference starts
            time = SystemClock.uptimeMillis();
        }

        protected Long doInBackground(String... img_files) {
            filename = img_files[0];
            mBitmap = BitmapFactory.decodeFile(filename);
            mBitmap = Bitmap.createScaledBitmap(mBitmap, 299, 299, false);
            image = mBitmap;
            runML(mBitmap);
            return null;
        }

        protected void onPostExecute(Long result){
            // Stuff to do after inference ends
            endTime = SystemClock.uptimeMillis();
            long latency = endTime-time;

            TextView labelView = (TextView) findViewById(R.id.labelText);
            labelView.setText(mostAccurateLabel + " " + latency + "ms");
        }
    }

    ////////////////////////////////////////
    // Running Inference off device code
    ////////////////////////////////////////
    public void runOffDevice(View view) throws IOException {
        new RunInferenceAsync().execute(mCurrentPhotoPath);
    }

    private class RunInferenceAsync extends AsyncTask<String, Float, Long> {
        String results;
        long time;
        String filename;

        protected void onPreExecute() {
            // Stuff to do before inference starts
            time = SystemClock.uptimeMillis();
        }

        protected Long doInBackground(String... img_files) {
            filename = img_files[0];
            // Do inference here!
            File file = new File(filename);

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", filename, RequestBody.create(MEDIA_TYPE_JPEG, file))
                    .build();

            Request request = new Request.Builder()
                    .url(hosturl)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            long code = 0;

            try {
                Response response = client.newCall(request).execute();
                results = response.body().string();
                code = (long) response.code();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return code;
        }

        protected void onPostExecute(Long result){
            // Stuff to do after inference ends
            endTime = SystemClock.uptimeMillis();
            long latency = endTime-time;

            TextView labelView = (TextView) findViewById(R.id.labelText);
            labelView.setText(results + " " + latency + "ms");
        }
    }
}
