package org.devathon.contest2016.command;

public @interface CommandMeta {
    String name();
    String permission() default "";
    String usage() default "";
}
