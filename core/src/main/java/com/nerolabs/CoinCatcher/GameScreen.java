package com.nerolabs.CoinCatcher;

import com.badlogic.gdx.ApplicationAdapter;
//import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    final Drop game;

    private Texture image;
    Texture backgroundTexture;
    Texture bucketTexture;
    Sprite bucketSprite;
    Texture coinTexture;
    Vector2 touchPos;
    Array<Sprite> dropGoldPieces;
    float dropTimer;
    Rectangle bucketRectangle;
    Rectangle dropRectangle;
    Sound dropSound;
    Music music;

    public GameScreen(final Drop game) {
        this.game = game;

        // Load in game images
        backgroundTexture = new Texture("background.png");
        bucketTexture = new Texture("Bucket.png");
        coinTexture = new Texture("GoldCoin.png");

        // Load audio and bg music
        dropSound = Gdx.audio.newSound(Gdx.files.internal("retroCoinNoise.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("CoinCatcherMusic.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);

        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1,1);

        touchPos = new Vector2();

        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();

        dropGoldPieces = new Array<>();

    }

    @Override
    public void show() {
        music.play();
    }

    @Override
    public void render(float delta) {
        input();
        logic();
        draw();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true); // true centers the camera
    }

    private void input() {
        float speed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta);

        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta);

        }

        // Mouse Controls here
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // Get where the touch happened on screen
            game.viewport.unproject(touchPos); // Convert the units to the world units of the viewport
            bucketSprite.setCenterX(touchPos.x); // Change the horizontally centered position of the bucket
        }

    }

    private void createGold() {
        float dropHeight = 1f;
        float dropWidth = 1f;
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // Create gold drop
        Sprite dropGold = new Sprite(coinTexture);
        dropGold.setSize(dropWidth, dropHeight);
        dropGold.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropGold.setY(worldHeight);
        dropGoldPieces.add(dropGold);
    }

    private void logic() {
        // Local variable for world dimensions
        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        // Bucket size dimensions
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();

        // Clamp values for world size
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));

        // Get the delta time
        float delta = Gdx.graphics.getDeltaTime(); // retrieve the current delta

        // Apply the bucket position and size to the bucketRectangle
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth / 2, bucketHeight / 2);

        for (int i = dropGoldPieces.size - 1; i >= 0; i--) {
            Sprite dropGold = dropGoldPieces.get(i); // Get the sprite from the list
            float dropWidth = dropGold.getWidth();
            float dropHeight = dropGold.getHeight();

            dropGold.translateY(-2f * delta);
            // Apply the drop position and size to the dropRectangle
            dropRectangle.set(dropGold.getX(), dropGold.getY(), dropWidth, dropHeight);

            // if the top of the drop goes below the bottom of the view, remove it
            if (dropGold.getY() < -dropHeight) dropGoldPieces.removeIndex(i);
            else if (bucketRectangle.overlaps(dropRectangle)) {
                dropGoldPieces.removeIndex(i);
                dropSound.play();
            }
        }

        dropTimer += delta;
        if (dropTimer > 1f) {
            dropTimer = 0;
            createGold();
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK); // Clears the screen each frame
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin(); // where we draw our images

        float worldWidth = game.viewport.getWorldWidth();
        float worldHeight = game.viewport.getWorldHeight();

        game.batch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        bucketSprite.draw(game.batch); // Sprites have their own draw method

        // Drop gold
        for (Sprite dropGold : dropGoldPieces) {
            dropGold.draw(game.batch);
        }

        game.batch.end();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        dropSound.dispose();
        music.dispose();
        coinTexture.dispose();
        bucketTexture.dispose();
    }

}
