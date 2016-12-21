package org.devathon.contest2016.event;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unchecked")
/**
 * Represents the simplest implementation of a thread safe (single thread can post or be dispatched to at the same time) event loop
 *
 * Allows unsubscription by listeners using the returned EventSubscription instance from the subscribe method
 *
 * Has two event modes, TICK and IMMEDIATE which are typically used when events come from many threads but are consumed on a single thread
 *
 * The thread that dispatches an event will be the one that an event is consumed on if the mode is IMMEDIATE
 *
 * in TICK mode the thread of consumption is entirely up to the called of the tick() method
 *
 * Methods that make no sense (calling tick() in immediate mode) will throw an IllegalStateException
 */
public final class EventTwister<ET> {
    public enum EventMode {
        TICK,
        IMMEDIATE
    }

    public interface EventListener<T> {
        void call(T event) throws Exception;
    }

    public interface EventSubscription {
        void unsubscribe();
        boolean isSubscribed();
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final Multimap<Class<? extends ET>, EventListener<? extends ET>> listeners =
            //multimap with a linked hash   and a linked list (cuz iteration and append)
            MultimapBuilder.linkedHashKeys().linkedListValues().build();
    private Queue<ET> eventQueue;
    private EventMode eventMode = EventMode.IMMEDIATE;

    public void setEventMode(EventMode mode) {
        try {
            lock.lock();
            if (this.eventMode == mode)
                return;

            this.eventMode = mode;

            if (eventMode == EventMode.TICK)
                eventQueue = new LinkedList<>();
            else if (eventMode == EventMode.IMMEDIATE) {
                if (eventQueue != null)
                    tick();

                eventQueue = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public <E extends ET> EventSubscription subscribe(Class<E> type, EventListener<E> listener) {
        try {
            lock.lock();
            listeners.put(type, listener);
        } finally {
            lock.unlock();
        }
        return new EventSubscription() {
            private boolean subscribed = true;

            @Override
            public void unsubscribe() {
                listeners.remove(type, listener);
                subscribed = false;
            }

            @Override
            public boolean isSubscribed() {
                return subscribed;
            }
        };
    }

    public <T extends ET> void post(T event) {
        switch (eventMode) {
            case IMMEDIATE:
                actualDispatch(event);
                return;
            case TICK:
                if (eventQueue == null)
                    throw new IllegalStateException();
                try {
                    lock.lock();
                    eventQueue.add(event);
                } finally {
                    lock.unlock();
                }
        }
    }

    public void tick() {
        if (eventMode != EventMode.TICK)
            throw new IllegalStateException();

        try {
            lock.lock();
            eventQueue.forEach(this::actualDispatch);
            eventQueue.clear();
        } finally {
            lock.unlock();
        }
    }

    public void breakAll() {
        if (eventMode == EventMode.TICK)
            tick();

        listeners.clear();
        eventMode = null;
        eventQueue = null;
    }

    private <T extends ET> void actualDispatch(T event) {
        try {
            lock.lock();
            for (EventListener<? extends ET> eventListener : listeners.get((Class<? extends ET>) event.getClass()))
                try {
                    ((EventListener<T>) eventListener).call(event);
                } catch (Exception e) {
                    System.err.println("Could not dispatch event to a listener due to an exception that was propigated...");
                    e.printStackTrace();
                }
        } finally {
            lock.unlock();
        }
    }
}
