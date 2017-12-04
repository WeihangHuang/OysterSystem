package com.tfl.billing;

import java.util.UUID;

public class JourneyEnd extends JourneyEvent {

    public JourneyEnd(UUID cardId, UUID readerId, Clock clock) {
        super(cardId, readerId, clock);
    }
}
