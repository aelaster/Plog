/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.lastsoft.plog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lastsoft.plog.db.Player;

import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class PlayersFragment extends Fragment{

    private static final String TAG = "PlayersFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;
    private float x,y;


    private enum LayoutManagerType {
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;
    private SwipeRefreshLayout pullToRefreshView;

    private OnFragmentInteractionListener mListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
        initDataset();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.players_view_frag, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        FloatingActionButton addPlayer = (FloatingActionButton) rootView.findViewById(R.id.add_player);
        addPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewXY[] = new int[2];
                v.getLocationOnScreen(viewXY);

                if (mListener != null) {
                    mListener.onFragmentInteraction("add_player", viewXY[0], viewXY[1]);
                }
            }
        });

        FloatingActionButton addGroup = (FloatingActionButton) rootView.findViewById(R.id.add_group);
        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int viewXY[] = new int[2];
                v.getLocationOnScreen(viewXY);

                if (mListener != null) {
                    mListener.onFragmentInteraction("add_group", viewXY[0], viewXY[1]);
                }
            }
        });


        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mAdapter = new CustomAdapter(getActivity(), mDataset, null);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        /*pullToRefreshView = (SwipeRefreshLayout) rootView.findViewById(R.id.pull_to_refresh_listview);
        pullToRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                pullToRefreshView.setRefreshing(false);
            }
        });*/


        // END_INCLUDE(initializeRecyclerView)
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(2);
        try {
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
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset() {
        List<Player> players = Player.listPlayersAZ();
        if (players != null) {
            mDataset = new String[players.size()];
            int i = 0;
            for(Player player:players){
                mDataset[i] = player.playerName;
                //Log.d("V1", "player name = " + player.playerName);
                i++;
            }
        }
    }

    protected void refreshDataset(){
        initDataset();
        mAdapter = new CustomAdapter(getActivity(), mDataset, null);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
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
        public void onFragmentInteraction(String id, float x, float y);
    }
}
