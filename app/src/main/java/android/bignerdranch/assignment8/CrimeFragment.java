package android.bignerdranch.assignment8;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {
    // request codes:
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_PHOTO2 = 3;

    // DatePickerFragment's tag:
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_PHOTO = "DialogPhoto";
    private Crime mCrime;           // a crime object reference.
    private EditText mTitleField;   // an EditText reference
    private Button mDateButton;     // a Button reference
    private Button mReportButton;   // a reference to the report crime button.
    private Button mSuspectButton;  // a reference to the pick suspect button.
    private CheckBox mSolvedCheckBox;   // CheckBox reference
    private ImageButton mPhotoButton;   // references for the photo and
    private ImageView mPhotoView;       //   camera button
    private ImageButton mPersonButton;
    private ImageView mPersonView;
    private File mPhotoFile;            // stores the location for the file
    private File mPhotoFile2;
    // an argument to add to the bundle
    private static final String ARG_CRIME_ID = "crime_id";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID)getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        mPhotoFile2 = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK)
            return;

        if(requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(
                    DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mDateButton.setText(mCrime.getDate().toString());
        }
        // handle the request for contact info
        else if(requestCode == REQUEST_CONTACT && data != null){
            Uri contactUri = data.getData();
            // we want the query to return values for these fields
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME
            };

            // do the query, the Uri is our "where" clause
            Cursor c = getActivity().getContentResolver().query(
                    contactUri, queryFields, null, null, null);

            try {
                if (c.getCount() == 0)
                    return;

                // get the field -- suspect's name
                c.moveToFirst();
                String suspect = c.getString(0);        // from column 0
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            }
            finally {
                c.close();
            }
        }
        //handle the request for taking a picture
        else if(requestCode == REQUEST_PHOTO) {
            updatePhotoView();

            // since we're done taking the picture now, revoke the permission
            // to write files
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "android.bignerdranch.assignment8.fileprovider",
                    mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        //handle the request for taking a picture
        else if(requestCode == REQUEST_PHOTO2) {
            updatePhotoView2();

            // since we're done taking the picture now, revoke the permission
            // to write files
            Uri uri2 = FileProvider.getUriForFile(getActivity(),
                    "android.bignerdranch.assignment8.fileprovider",
                    mPhotoFile2);
            getActivity().revokeUriPermission(uri2, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(
                R.layout.fragment_crime,    // layout resource id
                container,                  // the view's parent
                false);                     // view gets added in view activity's code.

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        // set the text...
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() { // set listener
            @Override
            public void beforeTextChanged(CharSequence s,
                                          int start,
                                          int count,
                                          int after) {
                // required to override this method, but we're not using it.
            }

            @Override
            public void onTextChanged(CharSequence s,
                                      int start,
                                      int before,
                                      int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // required to override this method, but we're not using it.
            }
        });

        mDateButton = (Button)v.findViewById(R.id.crime_date);
        mDateButton.setText(mCrime.getDate().toString());
        // mDateButton.setEnabled(false); <-- delete this
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                // replace this
                // DatePickerFragment dialog = new DatePickerFragment();
                // with this
                DatePickerFragment dialog =
                        DatePickerFragment.newInstance(mCrime.getDate());

                // set the target fragment:
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fm,DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        // update the check box...
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(
                            CompoundButton buttonView,
                            boolean isChecked) {
                        mCrime.setSolved(isChecked); } } );

        // listener for the report crime button
        mReportButton = (Button)v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        // make the intent
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        // get a reference for the suspect button
        mSuspectButton = (Button)v.findViewById(R.id.crime_suspect);

        // add a listener for the suspect button
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // start the activity, and request a result.
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        // if we got the result, update the button text.
        if(mCrime.getSuspect() != null)
            mSuspectButton.setText(mCrime.getSuspect());

        // get a reference to the Activity's PackageManager
        PackageManager pm = getActivity().getPackageManager();

        // search for an activity matching the intent we gave,
        // and match only activities with the CATEGORY_DEFAULT flag.
        // if it can't find a match, deactivate the button.
        if(pm.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null)
            mSuspectButton.setEnabled(false);

        // get the references for the widgets handling the photo and camera
        mPhotoButton = (ImageButton)v.findViewById(R.id.crime_camera);
        mPhotoView = (ImageView)v.findViewById(R.id.crime_photo);

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                PhotoDialogFragment dialog = PhotoDialogFragment.newInstance(mPhotoFile);
                dialog.show(fm, DIALOG_PHOTO);

            }
        });

        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(pm) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HANKSTER", "onClick Enter");
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "android.bignerdranch.assignment8.fileprovider",
                        mPhotoFile);
                Log.d("HANKSTER", "after Line 232");
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities =
                        getActivity().getPackageManager().queryIntentActivities(
                                captureImage, PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        updatePhotoView();

        mPersonButton = (ImageButton)v.findViewById(R.id.reporting_camera);
        mPersonView = (ImageView)v.findViewById(R.id.reporting_person);


        final Intent captureImage2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto2 = mPhotoFile2 != null && captureImage2.resolveActivity(pm) != null;
        mPersonButton.setEnabled(canTakePhoto2);
        mPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("HANKSTER", "onClick Enter");
                Uri uri2 = FileProvider.getUriForFile(getActivity(),
                        "android.bignerdranch.assignment8.fileprovider",
                        mPhotoFile2);

                captureImage2.putExtra(MediaStore.EXTRA_OUTPUT, uri2);
                List<ResolveInfo> cameraActivities2 =
                        getActivity().getPackageManager().queryIntentActivities(
                                captureImage2, PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity2 : cameraActivities2) {
                    getActivity().grantUriPermission(activity2.activityInfo.packageName,
                            uri2,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage2, REQUEST_PHOTO2);
            }
        });
        updatePhotoView2();
        return v;
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // helper method that generates the report
    private String getCrimeReport() {
        String solvedString = null;

        // each argument after the first in getString(...) replaces a place holder.
        if(mCrime.isSolved())
            solvedString = getString(R.string.crime_report_solved);
        else
            solvedString = getString(R.string.crime_report_unsolved);

        String dateFormat = "EEE, MMM, dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();

        if(suspect == null)
            suspect = getString(R.string.crime_report_nosuspect);
        else
            suspect = getString(R.string.crime_report_suspect, suspect);

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString,
                solvedString, suspect);

        return report;
    }

    private void updatePhotoView() {

        if(mPhotoFile == null || !mPhotoFile.exists())
            mPhotoView.setImageDrawable(null);
        else {
            Bitmap bm = PictureUtils.getScaledBitmap(
                    mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bm);
        }
    }


    private void updatePhotoView2() {

        if(mPhotoFile2 == null || !mPhotoFile2.exists())
            mPersonView.setImageDrawable(null);
        else {
            Bitmap bm2 = ReportingPicture.getScaledBitmap(
                    mPhotoFile2.getPath(), getActivity());
            mPersonView.setImageBitmap(bm2);
        }
    }
}

