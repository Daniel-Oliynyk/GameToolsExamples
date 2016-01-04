package gametoolsexamples;

//These two imports are recommended for easier use of library
import gametools.*;
import static gametools.Tools.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

//Main project class must extend game.
public class Space extends Game {
    //Giving values to variables won't work up here, do it in setup instead
    Sprite player; //Sprites are the characters and objects in your game that move, animate and have collision
    Group stars, bullets, plasma, enemies, explosions; //A group can be used for bulk commands to multiple sprites
    Animation explosionSmall, explosionLarge; //Animations are drawn on sprites and graphics
    BufferedImage missile, plasmaBolt, alien; //Images should be loaded once for efficiency
    Random random;
    int score, health, bulletTimer, enemyTimer;
    
    //The main java method that java always runs first
    public static void main(String[] args) {
        initialize(Space.class); //Sets up the tool class to find the base of the project for loading images
        new Space(); //Need to include this for game to run
    }
    
    //Method runs before window is created, used for changing game settings
    @Override
    protected void window() {
        setTitle("Example Space Game");
        setBackground(new Color(0x0b1037)); //Background can be color or image
        create(); //Creates the window and runs the setup (no settings should be changed after this)
    }
    
    //Method runs once after window is created, used for setup and initialization
    @Override
    protected void setup() {
        //Every project wide variable should be set here so that the game can easily be restarted by running the setup method
        player = new Sprite(loadImage("img/ship.png"));
        player.lockMovementArea(getArea()); //Doesn't allow the player to move outside the screen
        player.centerOn(getCenter());
        
        //Groups are similar to array lists but with additional properties
        enemies = new Group();
        explosions = new Group();
        stars = new Group();
        
        //Generates random star background
        BufferedImage star = loadImage("img/star.png");
        for (int i = 0; i < 200; i++) stars.add(new Sprite(randomPosition(getArea()), star));
        
        //Groups can automatically remove objects when they go off the screen
        bullets = new Group();
        bullets.removeWhenOffScreen();
        plasma = new Group();
        plasma.removeWhenOffScreen();
        
        //Generates an animation from a spritesheet using the passed in dimensions, sets the animation speed to two, and sets repeats to zero
        explosionSmall = new Animation(loadSpriteSheet("img/explosion-small.png", dm(30, 30)), 2, 0);
        explosionLarge = new Animation(loadSpriteSheet("img/explosion-large.png", dm(60, 60)), 2, 0);
        
        //Images should be loaded here for efficiency
        missile = loadImage("img/missile.png");
        plasmaBolt = loadImage("img/plasma.png");
        alien = loadImage("img/alien.png");
        
        //Game timers and score should be set and reset here
        random = new Random();
        score = 0;
        health = 100;
        bulletTimer = 0;
        enemyTimer = 0;
        
        //Increases font size
        Font large = new Font("Arial", Font.PLAIN, 16);
        painter().setFont(large);
    }
    
    //Method runs at a set FPS and contains main game code
    @Override
    protected void run() {
        player.face(mousePosition()); //Player always shoots toward mouse location
        int hor = 0, ver = 0; //Horizontal and vertical variables are lines on a compass
        //Key input can be handled by key pressed method
        if (keyPressed(KeyEvent.VK_W)) ver++;
        if (keyPressed(KeyEvent.VK_A)) hor--;
        if (keyPressed(KeyEvent.VK_S)) ver--;
        if (keyPressed(KeyEvent.VK_D)) hor++;
        //Horizontal and vertical variables can be combined to point where on the compass the player needs to go
        player.move(hor, ver);
        
        bulletTimer--;
        if ((keyPressed(KeyEvent.VK_SPACE) || mousePressed()) && bulletTimer < 0) {
            Sprite bullet = new Sprite(missile);
            //Bullet starts underneath center of player and at the same angle
            bullet.centerOn(player);
            bullet.setAngle(player.getAngle());
            bullet.setSpeed(10);
            bullet.setRelationalMovement(true); //Relational movement means the bullet's compass is in relation to its angle
            //The custom script acts on the bullet every frame
            bullet.script(new Script(bullet) {
                @Override
                public void update() {
                    //Bullet simply moves forward (in relation to its angle) every frame
                    sprite().move(Sprite.Direction.EAST);
                }
            });
            //After the bullet gets added to the group the cooldown restarts
            bullets.add(bullet);
            bulletTimer = 15;
        }
        
        enemyTimer--;
        if (enemyTimer < 0) {
            Sprite enemy = new Sprite(alien);
            //A random roll decides which part of the screen the enemy spawns in
            switch (random.nextInt(4)) {
                case 0:
                    //Left part of screen
                    enemy.setPosition(pt(-alien.getWidth(), random.nextInt(getHeight())));
                    break;
                case 1:
                    //Right part of screen
                    enemy.setPosition(pt(getWidth(), random.nextInt(getHeight())));
                    break;
                case 2:
                    //Top part of screen
                    enemy.setPosition(pt(random.nextInt(getWidth()), -alien.getHeight()));
                    break;
                case 3:
                default:
                    //Bottom part of screen
                    enemy.setPosition(pt(random.nextInt(getWidth()), getHeight()));
                    break;
            }
            enemy.face(player);
            enemy.setRelationalMovement(true); //Enemy also moves relationally for simplicity
            enemy.script(new Script(enemy) {
                //Variables can be stored within script object
                int moveTimer, shootTimer;
                boolean move;
                
                @Override
                public void update() {
                    moveTimer--;
                    if (moveTimer < 0) {
                        move = !move; //Enemy flips between moving and shooting
                        //Timer is longer for shooting than moving
                        moveTimer = move? random.nextInt(60) + 20 : random.nextInt(100) + 100;
                        shootTimer = 0;
                    }
                    if (move) sprite().move(Sprite.Direction.EAST); //If currently in movement mode, sprite simply moves forward
                    else {
                        sprite().turnTo(player); //Gradually turn to face player
                        shootTimer--;
                        if (shootTimer < 0) {
                            //Adds a bullet pretty much the same way as the player, except a different image
                            Sprite bullet = new Sprite(plasmaBolt);
                            bullet.centerOn(sprite());
                            bullet.setAngle(sprite().getAngle());
                            bullet.setSpeed(10);
                            bullet.setRelationalMovement(true);
                            bullet.script(new Script(bullet) {
                                @Override
                                public void update() {
                                    sprite().move(Sprite.Direction.EAST);
                                }
                            });
                            plasma.add(bullet);
                            shootTimer = 30;
                        }
                    }
                }
            });
            enemies.add(enemy);
            //Game gets gradually harder depending on score until minimum spawn time is reached
            int difficulty = 100 - ((score / 5) * 2);
            if (difficulty > 0) enemyTimer = random.nextInt(difficulty) + 50;
            else enemyTimer = 50;
        }
        
        Group newExplosions = new Group(); //The temporary group to collect all objects that need to explode
        
        //Take damage from enemy bullets
        List<Sprite> bulletDamage = plasma.getAllWithin(player); //Collects all enemy bullets that collide with the player
        health -= bulletDamage.size() * 5; //Subracts five health for each bullet
        plasma.remove(bulletDamage); //Removes the bullets from the bullet group
        newExplosions.add(bulletDamage); //Moves the bullets to the explosion group
        
        //Take damage from enemy ships
        List<Sprite> shipDamage = enemies.getAllWithin(player); //Collects all enemy ships that collide with the player
        health -= shipDamage.size() * 10; //Subracts ten health for each bullet
        enemies.remove(shipDamage); //Removes the ships from the enemy group
        newExplosions.add(shipDamage); //Moves the ships to the explosion group
        
        //Destroy enemies
        List<Sprite> kills = enemies.getAllWithin(bullets); //Collects all enemy ships that collide with the player bullets
        score += kills.size() * 15; //Adds fifteen points for each kill
        enemies.remove(kills); //Removes the ships from the enemy group
        newExplosions.add(kills); //Moves the ships to the explosion group
        
        //Bullet collisions
        List<Sprite> collisions = bullets.getAllWithin(plasma); //Collects all player bullets that collide with enemy bullets
        bullets.remove(collisions); //Removes the player bullets from the bullet group
        plasma.remove(plasma.getAllWithin(collisions)); //Removes the enemy bullets from the bullet group
        newExplosions.add(collisions); //Moves the bullets to the explosion group
        //Converts all new objects added to the explosion group to the properly sized explosion
        
        for (Sprite explosion : newExplosions.getAll()) {
            Position center = explosion.getCenter(); //Saves the current center of the object
            //Changes the object animation to the properly sized explosion
            if (explosion.getWidth() > 50) explosion.setAnimation(new Animation(explosionLarge));
            else explosion.setAnimation(new Animation(explosionSmall));
            explosion.centerOn(center); //Recenters explosion
            explosion.script(new Script(explosion) {
                @Override
                public void update() {
                    //As soon as explosion animation completes, it safely removes itself
                    if (sprite().getAnimation().isComplete()) sprite().remove(true);
                }
            });
        }
        explosions.add(newExplosions); //Moves all temporary explosions to the real explosion group
        
        //Shows popup with score and restarts game
        if (health <= 0) {
            messageDialog("You died with a score of " + score);
            setup(); //Since all project wide variables were set here running the setup resets the game
        }
        
        //Draws all the groups and the player
        stars.drawAll();
        bullets.drawAll();
        plasma.drawAll();
        enemies.drawAll();
        player.draw();
        explosions.drawAll();
        
        //Draws the score
        painter().setColor(Color.WHITE);
        painter().drawString(score + "", 15, 25); //The painter object can be used for drawing basic shapes and colors
        
        //Draws the health bar
        painter().setColor(Color.RED);
        painter().fillRect(getWidth() - 165, 15, 150, 15);
        painter().setColor(health > 50? Color.GREEN : Color.YELLOW);
        painter().fillRect(getWidth() - 165, 15, (int) (health * 1.5), 15);
        painter().setColor(Color.BLACK);
        painter().drawRect(getWidth() - 165, 15, 150, 15);
        painter().drawRect(getWidth() - 165, 15, (int) (health * 1.5), 15);
    }
}
