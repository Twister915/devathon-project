package org.devathon.contest2016.command;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.devathon.contest2016.inject.Inject;

public abstract class Command {
    @Inject @Getter private JavaPlugin plugin;

    protected abstract void execute(CommandSender sender, String[] args) throws CommandException;

    protected void handleException(CommandSender sender, CommandException e) {

    }
}
