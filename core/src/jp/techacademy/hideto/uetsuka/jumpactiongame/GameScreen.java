package jp.techacademy.hideto.uetsuka.jumpactiongame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.particles.influencers.ColorInfluencer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Airhead-Kangaroo on 2017/04/26.
 */

public class GameScreen extends ScreenAdapter {

    static final float CAMERA_WIDTH = 10;
    static final float CAMERA_HEIGHT = 15;
    static final float WORLD_WIDTH = 10;
    static final float WORLD_HEIGHT = 15 * 20;

    static final int GAME_STATE_READY = 0;
    static final int GAME_STATE_PLAYING = 1;
    static final int GAME_STATE_GAMEOVER= 2;

    static final float GRAVITY = -12;

    private JumpActionGame mGame;

    Sprite mBg;
    OrthographicCamera mCamera;
    FitViewport mViewPort;

    Random mRandom;
    List<Step> mSteps;
    List<Star> mStars;
    Ufo mUfo;
    Player mPlayer;

    float mHeightSoFar;
    int mGameState;
    Vector3 mTouchPoint;

    public GameScreen(JumpActionGame game){
        mGame = game;

        Texture bgTexture = new Texture("back.png");
        mBg = new Sprite(new TextureRegion(bgTexture,0,0,540,810));
        mBg.setSize(CAMERA_WIDTH,CAMERA_HEIGHT);
        mBg.setPosition(0,0);

        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false,CAMERA_WIDTH,CAMERA_HEIGHT);
        mViewPort = new FitViewport(CAMERA_WIDTH,CAMERA_HEIGHT,mCamera);

        mRandom = new Random();
        mSteps = new ArrayList<Step>();
        mStars = new ArrayList<Star>();
        mGameState = GAME_STATE_READY;
        mTouchPoint = new Vector3();

        createStage();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);

        mCamera.update();
        mGame.batch.setProjectionMatrix(mCamera.combined);

        mGame.batch.begin();

        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2);
        mBg.draw(mGame.batch);

        for(int i=0;i<mSteps.size();i++){
            mSteps.get(i).draw(mGame.batch);
        }

        for(int i=0;i<mStars.size();i++){
            mStars.get(i).draw(mGame.batch);
        }

        mUfo.draw(mGame.batch);
        mPlayer.draw(mGame.batch);

        mGame.batch.end();
    }

    private void createStage(){
        Texture stepTexture = new Texture("step.png");
        Texture starTexture = new Texture("star.png");
        Texture playerTexture = new Texture("uma.png");
        Texture ufoTexture = new Texture("ufo.png");

        float y = 0;
        float maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * -GRAVITY);
        while(y < WORLD_HEIGHT - 5){
            int type = mRandom.nextFloat() > 0.8 ? Step.STEP_TYPE_MOVING : Step.STEP_TYPE_STATIC;
            float x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH);

            Step step = new Step(type, stepTexture,0,0,144,36);
            step.setPosition(x,y);
            mSteps.add(step);

            if(mRandom.nextFloat() > 0.6f) {
                Star star = new Star(starTexture, 0, 0, 72, 72);
                star.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + star.STAR_HEIGHT + mRandom.nextFloat() * 3);
                mStars.add(star);
            }

            y += (maxJumpHeight - 0.5f);
            y -= mRandom.nextFloat() * (maxJumpHeight / 3);
        }
        mPlayer = new Player(playerTexture,0,0,72,72);
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.getWidth() / 2, Step.STEP_HEIGHT);

        mUfo = new Ufo(ufoTexture,0,0,120,74);
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH /2, y);
    }

    private void update(float delta){
        switch (mGameState){
            case GAME_STATE_READY:
                updateReady();
                break;
            case GAME_STATE_PLAYING:
                updatePlaying(delta);
                break;
            case GAME_STATE_GAMEOVER:
                updateGameOver();
                break;
        }
    }

    @Override
    public void resize(int width, int height) {
        mViewPort.update(width,height);
    }

    private void updateReady(){
        if(Gdx.input.justTouched()){
            mGameState = GAME_STATE_PLAYING;
        }
    }

    private void updatePlaying(float delta){
        float accel = 0;
        if(Gdx.input.isTouched()){
            mViewPort.unproject(mTouchPoint.set(Gdx.input.getX(),Gdx.input.getY(),0));
            Rectangle left = new Rectangle(0,0, CAMERA_WIDTH / 2, CAMERA_HEIGHT);
            Rectangle right = new Rectangle(CAMERA_WIDTH / 2,0,CAMERA_WIDTH / 2, CAMERA_HEIGHT);
            if(left.contains(mTouchPoint.x, mTouchPoint.y)){
                accel = 5.0f;
            }
            if(right.contains(mTouchPoint.x, mTouchPoint.y)){
                accel = -5.0f;
            }
        }

        for(int i=0;i<mSteps.size();i++){
            mSteps.get(i).update(delta);
        }

        if(mPlayer.getY() <= 0.5f){
            mPlayer.hitStep();
        }
        mPlayer.update(delta, accel);
        mHeightSoFar = Math.max(mPlayer.getV(), mHeightSoFar);

        checkCollision();
    }

    private void checkCollision(){
        if(mPlayer.getBoundingRectangle().overlaps(mUfo.getBoundingRectangle())){
            mGameState = GAME_STATE_GAMEOVER;
            return;
        }

        for(int i=0;i<mStars.size();i++){
            Star star = mStars.get(i);
            if(star.mState == Star.STAR_NONE){
                continue;
            }
            if(mPlayer.getBoundingRectangle().overlaps(star.getBoundingRectangle())){
                star.get();
                break;
            }
        }

        if(mPlayer.velocity.y > 0){
            return;
        }

        for(int i=0;i<mSteps.size();i++){
            Step step = mSteps.get(i);
            if(step.mState == Step.STEP_STATE_VANISH){
                continue;
            }
            if(mPlayer.getY() > step.getY()){
                if(mPlayer.getBoundingRectangle().overlaps(step.getBoundingRectangle())){
                    mPlayer.hitStep();
                    if(mRandom.nextFloat() > 0.5f){
                        step.vanish();
                    }
                    break;
                }
            }
        }
    }

    private void updateGameOver(){

    }
}
