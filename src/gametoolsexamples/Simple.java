package gametoolsexamples;

//These two imports are recommended for easier use of library
import gametools.*;
import static gametools.Tools.*;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Simple extends Game {
    //Giving values to variables won't work up here, do it in setup instead
    BufferedImage image, dot; //Images should be loaded once for efficiency
    Sprite player; //Sprites are the characters and objects in your game that move, animate and have collision
    Group dots; //A group is simliar to an array list and can be used for giving commands to multiple sprites
    int timer;
    
    //The main java method that java always runs first
    public static void main(String[] args) {
        initialize(Simple.class); //Sets up the tool class to find the base of the project for loading images
        new Simple(); //Need to include this for game to run
    }
    
    //Method runs before window is created, used for changing game settings
    @Override
    protected void window() {
        setTitle("Simple Example Game"); //Sets the title displayed at the top of the game window
        setBackground(Color.BLUE); //Background can be color or image
        create(); //Creates the window and runs the setup (no settings should be changed after this)
    }
    
    //Method runs once after window is created, used for setup and initialization
    @Override
    protected void setup() {
        timer = 0; //The countdown starts at zero
        image = loadImage("img/heart.png"); //Images should be loaded here for efficiency
        dot = loadImage("img/green.png"); //Folder names are seperated by slashes
        player = new Sprite(loadImage("img/ship.png")); //Sets the player to be a sprite represented by the ship image
        player.lockMovementArea(getArea()); //Doesn't allow the player to move outside the screen
        player.centerOn(getCenter()); //Centers the player on the middle of the screen
        player.setRelationalMovement(true); //Relational movement mean that the player directions are now relative to the player's angle
        dots = new Group(); //Initializes the dot group
    }
    
    //Method runs at a set FPS and contains main game code
    @Override
    protected void run() {
        //The key event constants can be used to check which key is pressed
        if (keyPressed(KeyEvent.VK_W)) player.move(Sprite.Direction.EAST); //Moves the player at the player's set speed forwar (relatively)
        if (keyPressed(KeyEvent.VK_D)) player.turn(Sprite.Rotation.CLOCKWISE); //Turns the player at the player's rotation speed
        if (keyPressed(KeyEvent.VK_A)) player.turn(Sprite.Rotation.COUNTER_CLOCKWISE);
        
        painter().setColor(Color.WHITE); //Painter color should be chosen before drawing
        painter().drawString("Hello World!", 200, 200); //The painter can be used for drawing simple shapes and text
        painter().drawImage(image, 200, 600, null); //Images can also be drawn with the painter
        
        timer--; //Increments countdown
        if (timer < 0) {
            dots.add(new Sprite(randomPosition(764, 764), dot)); //If timer completes, adds another dot in a random position
            timer = 60; //Restarts the timer
        }
        
        dots.remove(dots.getAllWithin(player)); //Removes all dots that collide with the player using rectangular collision
        dots.drawAll(); //Updates and draws all the sprites in the group
        player.draw(); //The draw method updates and displays the player
    }

}
