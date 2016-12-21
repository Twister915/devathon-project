package org.devathon.contest2016;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.devathon.contest2016.event.EventListener;
import org.devathon.contest2016.inject.DependencyInjector;
import org.devathon.contest2016.inject.Inject;
import org.devathon.contest2016.inject.Singleton;

@Singleton
public final class DevathonPlugin extends JavaPlugin {
    @Inject private EventListener listener;

    @SuppressWarnings("PointlessBinding")
    @Override
    public void onEnable() {
        DependencyInjector bind = new DependencyInjector()
                .bind(Plugin.class, this)
                .bind(JavaPlugin.class, this) //this is intentional I promise I understand it seems shitty
                .bind(DevathonPlugin.class, this);

        listener.listenEvent(PlayerJoinEvent.class, event -> {
            event.setJoinMessage("fuck");
        });
    }

    @Override
    public void onDisable() {
        // put your disable code here
    }
}

