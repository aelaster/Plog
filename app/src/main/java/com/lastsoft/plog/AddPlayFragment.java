package com.lastsoft.plog;

import android.animation.Animator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GamesPerPlay;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerPlay;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
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
public class AddPlayFragment extends Fragment {
    String mCurrentPhotoPath = "";

    private OnFragmentInteractionListener mListener;

    int cx, cy;
    File f;
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    //ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    //ArrayAdapter<String> adapter;

    // Construct the data source
    ArrayList<AddPlayer> arrayOfUsers;
    // Create the adapter to convert the array to views
    AddPlayerAdapter adapter;

    static ExpansionsAdapter expansionAdapter;
    // Attach the adapter to a ListView
    String gameName;
    long playID;
    static TextView textViewDate;

    static List<Game> expansions;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gameName = getArguments().getString("gameName");
            playID = getArguments().getLong("playID");
        }


        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);

        expansions = Game.findExpansionsFor(gameName);
        checkedItems = new boolean[expansions.size()];

        addedExpansions = new ArrayList<Game>();
        //List<Player> players = Player.listAll(Player.class);
        List<Player> players = Player.listPlayersAZ();
        playersName = new ArrayList<String>();
        playersID = new ArrayList<Integer>();
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



    EditText notesText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_play, container, false);
        rootView.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                cx = getArguments().getInt("cx");
                cy = getArguments().getInt("cy");
                // get the hypothenuse so the radius is from one corner to the other
                int radius = (int) Math.hypot(right, bottom);

                Animator reveal = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
                if (getArguments().getBoolean("doAccelerate")) {
                    reveal.setInterpolator(new DecelerateInterpolator(1.5f));
                }
                reveal.setDuration(700);
                reveal.start();
            }
        });

        View addButton = rootView.findViewById(R.id.addButton);
        notesText = (EditText) rootView.findViewById(R.id.notesText);

        View expansionButton = rootView.findViewById(R.id.addGameButton);
        expansionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBoxAlertDialogFragment newFragment = new CheckBoxAlertDialogFragment().newInstance(checkedItems, gameName);
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        playPhoto = (ImageView) rootView.findViewById(R.id.gamePhoto);
        View photoButton = rootView.findViewById(R.id.takePhoto);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 0);
                }*/
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
                /*try {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    f = createImageFile();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(takePictureIntent, 0);
                    //startActivity(takePictureIntent);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }*/
            }
        });

        View dateButton = rootView.findViewById(R.id.datePicker);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        textViewDate = (TextView) rootView.findViewById(R.id.textViewDate);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = dateFormat.parse(gameDate);
            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
            String output_date = outputFormatter.format(date1); // Output : 01/20/2012
            textViewDate.setText(getString(R.string.play_date) + output_date);
        }catch (ParseException e) {}

        // Construct the data source
        arrayOfUsers = new ArrayList<AddPlayer>();
        // Create the adapter to convert the array to views
        adapter = new AddPlayerAdapter(getActivity(), arrayOfUsers);

        expansionAdapter = new ExpansionsAdapter(getActivity(), addedExpansions);
        ListView listView2 = (ListView) rootView.findViewById(R.id.addGameList);
        listView2.setAdapter(expansionAdapter);


        // Attach the adapter to a ListView
        ListView listView = (ListView) rootView.findViewById(R.id.addPlayerList);
        listView.setAdapter(adapter);


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //listItems.add("Clicked : "+clickCounter++);
                AddPlayer newUser = new AddPlayer(-1, "", "", -9999999);
                adapter.add(newUser);
                adapter.notifyDataSetChanged();
            }
        });

        if (playID >= 0){
            //we're editing a play
            Play editPlay = Play.findById(Play.class, playID);
            //set up the values, based on DB

            //expansions
            List<GamesPerPlay> expansions = GamesPerPlay.getExpansions(editPlay);
            for(GamesPerPlay expansion:expansions){
                addedExpansions.add(expansion.game);
            }
            expansionAdapter.notifyDataSetChanged();

            //date
            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
            String output_date = outputFormatter.format(editPlay.playDate); // Output : 01/20/2012
            textViewDate.setText(getString(R.string.play_date) + output_date);
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
                AddPlayer addedPlayer = new AddPlayer(thisPlayer.getId(),thisPlayer.playerName,player.color, player.score); //id, name, color, score
                adapter.add(addedPlayer);
            }
            adapter.notifyDataSetChanged();

            //note
            notesText.setText(editPlay.playNotes);
        }

        return rootView;
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
        if (requestCode == 0 && resultCode == -1) {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .considerExifParams(true)
                    .build();
            ImageLoader.getInstance().displayImage(mCurrentPhotoPath, playPhoto, options);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
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

                    for (int i = 0; i < adapter.getCount(); i++) {
                        AddPlayer thisGuy = adapter.getItem(i);
                        if (thisGuy.score != -9999999) {
                            PlayersPerPlay newPlayer = new PlayersPerPlay(Player.findById(Player.class, thisGuy.playerID), thePlay, thisGuy.score, thisGuy.color);
                            newPlayer.save();
                        }
                    /*Log.d("V1", i + " / playerID=" + thisGuy.playerID);
                    Log.d("V1", i + " / playerName=" + thisGuy.playerName);
                    Log.d("V1", i + " / color=" + thisGuy.color);
                    Log.d("V1", i + " / score=" + thisGuy.score);*/
                    }



                    //then add the games to the play
                    if (playID>=0) {
                        //otherwise, delete the old expansions
                        List<GamesPerPlay> gamez = GamesPerPlay.getExpansions(thePlay);
                        for(GamesPerPlay game:gamez){
                            game.delete();
                        }
                    }else{//not a play yet, add the base game
                        Log.d("V1", "game name= " + gameName);
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
            if (playID<0) {
                ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);
            }else{
                onButtonPressed("refresh_plays");
            }
            ((MainActivity) getActivity()).onBackPressed();
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ((MainActivity) activity).onSectionAttached(6);
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String string);
    }

    public static class DatePickerFragment extends DialogFragment
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
                return new DatePickerDialog(getActivity(), this, year, month, day);
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
                textViewDate.setText(getString(R.string.play_date) + output_date);
            }catch (ParseException e) {}
        }
    }

    /*
    Executes fragment removal animation and removes the fragment from view.
     */
    public void removeYourself(){
        final AddPlayFragment mfragment = this;
        Animator unreveal = mfragment.prepareUnrevealAnimator(cx, cy);
        if(unreveal != null) {
            unreveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    try{
                        InputMethodManager inputManager = (InputMethodManager)
                                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }catch (Exception e){}
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // removeFragment the fragment only when the animation finishes
                    try {
                        if (playID<0) {
                            ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);
                        }else{
                            onButtonPressed("refresh_plays");
                        }
                        getFragmentManager().popBackStack();
                        getFragmentManager().beginTransaction().remove(mfragment).commit();
                        getFragmentManager().executePendingTransactions(); //Prevents the flashing.
                    }catch (Exception e){}
                    //((MainActivity)getActivity()).onBackPressed();

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            unreveal.start();
        }
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final AddPlayer addedPlayer = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.play_addplayer_item, parent, false);
            }

            Spinner player = (Spinner) convertView.findViewById(R.id.player);
            ArrayAdapter<String> playerSpinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, playersName);
            playerSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            player.setAdapter(playerSpinnerArrayAdapter);
            if (!addedPlayer.playerName.equals("")) {
                int spinnerPostion = playerSpinnerArrayAdapter.getPosition(addedPlayer.playerName);
                player.setSelection(spinnerPostion);
            }
            MySpinnerListener playerListener = new MySpinnerListener(addedPlayer, 2);
            player.setOnItemSelectedListener(playerListener);



            EditText scoreValue = (EditText) convertView.findViewById(R.id.score);
            scoreValue.addTextChangedListener(new MyTextWatcher(player, addedPlayer));
            if (addedPlayer.score != -9999999) {
                scoreValue.setText(""+addedPlayer.score);
            }


            Spinner color = (Spinner) convertView.findViewById(R.id.color);
            ArrayAdapter<CharSequence> colorSpinnerArrayAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.color_choices, android.R.layout.simple_spinner_item);
            colorSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            color.setAdapter(colorSpinnerArrayAdapter);
            if (!addedPlayer.color.equals("")) {
                int spinnerPostion = colorSpinnerArrayAdapter.getPosition(addedPlayer.color);
                color.setSelection(spinnerPostion);
            }
            MySpinnerListener colorListener = new MySpinnerListener(addedPlayer, 1);
            color.setOnItemSelectedListener(colorListener);



            View expansionButton = convertView.findViewById(R.id.closeButton);
            expansionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.remove(addedPlayer);
                    adapter.notifyDataSetChanged();
                }
            });

            return convertView;
        }


    }

    public class ExpansionsAdapter extends ArrayAdapter<Game> {
        public ExpansionsAdapter(Context context, ArrayList<Game> games) {
            super(context, 0, games);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final Game addedExpansion = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.play_showexpansions_item, parent, false);
            }

            TextView gameName = (TextView) convertView.findViewById(R.id.gameName);
            gameName.setText(addedExpansion.gameName);

            return convertView;
        }


    }

    public class MySpinnerListener implements AdapterView.OnItemSelectedListener{

        private AddPlayer playerToUpdate;
        private int updateType;

        public MySpinnerListener(AddPlayer thePlayer, int updateType) {
            this.playerToUpdate = thePlayer;
            this.updateType = updateType;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if(updateType == 1) {//color
                playerToUpdate.color = adapterView.getSelectedItem().toString();
            }else if(updateType == 2) {//player
                playerToUpdate.playerID = playersID.get(i);
                playerToUpdate.playerName = playersName.get(i);
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
                Log.d("V1", playerToUpdate.playerName);
                String score = editable.toString().trim();
                if (!score.toString().equals("")) {
                    playerToUpdate.score = Integer.parseInt(score.toString());
                }
            }
        }
    }

    /**
     * Get the animator to unreveal the circle
     *
     * @param cx center x of the circle (or where the view was touched)
     * @param cy center y of the circle (or where the view was touched)
     * @return Animator object that will be used for the animation
     */
    public Animator prepareUnrevealAnimator(float cx, float cy)
    {

        int radius = getEnclosingCircleRadius(getView(), (int)cx, (int)cy);
        if(radius == -1){
            return null;
        }
        Animator anim = ViewAnimationUtils.createCircularReveal(getView(), (int) cx, (int) cy, radius, 0);
        if(getArguments().getBoolean("doAccelerate")) {
            anim.setInterpolator(new AccelerateInterpolator(1.5f));
        }
        anim.setDuration(600);
        return anim;
    }

    /**
     * To be really accurate we have to start the circle on the furthest corner of the view
     *
     * @param v the view to unreveal
     * @param cx center x of the circle
     * @param cy center y of the circle
     * @return the maximum radius
     */
    private int getEnclosingCircleRadius(View v, int cx, int cy)
    {
        if(v == null){
            return -1;
        }
        int realCenterX = cx + v.getLeft();
        int realCenterY = cy + v.getTop();
        int distanceTopLeft = (int)Math.hypot(realCenterX - v.getLeft(), realCenterY - v.getTop());
        int distanceTopRight = (int)Math.hypot(v.getRight() - realCenterX, realCenterY - v.getTop());
        int distanceBottomLeft = (int)Math.hypot(realCenterX - v.getLeft(), v.getBottom() - realCenterY);
        int distanceBotomRight = (int)Math.hypot(v.getRight() - realCenterX, v.getBottom() - realCenterY);

        int[] distances = new int[] {distanceTopLeft, distanceTopRight, distanceBottomLeft, distanceBotomRight};
        int radius = distances[0];
        for (int i = 1; i < distances.length; i++)
        {
            if (distances[i] > radius)
                radius = distances[i];
        }
        return radius;
    }


    public static class CheckBoxAlertDialogFragment extends DialogFragment {



        public static CheckBoxAlertDialogFragment newInstance(boolean[] checkedItems, String gameName) {
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

            List<String> expansionNames = new ArrayList<String>();
            for(Game expansion:expansions){
                expansionNames.add(expansion.gameName);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                                Log.d("V1", checked.gameName);
                                Log.d("V1", ""+checked.getId());
                            }
                        })
                        // Set the action buttons
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK, so save the checkedItems results somewhere
                                // or return them to the component that opened the dialog
                                expansionAdapter.notifyDataSetChanged();

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
