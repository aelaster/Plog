package com.lastsoft.plog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.kbeanie.imagechooser.api.BChooserPreferences;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Location;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.lastsoft.plog.db.PlaysPerGameGroup;
import com.lastsoft.plog.util.DeletePlayTask;
import com.lastsoft.plog.util.LoadExpansionsTask;
import com.lastsoft.plog.util.PostPlayTask;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddPlayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddPlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPlayFragment extends Fragment implements
        ImageChooserListener, GoogleApiClient.ConnectionCallbacks
         {
    Activity mActivity;
    String mCurrentPhotoPath = "";
    String mCurrentPhotoName = "";
    Location locationId = null;
    String locationName = "";
    private OnFragmentInteractionListener mListener;
    int cx, cy;
    File f;
    ArrayList<Long> addedUsers;
    ArrayList<AddPlayer> arrayOfUsers;
    AddPlayerAdapter adapter;
    String gameName;
    long playID;
    long copyPlayID = -1;
    TextView textViewDate, textViewLocation;
    View expansionButton;
    static boolean[] checkedItems;
    ArrayList<Integer> playersID;
    ArrayList<String> playersName;
    static String gameDate;
    static ArrayList<Game> addedExpansions;
    ImageView playPhoto;
    Uri photoUri;
    File photoFile;
    ImageChooserManager imageChooserManager;
    ArrayAdapter<CharSequence> colorSpinnerArrayAdapter;
    boolean savedThis = false;
    boolean copyPlay = false;
    SharedPreferences app_preferences;

    private ViewGroup mContainerView_Players;
    private ViewGroup mContainerView_Expansions;


    List<Game> expansions;

     int PLACE_PICKER_REQUEST = 2;
     public GoogleApiClient mGoogleApiClient;
     android.location.Location lastKnownLocation;

    public static AddPlayFragment newInstance(int centerX, int centerY, boolean doAccelerate, String mGameName, long playID, boolean copyPlay) {
        AddPlayFragment fragment = new AddPlayFragment();
        Bundle args = new Bundle();
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        args.putString("gameName", mGameName);
        args.putBoolean("doAccelerate", doAccelerate);
        args.putLong("playID", playID);
        args.putBoolean("copyPlay", copyPlay);
        fragment.setArguments(args);
        return fragment;
    }

    public AddPlayFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gameName = getArguments().getString("gameName");
            playID = getArguments().getLong("playID");
            copyPlay = getArguments().getBoolean("copyPlay");
            if (copyPlay){
                copyPlayID = playID;
            }
        }
        app_preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .build();

        colorSpinnerArrayAdapter = ArrayAdapter.createFromResource(mActivity, R.array.color_choices, android.R.layout.simple_spinner_item);

        addedUsers = new ArrayList<>();
        addedExpansions = new ArrayList<>();
        List<Player> players = Player.listPlayersAZ(0);
        playersName = new ArrayList<>();
        playersID = new ArrayList<>();
        for(Player player:players){
            playersName.add(player.playerName);
            playersID.add(player.getId().intValue());
        }

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        if (String.valueOf(month+1).length()==1) {
            gameDate = year + "-0" + (month + 1) + "-" + day;
        }else{
            gameDate = year + "-" + (month + 1) + "-" + day;
        }
        setHasOptionsMenu(true);
    }


    View rootView;
    EditText notesText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_add_play, container, false);
        rootView.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));

        if (mActivity instanceof ViewPlayActivity) {
            Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setTitle(gameName);
            toolbar.setNavigationIcon(R.drawable.ic_action_back);
            ((ViewPlayActivity) mActivity).setSupportActionBar(toolbar);
        }

        ExpansionsLoader myExpansions = new ExpansionsLoader(mActivity);
        try {
            myExpansions.execute(Game.findGameByName(gameName).gameBGGID);
        } catch (Exception ignored) {

        }

        mContainerView_Players = (ViewGroup) rootView.findViewById(R.id.container_players);
        mContainerView_Expansions = (ViewGroup) rootView.findViewById(R.id.container_expansions);



        View addButton = rootView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPlayer newUser = new AddPlayer(-1, "", "", 0);
                adapter.add(newUser);
                adapter.notifyDataSetChanged();
                addPlayer(newUser);
            }
        });

        notesText = (EditText) rootView.findViewById(R.id.notesText);
        notesText.setSelectAllOnFocus(true);

        expansionButton = rootView.findViewById(R.id.addGameButton);


        playPhoto = (ImageView) rootView.findViewById(R.id.gamePhoto);
        playPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mCurrentPhotoPath.equals("")) {
                    File deleteImage = new File(mCurrentPhotoPath);
                    if (deleteImage.exists()) {
                        deleteImage.delete();
                    }

                    //delete play image thumb
                    File deleteImage_thumb = new File(mCurrentPhotoPath.substring(0, mCurrentPhotoPath.length() - 4) + "_thumb6.jpg");
                    if (deleteImage_thumb.exists()) {
                        deleteImage_thumb.delete();
                    }

                    mCurrentPhotoPath = "";
                    mCurrentPhotoName = "";
                    playPhoto.setImageDrawable(null);
                }
            }
        });
        View photoButton = rootView.findViewById(R.id.takePhoto);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager inputManager = (InputMethodManager)
                            mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception ignored) {
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                    // Create the File where the photo should go
                    photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        ex.printStackTrace();

                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        photoUri = Uri.fromFile(photoFile);
                        Log.d("V1", "photoUri = " + photoUri);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                photoUri);
                        startActivityForResult(takePictureIntent, 0);
                    }else{
                        Log.d("V1", "photofile is null");
                    }
                }
            }
        });


        View galleryButton = rootView.findViewById(R.id.choosePhoto);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        View dateButton = rootView.findViewById(R.id.datePicker);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = new DatePickerFragment();
                if (mActivity instanceof MainActivity) {
                    newFragment.show(((MainActivity) mActivity).getSupportFragmentManager(), "datePicker");
                } else {
                    newFragment.show(((ViewPlayActivity) mActivity).getSupportFragmentManager(), "datePicker");
                }
            }
        });

        View locationButton = rootView.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoogleApiClient.isConnected()) {
                    //first, check for last known latlng
                    Location lastKnown = Location.findLocationByLatLng(round(lastKnownLocation.getLatitude(), 4), round(lastKnownLocation.getLongitude(), 4));
                    if (lastKnown != null){
                        LocationDialogFragment newFragment = new LocationDialogFragment(null, lastKnown).newInstance(3, null, lastKnown);
                        if (mActivity instanceof MainActivity) {
                            newFragment.show(((MainActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                        } else {
                            newFragment.show(((ViewPlayActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                        }
                    }else {
                        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                                .getCurrentPlace(mGoogleApiClient, null);
                        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                            @Override
                            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                                Place foundPlace = null;
                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    Log.i("V1", String.format("Place '%s' with " +
                                                    "likelihood: %g",
                                            placeLikelihood.getPlace().getName(),
                                            placeLikelihood.getLikelihood()));
                                    if (Location.findLocationByAddress(placeLikelihood.getPlace().getAddress().toString()) != null) {
                                        foundPlace = placeLikelihood.getPlace().freeze();
                                        break;
                                    } else if (Location.findLocationByPlaceID(placeLikelihood.getPlace().getId().toString()) != null) {
                                        foundPlace = placeLikelihood.getPlace().freeze();
                                        break;
                                    } else if (Location.findLocationByName(placeLikelihood.getPlace().getName().toString()) != null) {
                                        foundPlace = placeLikelihood.getPlace().freeze();
                                        break;
                                    }
                                }
                                if (foundPlace != null) {
                                    //we found a saved location that is equal to a spot where we are at
                                    //confirm with user
                                    LocationDialogFragment newFragment = new LocationDialogFragment(foundPlace, null).newInstance(0, foundPlace, null);
                                    if (mActivity instanceof MainActivity) {
                                        newFragment.show(((MainActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                                    } else {
                                        newFragment.show(((ViewPlayActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                                    }
                                } else {
                                    //we did not find a location, so we should ask the user to pick one
                                    List<Location> locations = Location.listAll(Location.class);
                                    if (locations.size() == 0) {
                                        //there are no saved locations, so go right into place picker
                                        openPlacePicker();
                                    } else {
                                        //there are saved locations, so pop dialog, asking them to choose one
                                        LocationDialogFragment newFragment = new LocationDialogFragment(null, null).newInstance(1, null, null);
                                        if (mActivity instanceof MainActivity) {
                                            newFragment.show(((MainActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                                        } else {
                                            newFragment.show(((ViewPlayActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                                        }
                                    }
                                }
                                likelyPlaces.release();
                            }
                        });
                    }
                }else{
                    Log.d("V1", "not connected");
                }
            }
        });

        textViewDate = (TextView) rootView.findViewById(R.id.textViewDate);
        textViewLocation = (TextView) rootView.findViewById(R.id.locationName);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = dateFormat.parse(gameDate);
            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
            String output_date = outputFormatter.format(date1); // Output : 01/20/2012
            textViewDate.setText(output_date);
        } catch (ParseException ignored) {
        }

        // Construct the data source
        arrayOfUsers = new ArrayList<>();
        // Create the adapter to convert the array to views
        adapter = new AddPlayerAdapter(mActivity, arrayOfUsers);

        if (playID >= 0) {
            //we're editing a play
            Play editPlay = Play.findById(Play.class, playID);

            //set up the values, based on DB
            if (!copyPlay) {
                //date
                DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
                String output_date = outputFormatter.format(editPlay.playDate); // Output : 01/20/2012
                textViewDate.setText(output_date);
                DateFormat inputUser = new SimpleDateFormat("yyyy-MM-dd");
                gameDate = inputUser.format(editPlay.playDate);

                //photo
                final DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .cacheOnDisk(false)
                        .cacheInMemory(false)
                        .considerExifParams(true)
                        .build();

                mCurrentPhotoName = editPlay.playPhoto;
                mCurrentPhotoPath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES) + "/Plog/" + mCurrentPhotoName;

                ImageLoader.getInstance().displayImage("file://" + mCurrentPhotoPath, playPhoto, options);

            }

            if (editPlay.playLocation != null) {
                Location useMe = Location.findById(Location.class, editPlay.playLocation.getId());
                if (useMe != null) {
                    locationId = useMe;
                    textViewLocation.setText(useMe.locationName);
                }
            }

            //players
            List<PlayersPerPlay> players = PlayersPerPlay.getPlayers(editPlay);
            for (PlayersPerPlay player : players) {
                Player thisPlayer = player.player;
                AddPlayer addedPlayer;
                if (!copyPlay) {
                     addedPlayer = new AddPlayer(thisPlayer.getId(), thisPlayer.playerName, player.color, player.score); //id, name, color, score]
                }else{
                    addedPlayer = new AddPlayer(thisPlayer.getId(), thisPlayer.playerName, player.color, 0); //id, name, color, score]
                }
                addPlayer(addedPlayer);
                addedUsers.add(thisPlayer.getId());
                adapter.add(addedPlayer);
            }
            adapter.notifyDataSetChanged();

            //note
            notesText.setText(editPlay.playNotes);
        }else{
            //set last used location
            if (app_preferences.getBoolean("last_used_location", true) == true) {
                long lastLocation = app_preferences.getLong("lastUsedLocation", 0);
                if (lastLocation > 0) {
                    Location useMe = Location.findById(Location.class, lastLocation);
                    if (useMe != null) {
                        locationId = useMe;
                        textViewLocation.setText(useMe.locationName);
                    }
                }
            }
        }
        if (copyPlay){
            playID = -1;
        }

        return rootView;
    }

    private void chooseImage() {
        try{
            InputMethodManager inputManager = (InputMethodManager)
                    mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        BChooserPreferences prefs = new BChooserPreferences(mActivity);
        prefs.setFolderName("Pictures/Plog");

        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, false);
        imageChooserManager.setImageChooserListener(this);


        try {
            imageChooserManager.choose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addGame(Game game){
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                R.layout.play_showexpansions_item, mContainerView_Expansions, false);

        TextView gameName = (TextView) newView.findViewById(R.id.gameName);
        gameName.setText(game.gameName);
        mContainerView_Expansions.addView(newView);
    }

    private void clearGames(){
        mContainerView_Expansions.removeAllViews();
    }


    private void addPlayer(final AddPlayer addedPlayer) {
        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                R.layout.play_addplayer_item, mContainerView_Players, false);
        Spinner player = (Spinner) newView.findViewById(R.id.player);
        ArrayAdapter<String> playerSpinnerArrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, playersName);
        playerSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        player.setAdapter(playerSpinnerArrayAdapter);
        if (!addedPlayer.playerName.equals("")) {
            int spinnerPostion = playerSpinnerArrayAdapter.getPosition(addedPlayer.playerName);
            player.setSelection(spinnerPostion);
        }




        EditText scoreValue = (EditText) newView.findViewById(R.id.score);
        scoreValue.addTextChangedListener(new MyTextWatcher(player, addedPlayer));
        scoreValue.setSelectAllOnFocus(true);
        NumberFormat nf = new DecimalFormat("###.#");
        scoreValue.setText("" + nf.format(addedPlayer.score));


        Spinner color = (Spinner) newView.findViewById(R.id.color);

        colorSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        color.setAdapter(colorSpinnerArrayAdapter);

        boolean overwriteFlag = true;
        if (!addedPlayer.color.equals("")) {
            int spinnerPostion = colorSpinnerArrayAdapter.getPosition(addedPlayer.color);
            color.setSelection(spinnerPostion);
            overwriteFlag = false;
        }

        //the player spinner needs to pass reference to the color spinner
        MySpinnerListener playerListener = new MySpinnerListener(addedPlayer, color, overwriteFlag);
        player.setOnItemSelectedListener(playerListener);
        //the color spinner does not
        MySpinnerListener colorListener = new MySpinnerListener(addedPlayer, null, overwriteFlag);
        color.setOnItemSelectedListener(colorListener);



        // Set a click listener for the "X" button in the row that will remove the row.
        newView.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the row from its parent (the container view).
                // Because mContainerView has android:animateLayoutChanges set to true,
                // this removal is automatically animated.
                adapter.remove(addedPlayer);
                addedUsers.remove(addedPlayer.playerID);
                mContainerView_Players.removeView(newView);

                // If there are no rows remaining, show the empty view.
            }
        });

        // Because mContainerView has android:animateLayoutChanges set to true,
        // adding this view is automatically animated.
        mContainerView_Players.addView(newView);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "PLAY_" + timeStamp + "_";
        String imageFileName = fileName;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File plogDir = new File(storageDir, "/Plog/");
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                plogDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoName = image.getName();
        mCurrentPhotoPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/Plog/" + mCurrentPhotoName;
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("V1", "requestCode=" + requestCode);
        Log.d("V1", "resultCode=" + resultCode);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(false)
                .cacheInMemory(false)
                .considerExifParams(true)
                .build();
        if (requestCode == 0 && resultCode == 0) {
            mCurrentPhotoName = "";
        }else if (requestCode == 0 && resultCode == -1) {
            makeAThumb();
            ImageLoader.getInstance().displayImage("file://" + mCurrentPhotoPath, playPhoto, options);

        }else if (resultCode == -1 && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            imageChooserManager.submit(requestCode, data);
        }else if (requestCode == 2) {
            if (resultCode == -1) {
                // this is the place that the user selected
                Place place = PlacePicker.getPlace(data, mActivity);

                if (!place.getAddress().toString().contains(place.getName().toString()) && !place.getName().toString().contains("°")){
                    //address does not contain place name, which means the place name is set, so no confirmation is needed
                    //check to see if it already exists
                    Location checker = Location.findLocationByName(place.getName().toString());
                    if (checker != null){
                        //we found a location with this name already, so we'll just use it
                        //set this as the location for this play
                        locationId = checker;
                        textViewLocation.setText(checker.locationName);
                    }else {
                        Location addMe = new Location(place.getName().toString(), place.getAddress().toString(), place.getId(), place.getLatLng().latitude, place.getLatLng().longitude);
                        addMe.save();
                        //set this as the location for this play
                        locationId = addMe;
                        textViewLocation.setText(addMe.locationName);
                    }
                }else{
                    //address contains place name, meaning the place name is just an address
                    Location checker = Location.findLocationByAddress(place.getName().toString());
                    if (checker != null){
                        //we found a location with this address already, so we'll just use it
                        //set this as the location for this play
                        locationId = checker;
                        textViewLocation.setText(checker.locationName);
                    }else {
                        //we need a place name added
                        LocationDialogFragment newFragment = new LocationDialogFragment(place, null).newInstance(2, place, null);
                        if (mActivity instanceof MainActivity) {
                            newFragment.show(((MainActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                        } else {
                            newFragment.show(((ViewPlayActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                        }
                    }
                }
            }
        }
    }


    private void makeAThumb(){
        try {
            String thumbPath = mCurrentPhotoPath.substring(0, mCurrentPhotoPath.length() - 4) + "_thumb6.jpg";
            FileInputStream fis;
            fis = new FileInputStream(mCurrentPhotoPath);
            Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
            Bitmap b = resizeImageForImageView(imageBitmap, 500);
            if (b != null) {
                try {
                    b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(thumbPath)));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap resizeImageForImageView(Bitmap bitmap, int size) {
        Bitmap resizedBitmap;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor;
        if(originalHeight > originalWidth) {
            newHeight = size;
            multFactor = (float) originalWidth/(float) originalHeight;
            newWidth = (int) (newHeight*multFactor);
        } else if(originalWidth > originalHeight) {
            newWidth = size;
            multFactor = (float) originalHeight/ (float)originalWidth;
            newHeight = (int) (newWidth*multFactor);
        } else if(originalHeight == originalWidth) {
            newHeight = size;
            newWidth = size;
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if(playID >= 0){
            inflater.inflate(R.menu.edit_play, menu);
        }else {
            inflater.inflate(R.menu.add_play, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        try{
            InputMethodManager inputManager = (InputMethodManager)
                    mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception ignored){}

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                mActivity.onBackPressed();
                return true;
            case R.id.add_play:
                adapter.notifyDataSetChanged();
                if (adapter.getCount()>0) {
                    //if location id isnt blank, save it for default action
                    if (locationId != null){
                        SharedPreferences.Editor editor;
                        editor = app_preferences.edit();
                        editor.putLong("lastUsedLocation", locationId.getId());
                        editor.commit();
                    }

                    //first, add the play
                    try {
                        Play thePlay;

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date1 = dateFormat.parse(gameDate);
                        if (playID>=0) {
                            Play savePlay = Play.findById(Play.class, playID);
                            savePlay.playDate = date1;
                            savePlay.playNotes = notesText.getText().toString();
                            savePlay.playPhoto = mCurrentPhotoName;
                            savePlay.playLocation = locationId;
                            savePlay.save();
                            thePlay = savePlay;
                        }else{
                            Play newPlay = new Play(date1, notesText.getText().toString(), mCurrentPhotoName, locationId);
                            newPlay.save();
                            thePlay = newPlay;
                        }

                        //then add the players to the play
                        if (playID>=0) {
                            //delete old playaz before adding the new ones
                            List<PlayersPerPlay> playaz = PlayersPerPlay.getPlayers(thePlay);
                            for(PlayersPerPlay player:playaz){
                                player.delete();
                            }
                        }

                        float highScore = -99999;
                        float lowScore = 99999;
                        for (int i = 0; i < adapter.getCount(); i++) {
                            AddPlayer thisGuy = adapter.getItem(i);
                            if (thisGuy.score > highScore){
                                highScore = thisGuy.score;
                            }
                            if (thisGuy.score < lowScore){
                                lowScore = thisGuy.score;
                            }

                        }

                        for (int i = 0; i < adapter.getCount(); i++) {
                            AddPlayer thisGuy = adapter.getItem(i);
                            PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, thisGuy.playerID), thePlay, thisGuy.score, thisGuy.color, highScore, lowScore);
                            newPlayer.save();
                        }



                        //then add the games to the play
                        if (playID>=0) {
                            //otherwise, delete the old expansions
                            List<GamesPerPlay> gamez = GamesPerPlay.getExpansions(thePlay);
                            for(GamesPerPlay game:gamez){
                                if (game.expansionFlag == true){
                                    if (game.bggPlayId != null && !game.bggPlayId.equals("")){
                                        DeletePlayTask deletePlay = new DeletePlayTask(getActivity());
                                        try {
                                            deletePlay.execute(game.bggPlayId);
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                                game.delete();
                            }
                        }else{//not a play yet, add the base game
                            Game theGame = Game.findGameByName(gameName);
                            if (theGame != null) {
                                GamesPerPlay newBaseGame = new GamesPerPlay(thePlay, theGame, false);
                                newBaseGame.save();
                            }
                        }

                        for (int i = 0; i < addedExpansions.size(); i++) {
                            Game addedExpansion = addedExpansions.get(i);
                            GamesPerPlay newExpansion = new GamesPerPlay(thePlay, addedExpansion, true);
                            newExpansion.save();
                        }

                        //lastly, check existing groups
                        //if a group has played this game, add it to PlaysPerGameGroup
                        if (playID>=0) {
                            //delete the existing plays_per_game_group
                            //delete plays_per_game_group
                            List<PlaysPerGameGroup> plays = PlaysPerGameGroup.getPlays(Play.findById(Play.class, playID));
                            for(PlaysPerGameGroup play:plays){
                                play.delete();
                            }
                        }

                        List<GameGroup> gameGroups = GameGroup.listAll(GameGroup.class);
                        for (GameGroup thisGroup:gameGroups){
                            List<Player> players =  GameGroup.getGroupPlayers(thisGroup);
                            boolean included = true;
                            for (Player playa:players){
                                if (!addedUsers.contains(playa.getId())){
                                    included = false;
                                    break;
                                }
                            }
                            if (included){
                                //add this to PlaysPerGameGroup
                                PlaysPerGameGroup newGroupPlay = new PlaysPerGameGroup(thePlay, thisGroup);
                                newGroupPlay.save();
                            }
                        }

                        //remove from bucket list if it's there
                        //only do this if there are more than one players...or the remove solo plays setting is enabled
                        if (adapter.getCount() > 1 || app_preferences.getBoolean("solo_remove_bucket_list", true) == true){
                            Game baseGame = Game.findGameByName(gameName);
                            if (baseGame != null && baseGame.taggedToPlay > 0) {
                                baseGame.taggedToPlay = 0;
                                baseGame.save();
                                if (mActivity instanceof MainActivity) {
                                    onButtonPressed("refresh_games");
                                }
                            }
                        }

                        savedThis = true;

                        long currentDefaultPlayer = app_preferences.getLong("defaultPlayer", -1);
                        if (currentDefaultPlayer >= 0) {
                            Player defaultPlayer = Player.findById(Player.class, currentDefaultPlayer);
                            //post this to BGG
                            PlayPoster postPlay = new PlayPoster(getActivity(), defaultPlayer.bggUsername);
                            try {
                                postPlay.execute(thePlay);
                            } catch (Exception e) {

                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (mActivity instanceof MainActivity) {
                        onButtonPressed("refresh_plays");
                        onButtonPressed("refresh_games");
                    }
                    //}
                    mActivity.onBackPressed();
                }else{
                    ((MainActivity) mActivity).notifyUser(0);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


     @Override
     public void onStop() {
         super.onStop();
         mGoogleApiClient.disconnect();
     }


     @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        if (mActivity != null) {
            if (mActivity instanceof MainActivity) {
                ((MainActivity) mActivity).setTitle(gameName);
                ((MainActivity) mActivity).setUpActionBar(3);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (!savedThis && playID < 0){
            if (mCurrentPhotoPath.length() > 0) {
                String thumbPath = mCurrentPhotoPath.substring(0, mCurrentPhotoPath.length() - 4) + "_thumb6.jpg";

                File deleteImage = new File(mCurrentPhotoPath);
                if (deleteImage.exists()) {
                    deleteImage.delete();
                }

                File deleteImage_thumb = new File(thumbPath);
                if (deleteImage_thumb.exists()) {
                    deleteImage_thumb.delete();
                }
            }
        }

        mListener = null;
        if (mActivity != null) {
            if (mActivity instanceof MainActivity) {
                if (((MainActivity) mActivity).mPlaysFragment != null) {
                    if (((MainActivity) mActivity).forceBack) {
                        ((MainActivity) mActivity).setUpActionBar(10);
                    }else{
                        ((MainActivity) mActivity).setUpActionBar(6);
                    }
                } else {
                    if (((MainActivity) mActivity).mGamesFragment != null) {
                        if(((MainActivity) mActivity).mGamesFragment.getQuery().equals("BucketList")) {
                            ((MainActivity) mActivity).setUpActionBar(12);
                        }else{
                            ((MainActivity) mActivity).setUpActionBar(4);
                        }
                    }
                }
            }else{
                Log.d("V1", "mActivity isn't main");
            }
            mActivity = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).unbindDrawables(rootView);
        }
    }

     @Override
     public void onConnected(Bundle bundle) {
         lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                 mGoogleApiClient);
     }


     @Override
     public void onConnectionSuspended(int i) {

     }

     public class PlayPoster extends PostPlayTask {
        public PlayPoster(Context context, String bggUsername) {
            super(context, bggUsername);
        }
    }

    @Override
    public void onImageChosen(final ChosenImage chosenImage) {
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (chosenImage != null) {
                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                            .cacheOnDisk(false)
                            .cacheInMemory(false)
                            .considerExifParams(true)
                            .build();

                    try {
                        String[] splitMe = chosenImage.getFilePathOriginal().split("/");
                        mCurrentPhotoName = splitMe[splitMe.length-1];
                        mCurrentPhotoPath = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES) + "/Plog/" + mCurrentPhotoName;
                        makeAThumb();
                        ImageLoader.getInstance().displayImage("file://" + mCurrentPhotoPath, playPhoto, options);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Save a file: path for use with ACTION_VIEW intents

                }
            }
        });

    }

    @Override
    public void onError(String s) {
        //Log.d("V1", "error =" + s);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String string);
    }

    public class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = dateFormat.parse(gameDate);

                final Calendar c = Calendar.getInstance();
                c.setTime(date1);
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                // Create a new instance of DatePickerDialog and return it
                return new DatePickerDialog(mActivity, this, year, month, day);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;

        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            if (String.valueOf(month+1).length()==1) {
                gameDate = year + "-0" + (month + 1) + "-" + day;
            }else{
                gameDate = year + "-" + (month + 1) + "-" + day;
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date1 = dateFormat.parse(gameDate);
                DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
                String output_date = outputFormatter.format(date1); // Output : 01/20/2012
                textViewDate.setText(output_date);
            }catch (ParseException ignored) {}
        }
    }

    /*
    Executes fragment removal animation and removes the fragment from view.
     */
    public void removeYourself(){
        try {
            final AddPlayFragment mfragment = this;
            getFragmentManager().popBackStack();
            getFragmentManager().beginTransaction().remove(mfragment).commitAllowingStateLoss();
            getFragmentManager().executePendingTransactions(); //Prevents the flashing.
        }catch (Exception ignored){}
    }

    public class AddPlayer {
        public long playerID;
        public String playerName;
        public String color;
        public float score;

        public AddPlayer(long playerID, String playerName, String color, float score) {
            this.playerID = playerID;
            this.playerName = playerName;
            this.color = color;
            this.score = score;
        }
    }

    public class AddPlayerAdapter extends ArrayAdapter<AddPlayer> {
        public AddPlayerAdapter(Context context, ArrayList<AddPlayer> users) {
            super(context, 0, users);
        }
    }

    public class MySpinnerListener implements AdapterView.OnItemSelectedListener{

        private AddPlayer playerToUpdate;
        private Spinner colorSpinner;
        private boolean overwriteFlag;

        public MySpinnerListener(AddPlayer thePlayer, Spinner spinner, boolean overwriteFlag) {
            this.playerToUpdate = thePlayer;
            this.colorSpinner = spinner;
            this.overwriteFlag = overwriteFlag;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(colorSpinner == null) {//color
                playerToUpdate.color = adapterView.getSelectedItem().toString();
            }else {//player
                addedUsers.remove(playerToUpdate.playerID);
                playerToUpdate.playerID = playersID.get(i);
                playerToUpdate.playerName = playersName.get(i);
                addedUsers.add(playerToUpdate.playerID);
                if (overwriteFlag) {
                    Player colorCheck = Player.findById(Player.class, playerToUpdate.playerID);
                    if (colorCheck.defaultColor != null && !colorCheck.defaultColor.equals("")) {
                        int spinnerPostion = colorSpinnerArrayAdapter.getPosition(colorCheck.defaultColor);
                        colorSpinner.setSelection(spinnerPostion);
                        playerToUpdate.color = colorSpinner.getSelectedItem().toString();
                    }
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public class MyTextWatcher implements TextWatcher {

        private AddPlayer playerToUpdate;
        private Spinner playerSpinner;

        public MyTextWatcher(Spinner spinner, AddPlayer thePlayer) {
            this.playerToUpdate = thePlayer;
            this.playerSpinner = spinner;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (playerSpinner.getSelectedItem().toString().equals(playerToUpdate.playerName)) {
                String score = editable.toString().trim();
                if (!score.equals("")) {
                    if (!score.equals(".") && !score.equals("-") && !score.equals("-.")) {
                        playerToUpdate.score = Float.parseFloat(score);
                    }
                }
            }
        }
    }

    public class ExpansionsLoader extends LoadExpansionsTask {
        public ExpansionsLoader(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(final List<Game> result) {
            try {
                expansions = result;
                if (expansions != null) {
                    checkedItems = new boolean[expansions.size()];
                    if (expansions.size() > 0) {
                        mContainerView_Expansions.setVisibility(View.VISIBLE);
                        ViewGroup expLayout = (ViewGroup) rootView.findViewById(R.id.expansionsLayout);
                        expLayout.setVisibility(View.VISIBLE);
                        expansionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CheckBoxAlertDialogFragment newFragment = new CheckBoxAlertDialogFragment().newInstance(checkedItems, gameName);
                                if (mActivity instanceof MainActivity) {
                                    newFragment.show(((MainActivity) mActivity).getSupportFragmentManager(), "datePicker");
                                } else {
                                    newFragment.show(((ViewPlayActivity) mActivity).getSupportFragmentManager(), "datePicker");
                                }
                            }
                        });
                        if (playID >= 0 || copyPlayID >= 0) {
                            long useMe;
                            if (playID >= 0) {
                                useMe = playID;
                            } else {
                                useMe = copyPlayID;
                            }
                            //we're editing a play

                            Play editPlay = Play.findById(Play.class, useMe);
                            //set up the values, based on DB

                            //expansions
                            //for each expansion the game has
                            int x = 0;
                            for (Game expansion0 : expansions) {
                                //if the added expansions contains it
                                if (GamesPerPlay.doesExpansionExist(editPlay, expansion0)) {
                                    //add to added expansions
                                    addedExpansions.add(expansion0);
                                    //add the game to the list
                                    addGame(expansion0);
                                    //check it
                                    checkedItems[x] = true;
                                } else {
                                    //don't check it
                                    checkedItems[x] = false;
                                }
                                x++;
                            }
                        }
                    }
                }
            }catch (Exception ignored){}
        }
    }

    public class CheckBoxAlertDialogFragment extends DialogFragment {



        public CheckBoxAlertDialogFragment newInstance(boolean[] checkedItems, String gameName) {
            CheckBoxAlertDialogFragment frag = new CheckBoxAlertDialogFragment();
            Bundle args = new Bundle();
            args.putBooleanArray("checkedItem", checkedItems);
            args.putString("gameName", gameName);

            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String gameName = getArguments().getString("gameName");

            List<String> expansionNames = new ArrayList<>();
            for(Game expansion:expansions){
                expansionNames.add(expansion.gameName);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            // Set the dialog title
            builder.setTitle(R.string.choose_expansions)
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setMultiChoiceItems(expansionNames.toArray(new CharSequence[expansionNames.size()]), checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                                    boolean isChecked) {
                                checkedItems[which] = isChecked;
                                Game checked = expansions.get(which);
                                if (isChecked){
                                    addedExpansions.add(checked);
                                }else{
                                    addedExpansions.remove(checked);
                                }
                                }
                            })
                            // Set the action buttons
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            clearGames();
                            // User clicked OK, so save the checkedItems results somewhere
                            // or return them to the component that opened the dialog
                            for (int i = 0; i < addedExpansions.size(); i++){
                                Game addMe = addedExpansions.get(i);
                                addGame(addMe);
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            return builder.create();
        }
    }

     public class LocationDialogFragment extends DialogFragment {
         @Override
         public void onPause() {
             super.onPause();
             try {
                 InputMethodManager inputManager = (InputMethodManager)
                         mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                 inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                         InputMethodManager.HIDE_NOT_ALWAYS);
             } catch (Exception e) {
             }
         }

         ArrayList<GameGroup> addedGroups;
         ArrayList<CharSequence> theYears;
         Place thePlace;
         Location theLocation;
         View inflator = null;

         public LocationDialogFragment(Place place, Location location) {
             super();
             this.thePlace = place;
             this.theLocation = location;
         }

         public LocationDialogFragment newInstance(int dialogType, Place thePlace, Location theLocation) {
             LocationDialogFragment frag = new LocationDialogFragment(thePlace, theLocation);
             Bundle args = new Bundle();
             args.putInt("dialogType", dialogType);
             frag.setArguments(args);
             return frag;
         }

         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             final int dialogType = getArguments().getInt("dialogType");
             /*
             0 = confirm found place
             1 = list of saved locations, with add new on top
             2 = set place name
             3 = confirm found location
              */
             AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
             if (dialogType == 0 || dialogType == 3){
                 if (dialogType == 0) {
                     builder.setTitle(getString(R.string.are_you_at) + thePlace.getName().toString() + "?");
                 }else{
                     builder.setTitle(getString(R.string.are_you_at) + theLocation.locationName + "?");
                 }
                 builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         //not there, offer them the location chooser
                         dialog.dismiss();
                         LocationDialogFragment newFragment = new LocationDialogFragment(null, null).newInstance(1, null, null);
                         if (mActivity instanceof MainActivity) {
                             newFragment.show(((MainActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                         }else{
                             newFragment.show(((ViewPlayActivity) mActivity).getSupportFragmentManager(), "locationPicker");
                         }
                     }
                 })
                 .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {

                     }
                 });
             }else if (dialogType == 1){
                 List<Location> theLocations = Location.getAllLocations();
                 final List<String> locations = new ArrayList<>();
                 for (Location aLocation:theLocations){
                     locations.add(aLocation.locationName);
                 }
                 locations.add(getString(R.string.add_new));
                 locations.add(getString(R.string.none));
                 builder.setTitle(R.string.choose_location)
                     .setItems(locations.toArray(new CharSequence[locations.size()]), new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int item) {
                             dialog.dismiss();
                             if (item == locations.size()-1) {
                                 //setting blank
                                 locationId = null;
                                 textViewLocation.setText("");
                             }else if (item == locations.size()-2) {
                                 //trying to add a new one
                                 openPlacePicker();
                             } else {
                                 //selected a location

                                 //set the location to be output to the screen
                                 Location useMe = Location.findLocationByName(locations.get(item));
                                 locationId = useMe;
                                 textViewLocation.setText(useMe.locationName);
                                 //save the location in a variable, to be saved to the play
                             }
                         }
                     })
                     .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int id) {
                         }
                     });
             }else if (dialogType == 2){
                 //we have to set a place name here
                 LayoutInflater inflater = getActivity().getLayoutInflater();
                 inflator = inflater.inflate(R.layout.dialog_location_name, null);
                 builder.setView(inflator)
                     // Add action buttons
                     .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int id) {


                         }
                     })
                     .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             try {
                                 InputMethodManager inputManager = (InputMethodManager)
                                         mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                                 inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                                         InputMethodManager.HIDE_NOT_ALWAYS);
                             } catch (Exception e) {
                             }
                             LocationDialogFragment.this.getDialog().dismiss();
                         }
                     });
                 //then we will save it as the name for the location
                 //then we will use it as the location for this play
             }

             final AlertDialog ad = builder.create();
             ad.setOnShowListener(new DialogInterface.OnShowListener()
             {
                 @Override
                 public void onShow(DialogInterface dialog)
                 {
                     final ListView lv = ad.getListView(); //this is a ListView with your "buds" in it
                     if (lv != null) {
                         lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                             @Override
                             public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                 String selectedFromList = (String) (lv.getItemAtPosition(position));
                                 if (!selectedFromList.equals(getString(R.string.add_new)) && !selectedFromList.equals(getString(R.string.add_new))) {
                                     Location useMe = Location.findLocationByName(selectedFromList);
                                     useMe.delete();
                                 }
                                 LocationDialogFragment.this.getDialog().dismiss();
                                 return true;
                             }
                         });
                     }
                 }
             });
             return ad;
         }

         @Override
         public void onStart()
         {
             final int dialogType = getArguments().getInt("dialogType");
             super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
             AlertDialog d = (AlertDialog)getDialog();
             if(d != null)
             {
                 Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
                 positiveButton.setOnClickListener(new View.OnClickListener()
                 {
                     @Override
                     public void onClick(View v)
                     {
                         Boolean wantToCloseDialog = false;

                         if (dialogType == 0 || dialogType == 3){
                             //set this
                             LocationDialogFragment.this.getDialog().dismiss();
                             Location useMe;
                             if (dialogType == 0) {
                                 useMe = Location.findLocationByName(thePlace.getName().toString());
                             }else{
                                 useMe = theLocation;
                             }
                             locationId = useMe;
                             textViewLocation.setText(useMe.locationName);
                         }else if (dialogType == 2){
                             EditText edit = (EditText) inflator.findViewById(R.id.locationName);
                             String text = edit.getText().toString();

                             //check to see if this name already exists
                             Location checker = Location.findLocationByName(text);
                             if (checker != null) {
                                 //this name already exists.  toast the user and let them try it again
                                 Toast.makeText(mActivity, getString(R.string.location_name_exists), Toast.LENGTH_LONG).show();
                                 edit.selectAll();
                             }else{
                                 try {
                                     InputMethodManager inputManager = (InputMethodManager)
                                             mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                                     inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                                             InputMethodManager.HIDE_NOT_ALWAYS);
                                 } catch (Exception e) {
                                 }
                                 LocationDialogFragment.this.getDialog().dismiss();
                                 //then we will save it as the name for the new location
                                 Location addMe = new Location(text, thePlace.getAddress().toString(), thePlace.getId(), thePlace.getLatLng().latitude, thePlace.getLatLng().longitude);
                                 addMe.save();
                                 //then we will use it as the location for this play
                                 locationId = addMe;
                                 textViewLocation.setText(addMe.locationName);
                                 //set the location to be output to the screen
                             }


                         }

                         if(wantToCloseDialog)
                             dismiss();
                     }
                 });
             }
         }

     }

    public void openPlacePicker(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(mActivity), PLACE_PICKER_REQUEST);
        }catch (Exception ignored){

        }
    }

     public static double round(double value, int places) {
         if (places < 0) throw new IllegalArgumentException();

         BigDecimal bd = new BigDecimal(value);
         bd = bd.setScale(places, RoundingMode.HALF_UP);
         return bd.doubleValue();
     }
}
