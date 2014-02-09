package com.roboticoverlord.micro.bukkit;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class MinecartStarter extends JavaPlugin implements Listener {

  private ArrayList<Entity> mcarts;

  @Override
  public void onEnable() {
    getServer().getPluginManager().registerEvents(this, this);
    mcarts = new ArrayList<Entity>();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (label.equalsIgnoreCase("minecart")) {
      if (sender instanceof Player) {
        if (sender.hasPermission("minecartstarter.can") || sender.isOp()) {
          Material MaterialUnderSender = ((Player) sender).getWorld().getBlockAt(((Player) sender).getLocation()).getType();
          switch (MaterialUnderSender) {
            case RAILS:
            case ACTIVATOR_RAIL:
            case DETECTOR_RAIL:
            case POWERED_RAIL:

              Minecart newMinecart = ((Player) sender).getWorld().spawn(((Player) sender).getLocation(), Minecart.class);
              Vector newMinecartVelocity = newMinecart.getVelocity();

              double playerRotation = (((Player) sender).getLocation().getYaw() - 90.0F) % 360.0F;
              if (playerRotation < 0.0D) {
                playerRotation += 360.0D;
              }

              CardinalDirection playerCardinalDirection = getDirection(playerRotation);
              switch (playerCardinalDirection) {
                case North:
                  newMinecartVelocity.setX(-8);
                  break;
                case East:
                  newMinecartVelocity.setZ(-8);
                  break;
                case South:
                  newMinecartVelocity.setX(8);
                  break;
                case West:
                  newMinecartVelocity.setZ(8);
                  break;
              }

              newMinecart.setPassenger((Player) sender);
              newMinecart.setVelocity(newMinecartVelocity);
              mcarts.add(newMinecart);
              break;
            default:
              sender.sendMessage("| MinecartStarter | You're not standing on a track!");
              break;
          }
        } else
          sender.sendMessage("| MinecartStarter | You don't have permission!");
      } else
        sender.sendMessage("| MinecartStarter | You must be a player to perform this command!");
    }
    return true;
  }

  private enum CardinalDirection {

    North,
    East,
    South,
    West,
    Unknown
  }

  private CardinalDirection getDirection(double degrees) {
    if (degrees <= 45.0D || degrees > 315.0D) {
      return CardinalDirection.North;
    }
    if (degrees > 45.0D && degrees <= 135.0D) {
      return CardinalDirection.East;
    }
    if (degrees > 135.0D && degrees <= 225.0D) {
      return CardinalDirection.South;
    }
    if (degrees > 225.0D && degrees <= 315.0D) {
      return CardinalDirection.West;
    }
    return CardinalDirection.Unknown;
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerTeleport(PlayerTeleportEvent e) {
    if (e.getPlayer().isInsideVehicle()) {
      if (mcarts.contains(e.getPlayer().getVehicle())) {
        e.getPlayer().leaveVehicle();
      }
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onVehicleExit(VehicleExitEvent e) {
    if (e.getVehicle() instanceof Minecart) {
      if (mcarts.contains(e.getVehicle())) {
        e.getVehicle().remove();
        mcarts.remove(e.getVehicle());
      }
    }
  }
}