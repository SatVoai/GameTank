package tankGame.gameObjects.pickups;

import tankGame.TankGameWorld;
import tankGame.gameObjects.Tank;
import tankGame.weapons.ShotgunWeapon;
import utility.SoundHandler;
import utility.Vector2;

public class ShotgunPickup extends Pickup {
  public ShotgunPickup(Vector2 position) {
    super(position, 4);
    SoundHandler.getInstance().loadSound("weaponequip.wav");
  }

  @Override
  public void activatePickup(Tank tank) {
    tank.equip(new ShotgunWeapon());
    SoundHandler.getInstance().playSound("weaponequip.wav");
    die();
  }
}
