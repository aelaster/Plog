package com.lastsoft.plog;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.Play;
import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.db.PlayersPerGameGroup;
import com.lastsoft.plog.db.PlaysPerGameGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddGroupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddGroupFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ViewGroup mContainerView_Players;
    int cx, cy;
    ArrayList<AddPlayer> arrayOfUsers;
    ArrayList<Player> addedUsers;
    AddPlayerAdapter adapter;
    ArrayList<Integer> playersID;
    ArrayList<String> playersName;
    EditText groupName;

    public static AddGroupFragment newInstance(int centerX, int centerY, boolean doAccelerate) {
        AddGroupFragment fragment = new AddGroupFragment();
        Bundle args = new Bundle();
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        args.putBoolean("doAccelerate", doAccelerate);
        fragment.setArguments(args);
        return fragment;
    }

    public AddGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        List<Player> players = Player.listPlayersAZ();
        addedUsers = new ArrayList<>();
        playersName = new ArrayList<String>();
        playersID = new ArrayList<Integer>();
        for(Player player:players){
            playersName.add(player.playerName);
            playersID.add(player.getId().intValue());
        }
        setHasOptionsMenu(true);
    }

    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_add_group, container, false);
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

        mContainerView_Players = (ViewGroup) rootView.findViewById(R.id.container_players);
        groupName  = (EditText) rootView.findViewById(R.id.groupName);

        View addButton = rootView.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPlayer newUser = new AddPlayer(-1, "");
                adapter.add(newUser);
                adapter.notifyDataSetChanged();
                addPlayer(newUser);
            }
        });


        // Construct the data source
        arrayOfUsers = new ArrayList<AddPlayer>();
        // Create the adapter to convert the array to views
        adapter = new AddPlayerAdapter(mActivity, arrayOfUsers);

        return rootView;
    }

    Menu mMenu;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_group, menu);
        mMenu = menu;
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
        }catch (Exception e){}



        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                mActivity.onBackPressed();
                return true;

            case R.id.add_group:
                adapter.notifyDataSetChanged();
                if (adapter.getCount()>0) {

                    if (!groupName.getText().toString().isEmpty()) {
                        //first, add the group
                        GameGroup newGroup = new GameGroup(groupName.getText().toString());
                        newGroup.save();

                        //then add the players to the group
                        for (int i = 0; i < adapter.getCount(); i++) {
                            AddPlayer thisGuy = adapter.getItem(i);
                            addedUsers.add(Player.findById(Player.class,thisGuy.playerID));
                            PlayersPerGameGroup newPlayer = new PlayersPerGameGroup(Player.findById(Player.class, thisGuy.playerID), newGroup);
                            newPlayer.save();
                        }

                        //now go through existing plays and determine if this group should have plays logged in the plays per game group table
                        AddGroupTask initGroup = new AddGroupTask(mActivity, newGroup);
                        try {
                            initGroup.execute();
                        } catch (Exception ignored) {

                        }
                    }else{
                        mActivity.onBackPressed();
                    }
                }else{
                    mActivity.onBackPressed();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class AddGroupTask extends AsyncTask<Long, Void, Long[]> {

        Context theContext;
        GameGroup theGroup;

        private final ProgressDialog mydialog = new ProgressDialog(mActivity);

        public AddGroupTask(Context context, GameGroup gameGroup) {
            this.theGroup = gameGroup;
            this.theContext = context;
        }

        // can use UI thread here
        @Override
        protected void onPreExecute() {
            mydialog.setMessage(getString(R.string.initGroup));
            mydialog.setCancelable(false);
            try{
                mydialog.show();
            }catch (Exception ignored){}
        }

        // automatically done on worker thread (separate from UI thread)
        @Override
        protected Long[] doInBackground(final Long... args) {

            List<Play> plays = Play.listPlaysNewOld("", true);
            for (Play play:plays){
                List<Player> players = Player.getPlayersIDs(play);
                boolean included = true;

                for (Player addedUser:addedUsers){
                    if (!players.contains(addedUser)){
                        //the players of this game does not contain one of the added users
                        included = false;
                        break;
                    }
                }

                if (included){
                    //add this to PlaysPerGameGroup
                    PlaysPerGameGroup newGroupPlay = new PlaysPerGameGroup(play, theGroup);
                    newGroupPlay.save();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute ( final Long[] result){
            mydialog.dismiss();
            mActivity.onBackPressed();
        }
    }


    private void addPlayer(final AddPlayer addedPlayer) {
        // Instantiate a new "row" view.
        final ViewGroup newView = (ViewGroup) LayoutInflater.from(mActivity).inflate(
                R.layout.group_addplayer_item, mContainerView_Players, false);
        Spinner player = (Spinner) newView.findViewById(R.id.player);
        ArrayAdapter<String> playerSpinnerArrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, playersName);
        playerSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        player.setAdapter(playerSpinnerArrayAdapter);
        if (!addedPlayer.playerName.equals("")) {
            int spinnerPostion = playerSpinnerArrayAdapter.getPosition(addedPlayer.playerName);
            player.setSelection(spinnerPostion);
        }
        MySpinnerListener playerListener = new MySpinnerListener(addedPlayer, 2);
        player.setOnItemSelectedListener(playerListener);

        // Set a click listener for the "X" button in the row that will remove the row.
        newView.findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the row from its parent (the container view).
                // Because mContainerView has android:animateLayoutChanges set to true,
                // this removal is automatically animated.
                adapter.remove(addedPlayer);
                mContainerView_Players.removeView(newView);


            }
        });

        // Because mContainerView has android:animateLayoutChanges set to true,
        // adding this view is automatically animated.

        mContainerView_Players.addView(newView);
    }

    public class AddPlayer {
        public long playerID;
        public String playerName;

        public AddPlayer(long playerID, String playerName) {
            this.playerID = playerID;
            this.playerName = playerName;
        }
    }

    public class AddPlayerAdapter extends ArrayAdapter<AddPlayer> {
        public AddPlayerAdapter(Context context, ArrayList<AddPlayer> users) {
            super(context, 0, users);
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
            playerToUpdate.playerID = playersID.get(i);
            playerToUpdate.playerName = playersName.get(i);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

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
            ((MainActivity) mActivity).setUpActionBar(1);
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
        mMenu.clear();
        final AddGroupFragment mfragment = this;
        Animator unreveal = mfragment.prepareUnrevealAnimator(cx, cy);
        if(unreveal != null) {
            unreveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    try{
                        InputMethodManager inputManager = (InputMethodManager)
                                mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }catch (Exception e){}
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    // removeFragment the fragment only when the animation finishes
                    try {
                        getFragmentManager().popBackStack();
                        getFragmentManager().beginTransaction().remove(mfragment).commitAllowingStateLoss();
                        getFragmentManager().executePendingTransactions(); //Prevents the flashing.
                    }catch (Exception e){}
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
