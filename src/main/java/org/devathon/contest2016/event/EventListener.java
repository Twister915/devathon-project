package org.devathon.contest2016.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.devathon.contest2016.DevathonPlugin;
import org.devathon.contest2016.inject.Inject;
import org.devathon.contest2016.inject.Singleton;

@Singleton
public final class EventListener {
    @Inject private DevathonPlugin plugin;

    @SuppressWarnings("unchecked")
    public <T extends Event> void listenEvent(Class<T> eventType, EventPriority priority, boolean ignoreCancelled, ListenerCallback<T> callback) {
        Listener listener = new Listener() {};

        Bukkit.getPluginManager().registerEvent(eventType, listener, priority, (l, event) -> {
            try {
                callback.call((T) event);
            } catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to dispatch event to a listener...");
            }
        }, plugin, ignoreCancelled);
    }

    public interface ListenerCallback<T> {
        void call(T event);
    }
}
