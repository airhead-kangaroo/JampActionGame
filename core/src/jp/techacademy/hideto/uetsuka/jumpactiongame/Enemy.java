package jp.techacademy.hideto.uetsuka.jumpactiongame;

import com.badlogic.gdx.graphics.Texture;

/**
 * Created by Airhead-Kangaroo on 2017/04/29.
 */

public class Enemy extends GameObject {

    public static final float ENEMY_WIDTH = 1.0f;
    public static final float ENEMY_HEIGHT = 1.2f;
    public static final int ENEMY_STATE_ALIVE = 0;
    public static final int ENEMY_STATE_DEAD = 1;
    public static final float ENEMY_MOVE_VELOCITY = 8.0f;

    int direction;
    int mState;

    public Enemy(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight){
        super(texture,srcX,srcY,srcWidth,srcHeight);
        setSize(ENEMY_WIDTH,ENEMY_HEIGHT);
        mState = ENEMY_STATE_ALIVE;
        direction = 1;
    }

    public void update(float delta){
        velocity.x = ENEMY_MOVE_VELOCITY;
        setPosition(getX() + (velocity.x * delta * direction), getY());

        if(getX() + ENEMY_WIDTH / 2  > GameScreen.WORLD_WIDTH || getX() + ENEMY_WIDTH / 2 < 0 ){
            direction *= -1;
        }
    }


}
