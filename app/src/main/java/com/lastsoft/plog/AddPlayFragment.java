package com.lastsoft.plog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
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
        ImageChooserListener {
    String mCurrentPhotoPath = "";
    private OnFragmentInteractionListener mListener;
    int cx, cy;
    File f;
    ArrayList<AddPlayer> arrayOfUsers;
    AddPlayerAdapter adapter;
    String gameName;
    long playID;
    TextView textViewDate;

    private ViewGroup mContainerView_Players;
    private ViewGroup mContainerView_Expansions;

    List<Game> expansions;

    // TODO: Rename and change types and number of parameters
    public static AddPlayFragment newInstance(int centerX, int centerY, boolean doAccelerate, String mGameName, long playID) {
        AddPlayFragment fragment = new AddPlayFragment();
        Bundle args = new Bundle();
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        args.putString("gameName", mGameName);
        args.putBoolean("doAccelerate", doAccelerate);
        args.putLong("playID", playID);
        fragment.setArguments(args);
        return fragment;
    }

    public AddPlayFragment() {
        // Required empty public constructor
    }


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gameName = getArguments().getString("gameName");
            playID = getArguments().getLong("playID");
        }

        colorSpinnerArrayAdapter = ArrayAdapter.createFromResource(mActivity, R.array.color_choices, android.R.layout.simple_spinner_item);


        //ActionBar actionBar = ((MainActivity)mActivity).getSupportActionBar();
        //actionBar.setDisplayShowCustomEnabled(false);

        //expansions = Game.findExpansionsFor(gameName);
        //checkedItems = new boolean[expansions.size()];

        addedExpansions = new ArrayList<>();
        List<Player> players = Player.listPlayersAZ();
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

        try {
            expansions = Game.findExpansionsFor(gameName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        checkedItems = new boolean[expansions.size()];

        mContainerView_Players = (ViewGroup) rootView.findViewById(R.id.container_players);
        mContainerView_Expansions = (ViewGroup) rootView.findViewById(R.id.container_expansions);

        View addButton = rootView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //listItems.add("Clicked : "+clickCounter++);
                AddPlayer newUser = new AddPlayer(-1, "", "", 0);
                adapter.add(newUser);
                adapter.notifyDataSetChanged();
                addPlayer(newUser);
            }
        });

        notesText = (EditText) rootView.findViewById(R.id.notesText);
        notesText.setSelectAllOnFocus(true);

        View expansionButton = rootView.findViewById(R.id.addGameButton);
        if (expansions.size()==0) {
            mContainerView_Expansions.setVisibility(View.GONE);
            mContainerView_Expansions.setVisibility(View.GONE);
            ViewGroup expLayout = (ViewGroup) rootView.findViewById(R.id.expansionsLayout);
            expLayout.setVisibility(View.GONE);
        }else{
            expansionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBoxAlertDialogFragment newFragment = new CheckBoxAlertDialogFragment().newInstance(checkedItems, gameName);
                    newFragment.show(((MainActivity)mActivity).getSupportFragmentManager(), "datePicker");
                }
            });
        }

        playPhoto = (ImageView) rootView.findViewById(R.id.gamePhoto);
        playPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentPhotoPath = "";
                playPhoto.setImageDrawable(null);
            }
        });
        View photoButton = rootView.findViewById(R.id.takePhoto);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                    // Create the File where the photo should go
                    photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File

                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        photoUri = Uri.fromFile(photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                photoUri);
                        startActivityForResult(takePictureIntent, 0);
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
                newFragment.show(((MainActivity)mActivity).getSupportFragmentManager(), "datePicker");
            }
        });

        textViewDate = (TextView) rootView.findViewById(R.id.textViewDate);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = dateFormat.parse(gameDate);
            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
            String output_date = outputFormatter.format(date1); // Output : 01/20/2012
            textViewDate.setText(output_date);
        }catch (ParseException ignored) {}

        // Construct the data source
        arrayOfUsers = new ArrayList<>();
        // Create the adapter to convert the array to views
        adapter = new AddPlayerAdapter(mActivity, arrayOfUsers);
        //expansionAdapter = new ExpansionsAdapter(mActivity, addedExpansions);
        //ListView listView2 = (ListView) rootView.findViewById(R.id.addGameList);
        //listView2.setAdapter(expansionAdapter);



        if (playID >= 0){
            //we're editing a play
            //Log.d("V1", "editing a play");
            Play editPlay = Play.findById(Play.class, playID);
            //set up the values, based on DB

            //expansions
            //for each expansion the game has
            int x = 0;
            for(Game expansion0:expansions){
                //if the added expansions contains it
                if (GamesPerPlay.doesExpansionExist(editPlay, expansion0)) {
                    //add to added expansions
                    addedExpansions.add(expansion0);
                    //add the game to the list
                    addGame(expansion0);
                    //check it
                    checkedItems[x] = true;
                }else{
                    //don't check it
                    checkedItems[x] = false;
                }
                x++;
            }

            //date
            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
            String output_date = outputFormatter.format(editPlay.playDate); // Output : 01/20/2012
            textViewDate.setText(output_date);
            DateFormat inputUser = new SimpleDateFormat("yyyy-MM-dd");
            gameDate = inputUser.format(editPlay.playDate);

            //photo
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .considerExifParams(true)
                    .build();
            ImageLoader.getInstance().displayImage(editPlay.playPhoto, playPhoto, options);
            mCurrentPhotoPath = editPlay.playPhoto;

            //players
            List<PlayersPerPlay> players = PlayersPerPlay.getPlayers(editPlay);
            for(PlayersPerPlay player:players){
                Player thisPlayer = player.player;
                //Log.d("V1", "score = " + player.score);
                AddPlayer addedPlayer = new AddPlayer(thisPlayer.getId(),thisPlayer.playerName,player.color, player.score); //id, name, color, score
                addPlayer(addedPlayer);
                adapter.add(addedPlayer);
            }
            adapter.notifyDataSetChanged();

            //note
            notesText.setText(editPlay.playNotes);
        }

        return rootView;
    }

    private void chooseImage() {
        int chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        try {
            String filePath = imageChooserManager.choose();
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
        //if (addedPlayer.score != -9999999) {
            scoreValue.setText(""+addedPlayer.score);
        //}


        Spinner color = (Spinner) newView.findViewById(R.id.color);

        colorSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        color.setAdapter(colorSpinnerArrayAdapter);
        if (!addedPlayer.color.equals("")) {
            int spinnerPostion = colorSpinnerArrayAdapter.getPosition(addedPlayer.color);
            color.setSelection(spinnerPostion);
        }
        //the player spinner needs to pass reference to the color spinner
        MySpinnerListener playerListener = new MySpinnerListener(addedPlayer, color);
        player.setOnItemSelectedListener(playerListener);
        //the color spinner does not
        MySpinnerListener colorListener = new MySpinnerListener(addedPlayer, null);
        color.setOnItemSelectedListener(colorListener);

        // Set a click listener for the "X" button in the row that will remove the row.
        newView.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the row from its parent (the container view).
                // Because mContainerView has android:animateLayoutChanges set to true,
                // this removal is automatically animated.
                adapter.remove(addedPlayer);
                mContainerView_Players.removeView(newView);

                // If there are no rows remaining, show the empty view.
                /*if (mContainerView.getChildCount() == 0) {
                    findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                }*/
            }
        });

        // Because mContainerView has android:animateLayoutChanges set to true,
        // adding this view is automatically animated.
        mContainerView_Players.addView(newView);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PLAY_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file://" + image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .considerExifParams(true)
                .build();
        if (requestCode == 0 && resultCode == -1) {
            ImageLoader.getInstance().displayImage(mCurrentPhotoPath, playPhoto, options);




            try {
                String fixedPath = mCurrentPhotoPath.substring(6, mCurrentPhotoPath.length());
                String thumbPath = fixedPath.substring(0, fixedPath.length() - 4) + "_thumb.jpg";
                FileInputStream fis;
                fis = new FileInputStream(fixedPath);
                Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
                Bitmap b = resizeImageForImageView(imageBitmap, 500);
                if (b != null) {
                    try {
                        b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(thumbPath)));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                    }
                    b = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

    }else if (resultCode == -1 && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            imageChooserManager.submit(requestCode, data);
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
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        if(playID >= 0 ){
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
        if (id == R.id.add_play) {
            adapter.notifyDataSetChanged();
            if (adapter.getCount()>0) {
                //first, add the play
                try {
                    Play thePlay;

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date1 = dateFormat.parse(gameDate);
                    if (playID>=0) {
                        Play savePlay = Play.findById(Play.class, playID);
                        savePlay.playDate = date1;
                        savePlay.playNotes = notesText.getText().toString();
                        savePlay.playPhoto = mCurrentPhotoPath;
                        savePlay.save();
                        thePlay = savePlay;
                    }else{
                        Play newPlay = new Play(date1, notesText.getText().toString(), mCurrentPhotoPath);
                        newPlay.save();
                        thePlay = newPlay;
                    }
                    //Log.d("V1","game date=" + gameDate);
                    //Log.d("V1", "game notes=" + notesText.getText().toString());

                    //then add the players to the play
                    if (playID>=0) {
                        //delete old playaz before adding the new ones
                        List<PlayersPerPlay> playaz = PlayersPerPlay.getPlayers(thePlay);
                        for(PlayersPerPlay player:playaz){
                            player.delete();
                        }
                    }

                    int highScore = -99999;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        AddPlayer thisGuy = adapter.getItem(i);
                        if (thisGuy.score > highScore){
                            highScore = thisGuy.score;
                        }

                    }

                    for (int i = 0; i < adapter.getCount(); i++) {
                        AddPlayer thisGuy = adapter.getItem(i);
                        //if (thisGuy.score != -9999999) {
                            PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, thisGuy.playerID), thePlay, thisGuy.score, thisGuy.color, highScore);
                            newPlayer.save();
                        //}
                    }



                    //then add the games to the play
                    if (playID>=0) {
                        //otherwise, delete the old expansions
                        List<GamesPerPlay> gamez = GamesPerPlay.getExpansions(thePlay);
                        for(GamesPerPlay game:gamez){
                            game.delete();
                        }
                    }else{//not a play yet, add the base game
                        //Log.d("V1", "game name= " + gameName);
                        GamesPerPlay newBaseGame = new GamesPerPlay(thePlay, Game.findGameByName(gameName), false);
                        newBaseGame.save();
                    }
                    //Log.d("V1", "game name= " + gameName);

                    for (int i = 0; i < addedExpansions.size(); i++) {
                        Game addedExpansion = addedExpansions.get(i);
                        GamesPerPlay newExpansion = new GamesPerPlay(thePlay, addedExpansion, true);
                        newExpansion.save();
                        //Log.d("V1", "Added Expansions = " + addedExpansion.gameName);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            //removeYourself();
            /*if (playID<0) {
                //((MainActivity) mActivity).getSupportActionBar().setDisplayShowCustomEnabled(true);
            }else{*/
                onButtonPressed("refresh_plays");
            //}
            mActivity.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
        }
    }

    Activity mActivity;
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
    public void onStart() {
        super.onStart();
        if (mActivity != null) {
            ((MainActivity) mActivity).setUpActionBar(3);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mActivity != null) {
            if (((MainActivity)mActivity).mViewPlayFragment != null) {
                ((MainActivity) mActivity).setUpActionBar(8);
            }else{
                ((MainActivity) mActivity).setUpActionBar(4);
            }
            mActivity = null;
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ((MainActivity)mActivity).unbindDrawables(rootView);
    }

    @Override
    public void onImageChosen(final ChosenImage chosenImage) {
        mActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (chosenImage != null) {
                    DisplayImageOptions options = new DisplayImageOptions.Builder()
                            .cacheOnDisk(true)
                            .cacheInMemory(true)
                            .considerExifParams(true)
                            .build();
                    mCurrentPhotoPath = "file://" + chosenImage.getFilePathOriginal();
                    ImageLoader.getInstance().displayImage(mCurrentPhotoPath, playPhoto, options);
                }
            }
        });

    }

    @Override
    public void onError(String s) {
        //Log.d("V1", "error =" + s);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
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
            /*if (playID < 0) {
                //((MainActivity) mActivity).getSupportActionBar().setDisplayShowCustomEnabled(true);
            } else {
                //onButtonPressed("refresh_plays");
            }*/
            ((MainActivity) mActivity).getSupportActionBar().setDisplayShowCustomEnabled(false);
            getFragmentManager().popBackStack();
            getFragmentManager().beginTransaction().remove(mfragment).commit();
            getFragmentManager().executePendingTransactions(); //Prevents the flashing.
        }catch (Exception ignored){}
    }

    public class AddPlayer {
        public long playerID;
        public String playerName;
        public String color;
        public int score;

        public AddPlayer(long playerID, String playerName, String color, int score) {
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

        public MySpinnerListener(AddPlayer thePlayer, Spinner spinner) {
            this.playerToUpdate = thePlayer;
            this.colorSpinner = spinner;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(colorSpinner == null) {//color
                playerToUpdate.color = adapterView.getSelectedItem().toString();
            }else {//player
                playerToUpdate.playerID = playersID.get(i);
                playerToUpdate.playerName = playersName.get(i);
                Player colorCheck = Player.findById(Player.class, playerToUpdate.playerID);
                if (colorCheck.defaultColor != null && !colorCheck.defaultColor.equals("") && playID <= 0){
                    int spinnerPostion = colorSpinnerArrayAdapter.getPosition(colorCheck.defaultColor);
                    colorSpinner.setSelection(spinnerPostion);
                    playerToUpdate.color = colorSpinner.getSelectedItem().toString();
                    //playerToUpdate.color.setSelection(spinnerPostion);
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
                //Log.d("V1", playerToUpdate.playerName);
                String score = editable.toString().trim();
                if (!score.equals("")) {
                    playerToUpdate.score = Integer.parseInt(score);
                }
            }
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
            //final boolean[] checkedItems = getArguments().getBooleanArray("checkedItems");
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
                                //Log.d("V1", checked.gameName);
                                //Log.d("V1", "isChecked="+isChecked);
                            }
                        })
                        // Set the action buttons
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                clearGames();
                                // User clicked OK, so save the checkedItems results somewhere
                                // or return them to the component that opened the dialog
                                //Log.d("V1", "addedExpansions size = " + addedExpansions.size());
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

}
