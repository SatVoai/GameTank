package tankGame.gameObjects.projectiles;

import tankGame.TankGameWorld;
import utility.RandomNumberGenerator;
import utility.SpriteHandler;
import utility.Vector2;

import java.awt.*;

public class FlamethrowerProjectile extends Projectile {

  public FlamethrowerProjectile(Vector2 position, int lifeTime, TankGameWorld.Player owner) {
    super(position, lifeTime, owner);
    moveSpeed = 5;
    damage = 4;
    sprite = SpriteHandler.getInstance().loadSprite("flamethrow.png");
  }

  @Override
  public void setRotation(double rotation) {
    this.rotation = rotation + RandomNumberGenerator.getSpread(20);
  }

  @Override
  public void drawSprite(Graphics graphics) {
    if (sprite != null) {
      double xPos = position.x - sprite.getWidth()/2;
      double yPos = position.y - sprite.getHeight()/2;
      graphics.drawImage(sprite, (int)xPos, (int)yPos, null);
    }
  }
}
