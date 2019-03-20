package com.example.firebasestorage;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {


    private static final int Pick_image = 1;
    private static final int Capture_image = 2;
    ImageView user_img;
    Button upload;
    Uri image;
    Bitmap image_btm;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference reference;
    ProgressDialog dialog;
    EditText username, pass, email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        user_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlert();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //dialog.show();

                String emailText=email.getText().toString();
                String nameText=username.getText().toString();
                String passText=pass.getText().toString();
                authUser(nameText,emailText,passText);
                //uploadImage();
            }
        });
    }

    private void authUser(final String nameText, final String emailText, final String passText) {

        auth.createUserWithEmailAndPassword(emailText,passText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    reference = firebaseStorage.getReference("images/"+auth.getCurrentUser().getUid());
                    uploadImage(nameText,emailText,passText);
                }

            }
        });

    }

    private void uploadImage(final String name, final String email, final String pass) {

        BitmapDrawable drawable = (BitmapDrawable) user_img.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();

        reference.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String image = task.getResult().toString();

                            String uid=auth.getCurrentUser().getUid();
                            saveData(name,email,pass,image,uid);
                        }
                    }
                });

                dialog.dismiss();
                Toast.makeText(MainActivity.this, "Upload", Toast.LENGTH_SHORT).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                dialog.setMessage((int) progress + "% Uploaded");


            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveData(String name, String email, String pass,String image,String uid) {

        User user =new User(name,email,pass,image,uid);
        databaseReference.child(uid).setValue(user);
        startActivity(new Intent(this,ProfileActivity.class));

    }

    private void showAlert() {

        final String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Select ");
        dialog.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                if (options[i].equals("Gallery")) {
                    openGallery();
                } else if (options[i].equals("Camera")) {
                    openCamera();
                }

            }
        });
        dialog.show();
    }

    private void openCamera() {

        Intent camera = new Intent();
        camera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, Capture_image);
    }

    private void openGallery() {

        Intent gallery = new Intent();
        gallery.setAction(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery, "Choose Image"), Pick_image);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                image = data.getData();
                user_img.setImageURI(image);
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            if (data != null) {
                image_btm = (Bitmap) data.getExtras().get("data");
                user_img.setImageBitmap(image_btm);
            }
        }
    }

    private void init() {
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        user_img = findViewById(R.id.user_image);
        upload = findViewById(R.id.upload_img);

        database=FirebaseDatabase.getInstance();
        databaseReference=database.getReference("users");
        firebaseStorage = FirebaseStorage.getInstance();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading");
        dialog.setMessage("Please Wait...");



    }
}
