package com.iic.mokojin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iic.mokojin.models.Match;
import com.iic.mokojin.models.Models;
import com.iic.mokojin.models.Player;
import com.iic.mokojin.operation.EndMatchOperation;
import com.iic.mokojin.views.CharacterViewer;
import com.parse.ParseException;

import bolts.Continuation;
import bolts.Task;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class CurrentMatchFragment extends Fragment {

    private static final String LOG_TAG = CurrentMatchFragment.class.getName();

    private Match mCurrentMatch;

    @InjectView(R.id.empty_text_view)  View mEmptyText;
    @InjectView(R.id.players_container) View mPlayersContainer;

    @InjectView(R.id.player_a_name)  TextView mPlayerANameTextView;
    @InjectView(R.id.player_b_name)  TextView mPlayerBNameTextView;

    @InjectView(R.id.player_a_character) CharacterViewer mPlayerACharacter;
    @InjectView(R.id.player_b_character) CharacterViewer mPlayerBCharacter;


    public CurrentMatchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_current_match, container, false);
        ButterKnife.inject(this, rootView);

        return rootView;
    }

    private void refreshCurrentMatch() {
        mCurrentMatch = null;
        Match.getCurrent().continueWith(new Continuation<Match, Void>() {
            @Override
            public Void then(Task<Match> task) throws Exception {
                if (task.isCancelled()) {
                    Log.d(LOG_TAG, "Fetching of current match was cancelled");
                } else if (task.isFaulted()) {
                    Log.e(LOG_TAG, "Error fetching current match", task.getError());
                } else {
                    mCurrentMatch = task.getResult();
                    refreshUI();
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCurrentMatch();
    }

    private void refreshUI() {
        if (mCurrentMatch != null) {
            mEmptyText.setVisibility(View.INVISIBLE);
            mPlayersContainer.setVisibility(View.VISIBLE);

            mPlayerANameTextView.setText(mCurrentMatch.getPlayerA().getPerson().getName());
            mPlayerBNameTextView.setText(mCurrentMatch.getPlayerB().getPerson().getName());

            mPlayerACharacter.setPlayer(mCurrentMatch.getPlayerA());
            mPlayerBCharacter.setPlayer(mCurrentMatch.getPlayerB());
        } else {
            mEmptyText.setVisibility(View.VISIBLE);
            mPlayersContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void endMatch(Player.PlayerType playerType) {
        new EndMatchOperation(mCurrentMatch, playerType).run().continueWith(new Continuation<Match, Void>() {
            @Override
            public Void then(Task<Match> task) throws Exception {
                refreshCurrentMatch();
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void selectCharacter(Player player) {
        Intent selectCharacterIntent = new Intent(getActivity(), ChooseCharactersActivity.class);
        try {
            Models.saveToLocalStorage(player);
            selectCharacterIntent.putExtra(ChooseCharactersActivity.PLAYER_EXTRA, player.getObjectId());
            startActivity(selectCharacterIntent);
        } catch (ParseException ignored) {}
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.player_a_character)
    void onPlayerAClick() {
        endMatch(Player.PlayerType.PLAYER_A);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.player_b_character)
    void onPlayerBClick() {
        endMatch(Player.PlayerType.PLAYER_B);
    }


    @SuppressWarnings("unused")
    @OnLongClick(R.id.player_a_character)
    boolean onPlayerALongClick() {
        selectCharacter(mCurrentMatch.getPlayerA());
        return true;
    }

    @SuppressWarnings("unused")
    @OnLongClick(R.id.player_b_character)
    boolean onPlayerBLongClick() {
        selectCharacter(mCurrentMatch.getPlayerB());
        return true;
    }
}