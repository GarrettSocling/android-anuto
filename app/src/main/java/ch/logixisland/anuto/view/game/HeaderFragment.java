package ch.logixisland.anuto.view.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ch.logixisland.anuto.AnutoApplication;
import ch.logixisland.anuto.GameFactory;
import ch.logixisland.anuto.R;
import ch.logixisland.anuto.business.control.TowerSelector;
import ch.logixisland.anuto.business.game.BonusListener;
import ch.logixisland.anuto.business.game.CreditsListener;
import ch.logixisland.anuto.business.game.GameSpeed;
import ch.logixisland.anuto.business.game.GameSpeedListener;
import ch.logixisland.anuto.business.game.GameState;
import ch.logixisland.anuto.business.game.GameStateListener;
import ch.logixisland.anuto.business.game.LivesListener;
import ch.logixisland.anuto.business.game.ScoreBoard;
import ch.logixisland.anuto.business.level.WaveListener;
import ch.logixisland.anuto.business.level.WaveManager;
import ch.logixisland.anuto.util.StringUtils;
import ch.logixisland.anuto.view.AnutoFragment;

public class HeaderFragment extends AnutoFragment implements GameStateListener, WaveListener,
        CreditsListener, LivesListener, BonusListener, GameSpeedListener, View.OnClickListener {

    private final GameState mGameState;
    private final WaveManager mWaveManager;
    private final GameSpeed mSpeedManager;
    private final ScoreBoard mScoreBoard;
    private final TowerSelector mTowerSelector;

    private Handler mHandler;

    private TextView txt_credits;
    private TextView txt_lives;
    private TextView txt_wave;
    private TextView txt_bonus;

    private Button btn_next_wave;
    private Button btn_fast_forward;
    private Button btn_menu;

    private TowerView[] view_tower_x = new TowerView[4];

    public HeaderFragment() {
        GameFactory factory = AnutoApplication.getInstance().getGameFactory();
        mGameState = factory.getGameState();
        mScoreBoard = factory.getScoreBoard();
        mWaveManager = factory.getWaveManager();
        mSpeedManager = factory.getSpeedManager();
        mTowerSelector = factory.getTowerSelector();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mHandler = new Handler();

        View v = inflater.inflate(R.layout.fragment_header, container, false);

        txt_credits = (TextView) v.findViewById(R.id.txt_credits);
        txt_lives = (TextView) v.findViewById(R.id.txt_lives);
        txt_wave = (TextView) v.findViewById(R.id.txt_wave);
        txt_bonus = (TextView) v.findViewById(R.id.txt_bonus);

        btn_next_wave = (Button) v.findViewById(R.id.btn_next_wave);
        btn_fast_forward = (Button) v.findViewById(R.id.btn_fast_forward);
        btn_menu = (Button) v.findViewById(R.id.btn_menu);

        btn_next_wave.setOnClickListener(this);
        btn_fast_forward.setOnClickListener(this);
        btn_menu.setOnClickListener(this);

        view_tower_x[0] = (TowerView) v.findViewById(R.id.view_tower_1);
        view_tower_x[1] = (TowerView) v.findViewById(R.id.view_tower_2);
        view_tower_x[2] = (TowerView) v.findViewById(R.id.view_tower_3);
        view_tower_x[3] = (TowerView) v.findViewById(R.id.view_tower_4);

        btn_next_wave.setEnabled(!mGameState.isGameOver());
        txt_wave.setText(getString(R.string.wave) + ": " + mWaveManager.getWaveNumber());
        txt_credits.setText(getString(R.string.credits) + ": " + StringUtils.formatSuffix(mScoreBoard.getCredits()));
        txt_lives.setText(getString(R.string.lives) + ": " + mScoreBoard.getLives());
        txt_bonus.setText(getString(R.string.bonus) + ": " + StringUtils.formatSuffix(mScoreBoard.getWaveBonus() + mScoreBoard.getEarlyBonus()));
        btn_fast_forward.setText(getString(mSpeedManager.isFastForwardActive() ? R.string.fast_speed : R.string.normal_speed));

        for (int i = 0; i < view_tower_x.length; i++) {
            view_tower_x[i].setSlot(i);
        }

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mGameState.addListener(this);
        mWaveManager.addListener(this);
        mSpeedManager.addListener(this);
        mScoreBoard.addBonusListener(this);
        mScoreBoard.addCreditsListener(this);
        mScoreBoard.addLivesListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mGameState.removeListener(this);
        mWaveManager.removeListener(this);
        mSpeedManager.removeListener(this);
        mScoreBoard.removeBonusListener(this);
        mScoreBoard.removeCreditsListener(this);
        mScoreBoard.removeLivesListener(this);

        for (TowerView towerView : view_tower_x) {
            towerView.close();
        }

        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {
        if (v == btn_next_wave) {
            mWaveManager.startNextWave();
        }

        if (v == btn_fast_forward) {
            mSpeedManager.toggleFastForward();
        }

        if (v == btn_menu) {
            mTowerSelector.selectTower(null);
            Intent intent = new Intent(getActivity(), MenuActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void gameRestart() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < view_tower_x.length; i++) {
                    view_tower_x[i].setSlot(i);
                }
            }
        });
    }

    @Override
    public void gameOver() {

    }

    @Override
    public void waveNumberChanged() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txt_wave.setText(getString(R.string.wave) + ": " + mWaveManager.getWaveNumber() + " (" + mWaveManager.getRemainingEnemiesCount() + ")");
            }
        });
    }

    @Override
    public void nextWaveReadyChanged() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                btn_next_wave.setEnabled(mWaveManager.isNextWaveReady());
            }
        });
    }

    @Override
    public void remainingEnemiesCountChanged() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txt_wave.setText(getString(R.string.wave) + ": " + mWaveManager.getWaveNumber() + " (" + mWaveManager.getRemainingEnemiesCount() + ")");
            }
        });
    }

    @Override
    public void creditsChanged(final int credits) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txt_credits.setText(getString(R.string.credits) + ": " + StringUtils.formatSuffix(credits));
            }
        });
    }

    @Override
    public void livesChanged(final int lives) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txt_lives.setText(getString(R.string.lives) + ": " + lives);
            }
        });
    }

    @Override
    public void bonusChanged(final int waveBonus, final int earlyBonus) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                txt_bonus.setText(getString(R.string.bonus) + ": " + StringUtils.formatSuffix(waveBonus + earlyBonus));
            }
        });
    }

    @Override
    public void gameSpeedChanged() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                btn_fast_forward.setText(getString(mSpeedManager.isFastForwardActive() ? R.string.fast_speed : R.string.normal_speed));
            }
        });
    }
}
