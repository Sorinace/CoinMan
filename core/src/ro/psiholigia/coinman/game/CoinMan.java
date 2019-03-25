package ro.psiholigia.coinman.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;


public class CoinMan extends ApplicationAdapter {
    SpriteBatch batch;

    public static float SCALE_RATIO;

    Texture background;
    Texture[] man;
    float YOUR_IMAGE_WIDTH = 1080f;
    int manState = 0;
    int pause = 0;
    float gravity = 0.25f;
    float velocity = 0;
    int manY = 0;
    Rectangle manRectangle;
    BitmapFont font;
    Texture dizzy;

    int score = 0;
    int gameState = 0;

    Random random;

    ArrayList<Integer> coinXs = new ArrayList<Integer>();
    ArrayList<Integer> coinYs = new ArrayList<Integer>();
    ArrayList<Rectangle> coinRectangle = new ArrayList<Rectangle>();
    Texture coin;
    int coinCount;

    ArrayList<Integer> bombXs = new ArrayList<Integer>();
    ArrayList<Integer> bombYs = new ArrayList<Integer>();
    ArrayList<Rectangle> bombRectangle = new ArrayList<Rectangle>();
    Texture bomb;
    int bombCount;

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("bg.png");

        SCALE_RATIO = YOUR_IMAGE_WIDTH / Gdx.graphics.getWidth();
        Gdx.app.log("Ratio", String.valueOf(SCALE_RATIO));

        man = new Texture[4];
        man[0] = new Texture("frame-1.png");
        man[1] = new Texture("frame-2.png");
        man[2] = new Texture("frame-3.png");
        man[3] = new Texture("frame-4.png");

        manY = Gdx.graphics.getHeight() / 2;

        coin = new Texture("coin.png");
        bomb = new Texture("bomb.png");
        random = new Random();

        dizzy = new Texture("dizzy-1.png");

        font = new BitmapFont();
        font.setColor(Color.YELLOW);
        font.getData().setScale(6/SCALE_RATIO);
    }

    public void makeCoin() {
        float height = random.nextFloat() * Gdx.graphics.getHeight();
        coinYs.add((int) height);
        coinXs.add(Gdx.graphics.getWidth());
    }

    public void makeBomb() {
        float height = random.nextFloat() * Gdx.graphics.getHeight();
        bombYs.add((int) height);
        bombXs.add(Gdx.graphics.getWidth());
    }

    @Override
    public void render() {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (gameState == 1) {
            // GAME IS LIVE
            if (coinCount < 100) {
                coinCount++;
            } else {
                coinCount = 0;
                makeCoin();
            }

            if (bombCount < 250) {
                bombCount++;
            } else {
                bombCount = 0;
                makeBomb();
            }

            coinRectangle.clear();
            for (int i = 0; i < coinXs.size(); i++) {
                batch.draw(coin, coinXs.get(i), coinYs.get(i), coin.getWidth()/SCALE_RATIO, coin.getHeight()/SCALE_RATIO);
                coinXs.set(i, coinXs.get(i) - 2);
                coinRectangle.add(new Rectangle(coinXs.get(i), coinYs.get(i), coin.getWidth()/SCALE_RATIO, coin.getHeight()/SCALE_RATIO));
            }

            bombRectangle.clear();
            for (int i = 0; i < bombXs.size(); i++) {
                batch.draw(bomb, bombXs.get(i), bombYs.get(i), bomb.getWidth()/SCALE_RATIO, bomb.getHeight()/SCALE_RATIO);
                bombXs.set(i, bombXs.get(i) - 4);
                bombRectangle.add(new Rectangle(bombXs.get(i), bombYs.get(i), bomb.getWidth()/SCALE_RATIO, bomb.getHeight()/SCALE_RATIO));
            }

            if (Gdx.input.justTouched()) {
                velocity = -10;
            }

            if (pause < 8) {
                pause++;
            } else {
                pause = 0;
                if (manState < 3) {
                    manState++;
                } else {
                    manState = 0;
                }
            }
            velocity += gravity;
            manY -= velocity;

            if (manY <= 0) {
                manY = 0;
            }

        } else if (gameState == 0) {
            // Waiting to start
            if (Gdx.input.justTouched()) {
                gameState = 1;
            }
        } else if (gameState == 2) {
            // GAME OVER
            if (Gdx.input.justTouched()) {
                gameState = 1;
                manY = Gdx.graphics.getHeight() / 2;
                score = 0;
                velocity = 0;
                coinXs.clear();
                coinYs.clear();
                coinRectangle.clear();
                coinCount = 0;
                bombXs.clear();
                bombYs.clear();
                bombRectangle.clear();
                bombCount = 0;
            }
        }

        if (gameState == 2) {
            batch.draw(dizzy, (Gdx.graphics.getWidth() - man[manState].getWidth()) / 2, manY, dizzy.getWidth()/SCALE_RATIO, dizzy.getHeight()/SCALE_RATIO);
        } else {
            batch.draw(man[manState], (Gdx.graphics.getWidth() - man[manState].getWidth()) / 2, manY, man[0].getWidth()/SCALE_RATIO, man[0].getHeight()/SCALE_RATIO);
        }
        manRectangle = new Rectangle((Gdx.graphics.getWidth() - man[manState].getWidth()) / 2, manY, man[manState].getWidth()/SCALE_RATIO, man[manState].getHeight()/SCALE_RATIO);

        for (int i = 0; i < coinRectangle.size(); i++) {
            if (Intersector.overlaps(manRectangle, coinRectangle.get(i))) {
                // Gdx.app.log("Coin!", "Collision!");
                score++;

                coinRectangle.remove(i);
                coinXs.remove(i);
                coinYs.remove(i);
                break;
            }
        }

        for (int i = 0; i < bombRectangle.size(); i++) {
            if (Intersector.overlaps(manRectangle, bombRectangle.get(i))) {
                // Gdx.app.log("Bomb!", "Collision!");
                gameState = 2;
            }
        }

        font.draw(batch, String.valueOf(score), 50, 100);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
