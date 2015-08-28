package com.lastsoft.plog;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerGameGroup;
import com.lastsoft.plog.db.PlaysPerGameGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddPlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddPlayerFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    long playerID;
    int cx, cy;
    Player editPlayer;
    SharedPreferences app_preferences;
    SharedPreferences.Editor editor;
    EditText playerName;
    EditText bggUsername;
    EditText bggPassword;
    Switch defaultSwitch;
    Button deleteButton;
    Spinner color;
    TextView groupsLabel;
    ViewGroup mContainerView_Groups;
    ArrayList<AddGroup> arrayOfUsers;
    ArrayList<AddGroup> buhleetMe;
    AddGroupAdapter adapter;
    List<PlayersPerGameGroup> groupers;

    public static AddPlayerFragment newInstance(int centerX, int centerY, boolean doAccelerate, long playerID) {
        AddPlayerFragment fragment = new AddPlayerFragment();
        Bundle args = new Bundle();
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        args.putBoolean("doAccelerate", doAccelerate);
        args.putLong("playerID", playerID);
        fragment.setArguments(args);
        return fragment;
    }

    public AddPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playerID = getArguments().getLong("playerID");
            if (playerID >= 0){
                editFlag = true;
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_player, container, false);
        rootView.setBackgroundColor(getResources().getColor(R.color.cardview_initial_background));
        if (playerID<0) {
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
        }

        app_preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        editor = app_preferences.edit();

        deleteButton = (Button) rootView.findViewById(R.id.deleteButton);
        playerName = (EditText) rootView.findViewById(R.id.playerName);
        bggUsername = (EditText) rootView.findViewById(R.id.bggUsername);
        bggPassword = (EditText) rootView.findViewById(R.id.bggPassword);
        defaultSwitch = (Switch) rootView.findViewById(R.id.defaultSwitch);
        color = (Spinner) rootView.findViewById(R.id.color);
        ArrayAdapter<CharSequence> colorSpinnerArrayAdapter = ArrayAdapter.createFromResource(mActivity, R.array.color_choices, android.R.layout.simple_spinner_item);
        colorSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        color.setAdapter(colorSpinnerArrayAdapter);

        mContainerView_Groups = (ViewGroup) rootView.findViewById(R.id.container_groups);
        groupsLabel = (TextView) rootView.findViewById(R.id.groupsLabel);
        /*MySpinnerListener colorListener = new MySpinnerListener(addedPlayer, 1);
        color.setOnItemSelectedListener(colorListener);*/

        // Construct the data source
        buhleetMe = new ArrayList<>();
        arrayOfUsers = new ArrayList<>();
        // Create the adapter to convert the array to views
        adapter = new AddGroupAdapter(mActivity, arrayOfUsers);


        if (playerID >= 0){
            //edit me
            editPlayer = Player.findById(Player.class, playerID);
            playerName.setText(editPlayer.playerName);
            bggUsername.setText(editPlayer.bggUsername);
            bggPassword.setText(editPlayer.bggPassword);
            if (editPlayer.defaultColor != null && !editPlayer.defaultColor.equals("")){
                int spinnerPostion = colorSpinnerArrayAdapter.getPosition(editPlayer.defaultColor);
                color.setSelection(spinnerPostion);
            }

            long defaultPlayer = app_preferences.getLong("defaultPlayer", -1);
            if (defaultPlayer == playerID){
                defaultSwitch.setChecked(true);
            }else if (defaultPlayer >= 0){
                defaultSwitch.setVisibility(View.GONE);
            }


            //check to see if this player is a member of any groups
            groupers = PlayersPerGameGroup.getPlayer(editPlayer);
            if (groupers.size() > 0) {
                //if there are groups, show them
                for (PlayersPerGameGroup grouper : groupers) {
                    AddGroup addedGroup = new AddGroup(grouper.gameGroup.getId(), grouper.gameGroup.groupName);
                    addGroup(addedGroup);
                    adapter.add(addedGroup);
                }
            }else{
                //if not, hide the layout/label
                groupsLabel.setVisibility(View.GONE);
            }

        }else {
            groupsLabel.setVisibility(View.GONE);
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                if (playerID >= 0) {
                    ((MainActivity) mActivity).deletePlayer(playerID);
                }else{
                    mActivity.onBackPressed();
                }
            }
        });


        return rootView;
    }


    public void enableDelete(){
        deleteButton.setEnabled(true);
    }


    private void addGroup(final AddGroup addedGroup) {
        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                R.layout.player_addgroup_item, mContainerView_Groups, false);

        TextView group = (TextView) newView.findViewById(R.id.group);
        group.setText(addedGroup.groupName);

        // Set a click listener for the "X" button in the row that will remove the row.
        newView.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the row from its parent (the container view).
                // Because mContainerView has android:animateLayoutChanges set to true,
                // this removal is automatically animated.
                buhleetMe.add(addedGroup);
                adapter.remove(addedGroup);
                mContainerView_Groups.removeView(newView);



            }
        });

        // Because mContainerView has android:animateLayoutChanges set to true,
        // adding this view is automatically animated.

        mContainerView_Groups.addView(newView);
    }

    public class AddGroup {
        public long groupID;
        public String groupName;

        public AddGroup(long groupID, String groupName) {
            this.groupID = groupID;
            this.groupName = groupName;
        }
    }

    public class AddGroupAdapter extends ArrayAdapter<AddGroup> {
        public AddGroupAdapter(Context context, ArrayList<AddGroup> users) {
            super(context, 0, users);
        }
    }

    Menu mMenu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(playerID >= 0 ){
            inflater.inflate(R.menu.edit_player, menu);
        }else {
            inflater.inflate(R.menu.add_player, menu);
        }
        mMenu = menu;
    }

    boolean editFlag = false;
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
            case R.id.add_player:
                if (!playerName.getText().toString().isEmpty()) {
                    //check to see if this name already exists
                    //for now, toast them if it does.
                    boolean nameTakenFlag = false;

                    if (playerID >= 0){
                        //edit
                        editFlag = true;
                        if (!editPlayer.playerName.equals(playerName.getText().toString()) && Player.playerExists(playerName.getText().toString())){
                            //if the edit player's name isn't equal to the input name AND the player name already exists
                            nameTakenFlag = true;
                        }
                    }else{
                        //if this new player's name already exists
                        if (Player.playerExists(playerName.getText().toString())){
                            nameTakenFlag = true;
                        }
                    }

                    if (nameTakenFlag){
                        Toast.makeText(mActivity, getString(R.string.name_taken), Toast.LENGTH_SHORT).show();
                    }else {
                        if (playerID >= 0) {
                            editPlayer.bggUsername = bggUsername.getText().toString();
                            editPlayer.bggPassword = bggPassword.getText().toString();
                            editPlayer.playerName = playerName.getText().toString();
                            editPlayer.defaultColor = color.getSelectedItem().toString();
                            editPlayer.save();
                        } else {
                            Player player = new Player(playerName.getText().toString(), bggUsername.getText().toString(), bggPassword.getText().toString(), color.getSelectedItem().toString());
                            player.save();
                            playerID = player.getId();
                        }


                        if (defaultSwitch.isChecked()) {
                            //set app preference
                            editor.putLong("defaultPlayer", playerID);
                            editor.commit();
                            ((MainActivity) mActivity).logIntoBGG();
                        } else {
                            //check to see if the pref is this bggusername.  if so, clear it.
                            long currentDefaultPlayer = app_preferences.getLong("defaultPlayer", -1);
                            if (currentDefaultPlayer == playerID){
                                ((MainActivity) mActivity).logOutOfBGG();
                                editor.putLong("defaultPlayer", -1);
                                editor.commit();
                            }
                        }

                        if (buhleetMe != null) {
                            for (AddGroup gone : buhleetMe) {
                                GameGroup deleteMe = GameGroup.findById(GameGroup.class, gone.groupID);
                                deleteGroupMember(deleteMe.getId());
                            }
                        }
                        if (!editFlag) {
                            onButtonPressed("refresh_players_add");
                        }
                    }
                }
                if (!removingGroup) {
                    removeYourself();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    boolean removingGroup = false;
    public void deleteGroupMember(long groupID){
        GameGroup theGroup = GameGroup.findById(GameGroup.class, groupID);
        PlayersPerGameGroup deleteMe = PlayersPerGameGroup.getPlayer(Player.findById(Player.class, playerID), theGroup);
        deleteMe.delete();

        List<PlayersPerGameGroup> players =  PlayersPerGameGroup.getPlayers(theGroup);
        if (players.size() <= 1){
            //this group only has one person now, or none, so it needs to be buhleeted
            //delete the players
            removingGroup = true;
            RemoveGroupTask removeGroup = new RemoveGroupTask(mActivity, theGroup);
            try {
                removeGroup.execute();
            } catch (Exception ignored) {

            }
        }
    }

    public class RemoveGroupTask extends AsyncTask<Long, Void, Long[]> {

        Context theContext;
        GameGroup theGroup;

        private final ProgressDialog mydialog = new ProgressDialog(mActivity);

        public RemoveGroupTask(Context context, GameGroup gameGroup) {
            this.theGroup = gameGroup;
            this.theContext = context;
        }

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            mydialog.setMessage(getString(R.string.removeGroup) + theGroup.groupName);
            mydialog.setCancelable(false);
            try{
                mydialog.show();
            }catch (Exception ignored){}
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Long[] doInBackground(final Long... args) {

            List<PlayersPerGameGroup> players =  PlayersPerGameGroup.getPlayers(theGroup);
            for (PlayersPerGameGroup player : players) {
                player.delete();
            }

            //delete the plays
            List<PlaysPerGameGroup> plays = PlaysPerGameGroup.getPlays(theGroup);
            for(PlaysPerGameGroup play:plays){
                play.delete();
            }

            theGroup.delete();

            return null;
        }

        @Override
        protected void onPostExecute ( final Long[] result){
            mydialog.dismiss();
            onButtonPressed("refresh_players_add");
            removeYourself();
        }
    }

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
            ((MainActivity) mActivity).setUpActionBar(2);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mActivity != null) {
            ((MainActivity) mActivity).setUpActionBar(5);
            mActivity = null;
        }
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
        void onFragmentInteraction(String string);
    }

    /*
    Executes fragment removal animation and removes the fragment from view.
     */
    public void removeYourself(){
        final AddPlayerFragment mfragment = this;
        mMenu.clear();
        if (editFlag){
            try {
                getFragmentManager().popBackStack();
                getFragmentManager().beginTransaction().remove(mfragment).commitAllowingStateLoss();
                getFragmentManager().executePendingTransactions(); //Prevents the flashing.
            } catch (Exception ignored) {
            }
        }else {
            Animator unreveal = mfragment.prepareUnrevealAnimator(cx, cy);
            if (unreveal != null) {
                unreveal.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        try {
                            InputMethodManager inputManager = (InputMethodManager)
                                    mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                            inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // removeFragment the fragment only when the animation finishes
                        try {
                            getFragmentManager().popBackStack();
                            getFragmentManager().beginTransaction().remove(mfragment).commitAllowingStateLoss();
                            getFragmentManager().executePendingTransactions(); //Prevents the flashing.
                        } catch (Exception ignored) {
                        }
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

}
