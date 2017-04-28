package jp.techacademy.hideto.uetsuka.jumpactiongame;

import com.badlogic.gdx.graphics.Texture;

/**
 * Created by Airhead-Kangaroo on 2017/04/26.
 */

public class Ufo extends GameObject {
    public static final float UFO_WIDTH = 2.0f;
    public static final float UFO_HEIGHT = 1.3f;

    public Ufo(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        super(texture, srcX, srcY, srcWidth, srcHeight);
        setSize(UFO_WIDTH, UFO_HEIGHT);
    }
}
