package com.lastsoft.plog;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;

import com.lastsoft.plog.db.Game;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddGameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddGameFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    int cx, cy;

    public static AddGameFragment newInstance(int centerX, int centerY, boolean doAccelerate) {
        AddGameFragment fragment = new AddGameFragment();
        Bundle args = new Bundle();
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        args.putBoolean("doAccelerate", doAccelerate);
        fragment.setArguments(args);
        return fragment;
    }

    public AddGameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_add_game, container, false);
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

        View button = rootView.findViewById(R.id.button);
        final EditText gameName = (EditText) rootView.findViewById(R.id.gameName);
        final Switch addToBGG = (Switch) rootView.findViewById(R.id.addToBGG);
        final Switch expansionFlag = (Switch) rootView.findViewById(R.id.expansionFlag);



        // Set a listener to reveal the view when clicked.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            view.setEnabled(false);
            if (!gameName.getText().toString().isEmpty()) {
                /*Player newPlayer = new Player(playerName.getText().toString(), bggUsername.getText().toString());
                MyDBHandler dbHandler = new MyDBHandler(mActivity, null, null, 1);
                dbHandler.addPlayer(newPlayer);*/
                Game game = new Game(gameName.getText().toString(), expansionFlag.isChecked());
                game.save();
                ((MainActivity) mActivity).searchGameViaBGG(gameName.getText().toString(), addToBGG.isChecked(), expansionFlag.isChecked(), game.getId());
                onButtonPressed("refresh_games");
            }
            removeYourself();
            }
        });
        return rootView;
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
            ((MainActivity) mActivity).setUpActionBar(0);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mActivity != null) {
            ((MainActivity) mActivity).setUpActionBar(4);
            mActivity = null;
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
        }catch (Exception e){}

        //noinspection SimplifiableIfStatement
        switch (item.getItemId()) {
            case android.R.id.home:
                //called when the up affordance/carat in actionbar is pressed
                mActivity.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        final AddGameFragment mfragment = this;
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
