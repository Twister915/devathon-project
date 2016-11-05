package org.devathon.contest2016.event;

import lombok.Synchronized;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.devathon.contest2016.inject.Inject;
import org.devathon.contest2016.inject.Parent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@ToString
public final class EventListener {
    @Inject private JavaPlugin plugin;
    @Parent private Object creator;

    private final Set<ListenerSub> listeners = new HashSet<>();

    @SuppressWarnings("unchecked")
    public <T extends Event> ListenerSub listenEvent(Class<T> eventType, EventPriority priority, boolean ignoreCancelled, ListenerCallback<T> callback) {
        Listener listener = new Listener() {};

        ListenerSub sub = new ListenerSub() {
            @Override
            public void unsubscribe() {
                unsubscribeNoRemove();
                listeners.remove(this);
            }

            @Override
            void unsubscribeNoRemove() {
                HandlerList.unregisterAll(listener);
            }
        };

        Bukkit.getPluginManager().registerEvent(eventType, listener, priority, (l, event) -> {
            try {
                callback.call((T) event);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to dispatch event for " + (creator == null ? "???" : creator.getClass().getSimpleName()) + "'s EventListener...");
                e.printStackTrace();
            }
        }, plugin, ignoreCancelled);

        registerListener(sub);
        return sub;
    }

    public <T extends Event> ListenerSub listenEvent(Class<T> type, EventPriority priority, ListenerCallback<T> callback) {
        return listenEvent(type, priority, false, callback);
    }

    public <T extends Event> ListenerSub listenEvent(Class<T> type, ListenerCallback<T> callback) {
        return listenEvent(type, EventPriority.NORMAL, callback);
    }

    @Synchronized
    private void registerListener(ListenerSub sub) {
        listeners.add(sub);
    }

    @Synchronized
    public void unregisterAll() {
        Iterator<ListenerSub> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().unsubscribeNoRemove();
            iterator.remove();
        }
    }

    public interface ListenerCallback<T> {
        void call(T event);
    }

    public abstract class ListenerSub {
        public abstract void unsubscribe();
        abstract void unsubscribeNoRemove();
    }
}
