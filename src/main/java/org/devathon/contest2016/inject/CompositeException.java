package org.devathon.contest2016.inject;

import java.io.PrintWriter;
import java.util.Collection;

public final class CompositeException extends RuntimeException {
    private final Collection<? extends Throwable> throwables;

    public CompositeException(Collection<? extends Throwable> throwables) {
        this.throwables = throwables;
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        s.println("Multiple errors, printing all.");

        int index = 0;
        for (Throwable throwable : throwables) {
            s.println("Error " + (index++));
            throwable.printStackTrace();
        }
    }
}
