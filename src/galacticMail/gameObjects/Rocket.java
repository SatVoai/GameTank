package galacticMail.gameObjects;

import galacticMail.GalacticMailPanel;
import galacticMail.GalacticMailWorld;
import galacticMail.PointsHandler;
import galacticMail.RocketKeyInput;
import general.GamePanel;
import general.GameWorld;
import utility.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CopyOnWriteArrayList;

public class Rocket extends SpaceObject {
  private final double MOVE_SPEED = 0.07;
  private final double TURN_SPEED = 1;
  private final double MAX_VELOCITY = 4.5;
  private final int LANDING_TIME = 24;
  private final int INVULNERABILITY_TIME = 24;
  private final int POINTS_FOR_LANDING = 500;
  private final int POINTS_LOST_FOR_STAYING = 1;

  private GalacticMailWorld galacticMailWorld;

  private Vector2 velocityVector = new Vector2(0, 0);
  private RocketKeyInput rocketKeyInput;
  private MultiSprite flyingMultiSprite;
  private MultiSprite landedMultiSprite;
  private Moon dockedMoon = null;

  private Timer landingTimer = new Timer();
  private Timer invulnerabilityTimer = new Timer();

  public Rocket(Vector2 position) {
    // Spawn slightly above position
    super(Vector2.addVectors(position, new Vector2(0, -16)), 24);
    setRotation(90);

    if (GameWorld.getInstance() instanceof GalacticMailWorld) {
      galacticMailWorld = (GalacticMailWorld)GameWorld.getInstance();
    }

    rocketKeyInput = new RocketKeyInput();
    initializeSpawnMoon();
    initializeSprites();
    preloadSounds();
  }

  private void initializeSpawnMoon() {
    Moon myMoon = (Moon)GameWorld.getInstance()
            .instantiate(new SpawnMoon(position));
    dock(myMoon);

    initializeSprites();
  }

  private void initializeSprites() {
    BufferedImage spriteStrip = SpriteHandler.getInstance()
            .loadSprite("Flying_strip72.png");
    flyingMultiSprite = new MultiSprite(spriteStrip, 72);
    spriteStrip = SpriteHandler.getInstance()
            .loadSprite("Landed_strip72.png");
    landedMultiSprite = new MultiSprite(spriteStrip, 72);
    SpriteHandler.getInstance().loadSprite("Explosion_space_strip9.png");
    renderingLayerIndex = 3;
  }

  private void preloadSounds() {
    SoundHandler.getInstance().loadSound("Launch.wav");
    SoundHandler.getInstance().loadSound("landing.wav");
    SoundHandler.getInstance().loadSound("Explosion.wav");
    SoundHandler.getInstance().loadSound("LifeGet.wav");
  }

  @Override
  public void update() {
    turn();
    move();
    launch();
    checkCollisions();
    losePoints();
  }

  private void turn() {
    double rotationalVelocity = rocketKeyInput.getTurnInput()*TURN_SPEED;
    if (dockedMoon != null) {
      rotationalVelocity *= 6;
    }
    setRotation(rotation - rotationalVelocity);
  }

  private void move() {
    if (dockedMoon == null) {
      Vector2 newMoveVector = Vector2.newRotationMagnitudeVector(rotation, MOVE_SPEED);
      velocityVector = Vector2.addVectors(velocityVector, newMoveVector);
      velocityVector.clampMagnitude(MAX_VELOCITY);
      setPosition(Vector2.addVectors(position, velocityVector));
    }
    else {
      velocityVector = new Vector2(0, 0);
      if (!landingTimer.isDone()) {
        Vector2 moveVector = Vector2.getMoveVectorOverTime(position,
                dockedMoon.getPosition(), landingTimer.getTime());
        setPosition(Vector2.addVectors(position, moveVector));
      }
      else {
        setPosition(dockedMoon.getPosition());
      }
    }
  }

  private void launch() {
    if (rocketKeyInput.getLaunchPressed() && dockedMoon != null
            && landingTimer.isDone()) {
      dockedMoon.destroy();
      dockedMoon = null;
      velocityVector = Vector2.newRotationMagnitudeVector(rotation, MOVE_SPEED*32);
      invulnerabilityTimer.set(INVULNERABILITY_TIME);
      SoundHandler.getInstance().playSound("Launch.wav");
    }
  }

  private void checkCollisions() {
    GameWorld gameWorld = GameWorld.getInstance();
    if (alive && gameWorld instanceof GalacticMailWorld) {
      GalacticMailWorld galacticMailWorld = (GalacticMailWorld)gameWorld;

      if (dockedMoon == null) {
        CopyOnWriteArrayList<Moon> moons = galacticMailWorld.getMoons();
        for (Moon moon : moons) {
          if (trigger.isOverlapping(moon.getTrigger())) {
            dock(moon);
          }
        }
      }
      if (dockedMoon == null && invulnerabilityTimer.isDone()) {
        CopyOnWriteArrayList<Asteroid> asteroids = galacticMailWorld.getAsteroids();
        for (Asteroid asteroid : asteroids) {
          if (trigger.isOverlapping(asteroid.getTrigger())) {
            die();
          }
        }
      }
      CopyOnWriteArrayList<Planet> planets = galacticMailWorld.getPlanets();
      for (Planet planet : planets) {
        if (trigger.isOverlapping(planet.getTrigger()) && planet.isAlive()) {
          planet.destroy();
          ((GalacticMailPanel) GamePanel.getInstance()).addLife();
          SoundHandler.getInstance().playSound("LifeGet.wav");
        }
      }
    }
  }

  // Lose points if rocket is docked, but not if game is over.
  private void losePoints() {
    if (dockedMoon != null && !galacticMailWorld.isGameOver()) {
      PointsHandler.getInstance().losePoints(POINTS_LOST_FOR_STAYING);
    }
  }

  private void dock(Moon moon) {
    dockedMoon = moon;
    landingTimer.set(LANDING_TIME);

    if (!(moon instanceof SpawnMoon)) {
      PointsHandler.getInstance().addPoints(POINTS_FOR_LANDING);
      SoundHandler.getInstance().playSound("landing.wav");
    }

    // win condition
    if (galacticMailWorld.getMoons().size() == 1) {
      galacticMailWorld.setGameState(GalacticMailWorld.GameState.Victory);
    }
  }

  @Override
  public void drawSprite(Graphics graphics) {
    if (dockedMoon == null || !landingTimer.isDone()) {
      sprite = flyingMultiSprite.getSubSpriteByRotation(rotation);
    }
    else {
      sprite = landedMultiSprite.getSubSpriteByRotation(rotation);
    }
    super.drawSprite(graphics);
  }

  @Override
  public void die(){
    super.die();
    GameWorld.getInstance().instantiate(new Explosion(position, rotation));
    SoundHandler.getInstance().playSound("Explosion.wav");
  }
}
