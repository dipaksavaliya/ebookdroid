package org.emdev.ui;

public interface ActivityEvents {

    int BEFORE_CREATE = 1 << 0;
    int ON_RECREATE = 1 << 1;
    int ON_CREATE = 1 << 2;
    int AFTER_CREATE = 1 << 3;
    int ON_RESTART = 1 << 4;
    int ON_START = 1 << 5;
    int ON_POST_CREATE = 1 << 6;
    int ON_RESUME = 1 << 7;
    int ON_POST_RESUME = 1 << 8;
    int ON_PAUSE = 1 << 9;
    int ON_STOP = 1 << 10;
    int ON_DESTROY = 1 << 11;

    int ACTIVITY_EVENTS = ON_CREATE | ON_RESTART | ON_START | ON_POST_CREATE | ON_RESUME | ON_PAUSE | ON_STOP
            | ON_DESTROY;

    int CONTROLLER_EVENTS = BEFORE_CREATE | ON_RECREATE | AFTER_CREATE | ON_RESTART | ON_START | ON_POST_CREATE
            | ON_RESUME | ON_PAUSE | ON_STOP | ON_DESTROY;

}
