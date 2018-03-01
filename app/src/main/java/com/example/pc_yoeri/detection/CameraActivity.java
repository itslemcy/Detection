package com.example.pc_yoeri.detection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kairos.Kairos;
import com.kairos.KairosListener;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class CameraActivity extends AppCompatActivity {

    private ImageView mImageView;
    private TextView mDetails;
    private EditText mEmailField;

    private static final int CAMERA_REQUEST_CODE = 1;

    private String key;
    private StorageReference mStorage;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private Kairos myKairos;
    private KairosListener listener;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setting the title
        toolbar.setTitle("Face Epicness");
        //placing toolbar in place of actionbar
        setSupportActionBar(toolbar);

        // instantiate a new kairos instance
        myKairos = new Kairos();

        // set authentication
        String app_id = "";
        String api_key = "";
        myKairos.setAuthentication(this, app_id, api_key);

        // Create an instance of the KairosListener
        listener = makeKairosListener();

        //Create a firebase instance
        mStorage = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();

    }

    //Inflating the menu
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //Make menu items clickable
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menuLogout:
                Toast.makeText(this, "You clicked logout", Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            //Set imageview to the camerapicture
            mImageView = (ImageView) findViewById(R.id.imageView);
            mImageView.setImageBitmap(imageBitmap);

            //Generate key for Storage + Realtime database
            key = database.getReference().push().getKey();

            StorageReference filepath = mStorage.child("Photos").child(key);

            // Get the data from an ImageView as bytes
            mImageView.setDrawingCacheEnabled(true);
            mImageView.buildDrawingCache();
            Bitmap bitmap = mImageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data2 = baos.toByteArray();

            UploadTask uploadTask = filepath.putBytes(data2);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                }
            });

            // Kairos - Take the Bitmap and get facedata (Face recognition magic)
            String selector = "FULL";
            String minHeadScale = "0.25";
            try {
                myKairos.detect(imageBitmap, selector, minHeadScale, listener);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    }

    public KairosListener makeKairosListener(){
        KairosListener kListener = new KairosListener() {

            @Override
            public void onSuccess(String response) {
                Log.d("KAIROS:", response);
                mDetails = (TextView) findViewById(R.id.details);
                mDetails.setText(response);

                myRef = database.getReference("/Users/" + "User_01" + "/PictureData/" + key);
                myRef.setValue(response);

                mEmailField = (EditText) findViewById(R.id.EmailField);
                String email = mEmailField.getText().toString();
                myRef = database.getReference("/Users/" + "User_01" + "/Email");
                myRef.setValue(email);
            }

            @Override
            public void onFail(String response) {
                Log.d("KAIROS:", response);
            }
        };
        return kListener;
    }

    public void uploadClick(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }
}
