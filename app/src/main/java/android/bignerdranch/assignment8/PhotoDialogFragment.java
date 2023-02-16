package android.bignerdranch.assignment8;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.io.File;

public class PhotoDialogFragment extends DialogFragment {
    private ImageView mPhotoView;
    //private File mPhotoFile;
    private static final String ARG_PHOTO_FILE = "photoFile";
    public static final String EXTRA_PHOTO = "android.bignerdranch.assignment8.photo";

    public static PhotoDialogFragment newInstance(File path) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHOTO_FILE, path);

        PhotoDialogFragment fragment = new PhotoDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        File mPhotoFile = (File) getArguments().getSerializable(ARG_PHOTO_FILE);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_fragment, null);

        mPhotoView = (ImageView)v.findViewById(R.id.crime_photo);

        if(mPhotoFile == null || !mPhotoFile.exists())
            mPhotoView.setImageDrawable(null);
        else {
            Bitmap bm = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bm);
        }


        return new AlertDialog.Builder(getActivity())
                .setView(v)

                .create();
    }





}
