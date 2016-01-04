package gametoolsexamples;

//These three imports are recommended for easier use of library
import gametools.*;
import gametools.gravity.*;
import static gametools.Tools.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

//Project can either extend Game or GravityGame depeding on the content
public class Platformer extends GravityGame {
    //Giving values to variables won't work up here, do it in setup instead
    Mass player; //Mass class is a sprite that uses gravity
    BufferedImage box, heart; //Images should be loaded once for efficiency
    Random random;
    int score, time, distance, health, countdown, lastBonus;
    double speed; //The library uses doubles for everything, so this allows more accuracy
    
    //The main java method that java always runs first
    public static void main(String[] args) {
        initialize(Platformer.class); //Sets up the tool class to find the base of the project for loading images
        new Platformer(); //Need to include this for game to run
    }
    
    //Method runs before window is created, used for changing game settings
    @Override
    protected void window() {
        setTitle("Example Platform Game");
        setBackground(Color.DARK_GRAY); //Background can be color or image
        setDefaultGravity(75); //Gravity is percent
        create(); //Creates the window (no settings should be changed after this)
    }
    
    //Method runs once after window is created, used for setup and initialization
    @Override
    protected void setup() {
        //Every project wide value should be set here so that the game can easily be restarted by running the setup method
        score = 0;
        time = 0;
        lastBonus = 0;
        distance = 30;
        speed = 1.5;
        health = 3;
        
        //Sets up images and sprites
        heart = loadImage("img/heart.png"); //Images should be loaded here for efficiency
        player = new Mass(loadImage("img/green.png"));
        player.setSpeed(10);
        box = generateBox(Color.BLUE, dm(160, 30)); //Generates first platform image
        random = new Random();
        
        //Adds first nine platforms
        int max = getWidth() - box.getWidth();
        for (int i = 0; i < 9; i++) platforms().add(new Sprite(pt(random.nextInt(max), i * 80 + 60), box));
        platforms().removeWhenOffScreen(); //Remove unnecessary platforms automatically
        
        //Increases font size
        Font large = new Font("Arial", Font.PLAIN, 16);
        painter().setFont(large);
        respawn(); //Spawn the player
    }
    
    //Method runs at a set FPS and contains main game code
    @Override
    protected void run() {
        //Update score and timer by one
        score++;
        time++;
        
        //Shrink the box width
        if (time % 30 == 0 && box.getWidth() > 40) box = generateBox(Color.BLUE, dm(box.getWidth() - 1, 30));
        //Increase the speed
        if (time % 30 == 10 && speed < 4.5) speed += 0.02;
        //Increase box distance
        if (time % 30 == 20 && distance < 80) distance++;
        
        //If top box farther than box distance, spawn another one
        if (platforms().get(platforms().size() - 1).getY() > distance)
            platforms().add(new Sprite(pt(random.nextInt(getWidth() - box.getWidth()), -30), box));
        
        //Move everything down
        platforms().translate(pt(0, speed));
        player.translate(pt(0, speed));
        
        //Movement and jumping input
        if (keyPressed(KeyEvent.VK_RIGHT) || keyPressed(KeyEvent.VK_D)) player.move(Sprite.Direction.EAST);
        if (keyPressed(KeyEvent.VK_LEFT) || keyPressed(KeyEvent.VK_A)) player.move(Sprite.Direction.WEST);
        if (keyPressed(KeyEvent.VK_SPACE) && player.isOnGround()) player.jump(18);
        
        //Teleport player to other side if he is off screen
        if (player.getX() >= getWidth()) player.setX(0);
        else if (player.getX() < 0) player.setX(getWidth() - player.getWidth());
        
        //If player falls of the screen
        if (player.getY() >= getHeight()) {
            health--;
            //If player is dead then reset game and print score
            if (health < 0) {
                messageDialog("You died with a score of " + score); //Display popup with score
                platforms().clear();
                setup(); //Resets all variables
            }
            else respawn();
        }
        else if (player.getY() < -player.getHeight()) {
            //If player jumps out of the top he gets a score bonus.
            lastBonus = (int) Math.floor(score * 0.15);
            score += lastBonus;
            countdown = 60;
            respawn();
        }
        
        //Draw the platforms and the player
        platforms().drawAll();
        player.draw();
        
        //Draws the health images
        for (int i = 0; i < health; i++) painter().drawImage(heart, getWidth() - ((i + 1) * heart.getWidth()) - 10, 10, null);
        painter().drawString(score + "", 15, 20); //Draws the score
        
        //Draws score bonus text if needed
        if (countdown > 0) painter().drawString("15% Score Bonus! +" + lastBonus, 320, 100);
        countdown--;
    }
    
    //Respawns the player in the middle of the screen on a red platform
    void respawn() {
        //Create a new red platform in the center
        Sprite platform = new Sprite(generateBox(Color.RED, dm(box.getWidth(), box.getHeight())));
        platform.centerOn(getCenter());
        
        //Remove overlapping platforms
        platforms().remove(platforms().getAllWithin(platform));
        platforms().add(0, platform); //Add to bottom to not interfere with spawn distance
        
        //Move the player above the platform
        player.centerOn(getCenter());
        player.translate(pt(0, (-platform.getHeight() - player.getHeight()) / 2));
        player.stopJump();
    }
}
