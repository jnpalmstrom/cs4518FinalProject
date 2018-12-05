package wpi.epplevinsky.finalproject;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static Integer REQUEST_IMAGE_CAPTURE = 5;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

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

            mPictureView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
        }
    }
}
