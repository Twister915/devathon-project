package org.devathon.contest2016.event;

import lombok.Data;

public class UsingEventLoop {
    public static void main(String[] args) {
        EventTwister<Object> objectEventTwister = new EventTwister<>();

        //....

        EventTwister.EventSubscription handle = objectEventTwister.subscribe(PlayerMovedEvent.class, event -> {
            System.out.printf("Player %s moved to %d, %d, %d", event.getPlayerName(), event.getNewX(), event.getNewY(), event.getNewZ());
        });

        objectEventTwister.post(new PlayerMovedEvent("Joey", 1, 2, 3));
        //prints the thing above

        handle.unsubscribe();
        objectEventTwister.post(new PlayerMovedEvent("Joey", 4, 5, 6));
        //does nothing

        objectEventTwister.breakAll();
        //releases all other listeners from memory and stuff like that
    }

    @Data
    private static final class PlayerMovedEvent {
        private final String playerName;
        private final int newX, newY, newZ;
    }
}
