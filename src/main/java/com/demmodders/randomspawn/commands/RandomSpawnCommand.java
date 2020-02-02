package com.demmodders.randomspawn.commands;


import com.demmodders.datmoddingapi.delayedexecution.DelayHandler;
import com.demmodders.datmoddingapi.delayedexecution.delayedevents.DelayedTeleportEvent;
import com.demmodders.datmoddingapi.structures.Location;
import com.demmodders.randomspawn.Util;
import com.demmodders.randomspawn.config.RandomSpawnConfig;
import com.demmodders.randomspawn.structures.Player;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Teleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RandomSpawnCommand extends CommandBase {

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public String getName() {
        return "randomspawn";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TextFormatting.GOLD + "/spawn [player] - " + (RandomSpawnConfig.saveSpawn ? "Teleports you to your personal spawn, use \"/spawnreset\" for a new spawn" : "Teleports you to a random place in the world");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        // Ensure its either being called by a player, or on a player
        if (!(sender instanceof EntityPlayerMP) && args.length != 1) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "As the server, you can only make specific players go to spawn, use: /spawn <player>"));
            return;
        }

        // If its by a player, make sure they have the permissions
        if (sender instanceof EntityPlayerMP) {
            if (!PermissionAPI.hasPermission((EntityPlayerMP) sender, "datrandomteleport.rspawn.spawn") || (!PermissionAPI.hasPermission((EntityPlayerMP) sender, "datrandomteleport.rspawn.spawnother") && args.length > 0)) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "You don't have permission to do that"));
                return;
            }
        }

        // Set the target to their argrument if they have provided one
        Player player;
        EntityPlayerMP target;
        if (args.length != 0) {
            target = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(args[0]);
        } else {
            target = (EntityPlayerMP) sender;
        }

        // Tell the player if we can't find their argument
        if (target != null){
            player = Util.getPlayer(target.getUniqueID());
            if(args.length != 0) sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "Teleporting " + args[0] + " to their spawn"));
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Unable to find that player"));
            return;
        }

        // work out what to say to the player
        if (RandomSpawnConfig.saveSpawn && player != null) {
            if (target == sender && player.lastTeleport + (RandomSpawnConfig.spawnReDelay * 1000) > System.currentTimeMillis()){
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "you cannot teleport to spawn for another " + (RandomSpawnConfig.spawnReDelay - ((((System.currentTimeMillis())) - player.lastTeleport)/1000)) + " seconds"));
                return;
            }
            target.sendMessage(new TextComponentString(TextFormatting.GOLD + "Telporting to your spawn" + (RandomSpawnConfig.spawnDelay > 0 ? " in " + RandomSpawnConfig.spawnDelay + " seconds" : "")));
        } else {
            target.sendMessage(new TextComponentString(TextFormatting.GOLD + "Telporting to a random destination" + (RandomSpawnConfig.spawnDelay > 0 ? "in " + RandomSpawnConfig.spawnDelay + " seconds" : "")));
        }

        // Create the teleport event and wait for the delay
        DelayHandler.addEvent(new DelayedSpawnEvent(target, RandomSpawnConfig.spawnDelay));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getAliases() {
        ArrayList<String> aliases = new ArrayList<>();
        aliases.add("spawn");
        return aliases;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1){
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}