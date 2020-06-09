package com.hussnain.socialmediaapp.NavFragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hussnain.socialmediaapp.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private ImageView coverPic;
    private CircularImageView profilePic;
    private TextView profileName, profileEmail, profilePhone;
    FloatingActionButton fab;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private StorageReference storageReference;
    private String storagePath = "users_profile_cover_imgs/";
    private ProgressDialog progressDialog;

    //for permissions
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //for picking image from gallery or camera
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri uri;

    private String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");
        storageReference = getInstance().getReference();

//        init permissions
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        profilePic = view.findViewById(R.id.profilePicID);
        coverPic = view.findViewById(R.id.ID_coverImage);
        profileName = view.findViewById(R.id.userNameID);
        profileEmail = view.findViewById(R.id.userEmailID);
        profilePhone = view.findViewById(R.id.userPhoneID);
        progressDialog = new ProgressDialog(getActivity());
        fab = view.findViewById(R.id.ID_edit);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editProfileAction();

            }
        });

        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    try {
                        Glide.with(getContext()).load(image).into(profilePic);

                    } catch (Exception e) {
                        Glide.with(getContext())
                                .load(R.drawable.ic_addimage)
                                .into(profilePic);
                    }

                    try {
                        Glide.with(getContext()).load(cover).into(coverPic);

                    } catch (Exception e) {

                        Glide.with(getContext())
                                .load(R.drawable.ic_broken_image)
                                .into(coverPic);
                    }

                    profileName.setText(name);
                    profileEmail.setText(email);
                    profilePhone.setText(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result2 = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result2 && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void editProfileAction() {

        String[] options = {"Edit Profile picture", "Edit Cover Photo", "Edit Name", "Edit Phone"};

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        builder.setTitle("Choose Action");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int option) {

                if (option == 0) {
                    //Edit profile picture
                    progressDialog.setMessage("Updating profile picture");
                    profileOrCoverPhoto = "image";
                    showImgPicDialog();
                } else if (option == 1) {
                    //Edit cover picture
                    progressDialog.setMessage("Updating cover photo");
                    profileOrCoverPhoto = "cover";
                    showImgPicDialog();

                } else if (option == 2) {
                    //Edit Name
                    progressDialog.setMessage("Updating Name");
                    editNamePhoneUpDateDialog("name");

                } else if (option == 3) {
                    //Edit Phone
                    progressDialog.setMessage("Updating phone");
                    editNamePhoneUpDateDialog("phone");

                }
            }
        });
        builder.create().show();

    }

    private void editNamePhoneUpDateDialog(final String key) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 20, 10, 10);

        final EditText text = new EditText(getActivity());
        text.setHint("Enter " + key);
        linearLayout.addView(text);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String newData = text.getText().toString().trim();

                if (!TextUtils.isEmpty(newData)) {

                    progressDialog.show();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(key, newData);

                    reference.child(user.getUid()).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else
                    Toast.makeText(getActivity(), "Please enter" + key, Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("Cencel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();


    }

    private void showImgPicDialog() {

        String options[] = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        builder.setTitle("Pick Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int option) {

                if (option == 0) {

                    progressDialog.setMessage("Updating photo");

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                }

                if (option == 1) {
                    //Edit picture
                    progressDialog.setMessage("Updating photo");

                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGellery();
                    }

                }

            }
        });
        builder.create().show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    if (cameraAccepted && storageAccepted) {
                pickFromCamera();

                Toast.makeText(getActivity(), "Permission granted..", Toast.LENGTH_SHORT).show();

            } else Toast.makeText(getContext(),
                    "Please Enable Camera & Storage Permission", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                  if (storageAccepted) {
                pickFromGellery();
                Toast.makeText(getActivity(), "Permission granted..", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getContext(), "Please Enable Storage Permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFromGellery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == IMAGE_PICK_CAMERA_CODE) {

            uri=data.getData();
            uploadProfileCoverPhoto(uri);

        }
        if (requestCode == IMAGE_PICK_GALLERY_CODE) {
            uri = data.getData();
            uploadProfileCoverPhoto(uri);
        }
    }


    private void uploadProfileCoverPhoto(Uri uri) {

        progressDialog.show();

        String filePathAndName = storagePath + " " + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);

        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful()) ;
                        Uri downloadedUri = uriTask.getResult();

                        if (uriTask.isSuccessful()) {

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put(profileOrCoverPhoto, downloadedUri.toString());

                            reference.child(user.getUid()).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            progressDialog.dismiss();
                                            Toast.makeText(getContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else
                            Toast.makeText(getContext(), "Some Error Orrured", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }
}