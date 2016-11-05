package org.devathon.contest2016.inject;

import java.util.Collection;

public final class CompositeException extends RuntimeException {
    private final Collection<? extends Throwable> throwables;

    public CompositeException(Collection<? extends Throwable> throwables) {
        this.throwables = throwables;
//        printStackTrace();
    }

    @Override
    public void printStackTrace() {
        System.err.println("Multiple errors, printing all.");

        int index = 0;
        for (Throwable throwable : throwables) {
            System.err.println("Error " + (index++));
            throwable.printStackTrace();
        }
    }
}
