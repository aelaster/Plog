package com.lastsoft.plog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.lastsoft.plog.db.Player;
import com.lastsoft.plog.dialogs.SetupWizardDialogFragment;
import com.lastsoft.plog.util.LoadGamesTask;
import com.lastsoft.plog.wizard.model.AbstractWizardModel;
import com.lastsoft.plog.wizard.model.CustomerInfoPage;
import com.lastsoft.plog.wizard.model.ModelCallbacks;
import com.lastsoft.plog.wizard.model.Page;
import com.lastsoft.plog.wizard.ui.PageFragmentCallbacks;
import com.lastsoft.plog.wizard.ui.ReviewFragment;
import com.lastsoft.plog.wizard.ui.StepPagerStrip;

import java.util.List;

/**
 * Created by TheFlash on 6/1/2015.
 */
public class SetupWizardFragment extends Fragment implements
        PageFragmentCallbacks,
        ReviewFragment.Callbacks,
        ModelCallbacks,
        SetupWizardDialogFragment.OnDialogButtonClickListener{
    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private boolean mEditingAfterReview;

    private AbstractWizardModel mWizardModel = new SetupWizard_Model(getActivity());

    private boolean mConsumePageSelectedEvent;

    private Button mNextButton;
    private Button mPrevButton;

    private List<Page> mCurrentPageSequence;
    private StepPagerStrip mStepPagerStrip;
        public static SetupWizardFragment newInstance() {
                SetupWizardFragment fragment = new SetupWizardFragment();
                Bundle args = new Bundle();
                fragment.setArguments(args);
                return fragment;
        }

        public SetupWizardFragment() {
                // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
                // Inflate the layout for this fragment
                View rootView = inflater.inflate(R.layout.fragment_setup_wizard, container, false);
                if (savedInstanceState != null) {
                    mWizardModel.load(savedInstanceState.getBundle("model"));
                }

                mWizardModel.registerListener(this);

                mPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
                mPager = (ViewPager) rootView.findViewById(R.id.pager);
                mPager.setAdapter(mPagerAdapter);
                mStepPagerStrip = (StepPagerStrip) rootView.findViewById(R.id.strip);
                mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
                    @Override
                    public void onPageStripSelected(int position) {
                        position = Math.min(mPagerAdapter.getCount() - 1, position);
                        if (mPager.getCurrentItem() != position) {
                            mPager.setCurrentItem(position);
                        }
                    }
                });

                mNextButton = (Button) rootView.findViewById(R.id.next_button);
                mPrevButton = (Button) rootView.findViewById(R.id.prev_button);

                mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mStepPagerStrip.setCurrentPage(position);

                        if (mConsumePageSelectedEvent) {
                            mConsumePageSelectedEvent = false;
                            return;
                        }

                        mEditingAfterReview = false;
                        updateBottomBar();
                    }
                });

                mNextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
                            SetupWizardDialogFragment newFragment = new SetupWizardDialogFragment();
                            newFragment.setTargetFragment(SetupWizardFragment.this, 0);
                            newFragment.show(((MainActivity)mActivity).getSupportFragmentManager(), "place_order_dialog");
                        } else {
                            if (mEditingAfterReview) {
                                mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                            } else {
                                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                            }
                        }
                    }
                });

                mPrevButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                    }
                });

                onPageTreeChanged();
                updateBottomBar();
                return rootView;
        }



        Activity mActivity;
        @Override
        public void onAttach(Activity activity) {
                super.onAttach(activity);
                mActivity = activity;
                try {
                    ((MainActivity) mActivity).setUpActionBar(9);
                } catch (ClassCastException e) {
                        throw new ClassCastException(activity.toString()
                                + " must implement OnFragmentInteractionListener");
                }
        }

        @Override
        public void onDetach() {
                super.onDetach();
                mActivity = null;
        }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 = review step
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
            mNextButton.setText(R.string.finish);
            mNextButton.setBackgroundResource(R.drawable.finish_background);
            mNextButton.setTextAppearance(mActivity, R.style.TextAppearanceFinish);
        } else {
            mNextButton.setText(mEditingAfterReview
                    ? R.string.review
                    : R.string.next);
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            mActivity.getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            mNextButton.setTextAppearance(mActivity, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        }

        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String key) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(key)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    @Override
    public void onPositiveClick_SetupWizard() {
        String userName = mWizardModel.findByKey("Enter your name.  You will be the first player entered.  Your BGG Name will be used to pull your collection.  Your BGG Password will be used to log your plays.").getData().getString(CustomerInfoPage.NAME_DATA_KEY);
        if (userName != null) {
            //add theis player
            String bggInfo = mWizardModel.findByKey("Enter your name.  You will be the first player entered.  Your BGG Name will be used to pull your collection.  Your BGG Password will be used to log your plays.").getData().getString(CustomerInfoPage.EMAIL_DATA_KEY);
            String bggInfo_pw = mWizardModel.findByKey("Enter your name.  You will be the first player entered.  Your BGG Name will be used to pull your collection.  Your BGG Password will be used to log your plays.").getData().getString(CustomerInfoPage.PASSWORD_DATA_KEY);
            boolean nameTakenFlag = false;
            //if this new player's name already exists
            if (Player.playerExists(userName)) {
                nameTakenFlag = true;
            }
            if (nameTakenFlag) {
                Toast.makeText(mActivity, getString(R.string.name_taken), Toast.LENGTH_SHORT).show();
            } else {
                Player player;
                player = Player.findPlayerByName(userName);
                if (player == null){
                    player = new Player(userName, bggInfo, bggInfo_pw);
                    player.save();
                }else{
                    player.bggUsername = bggInfo;
                    player.bggPassword = bggInfo_pw;
                    player.save();
                }

                if (bggInfo != null) {
                    //set app preference
                    SharedPreferences app_preferences;
                    SharedPreferences.Editor editor;
                    app_preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                    editor = app_preferences.edit();
                    editor.putLong("defaultPlayer", player.getId());
                    editor.commit();
                }
                GamesLoader initDb = new GamesLoader(getActivity());
                try {
                    initDb.execute();
                } catch (Exception e) {

                }
            }
        }
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            if (mCurrentPageSequence == null) {
                return 0;
            }
            return Math.min(mCutOffPage + 1, mCurrentPageSequence.size() + 1);
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }

    public class GamesLoader extends LoadGamesTask {
        private final ProgressDialog mydialog;
        public GamesLoader(Context context) {
            super(context);
            mydialog = new ProgressDialog(theContext);
        }

        // can use UI thread here
        @Override
        protected void onPreExecute() {

            mydialog.setMessage(theContext.getString(R.string.setting_up));
            mydialog.setCancelable(false);
            try{
                mydialog.show();
            }catch (Exception e){}
        }


        @Override
        protected void onPostExecute(final String result) {
            mydialog.dismiss();
            getActivity().finish();
            startActivity(getActivity().getIntent());
        }
    }

}
