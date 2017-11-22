package com.tfl.billing;

import org.junit.Assert;
import org.junit.Test;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;

/**
 * Created by apple on 11/23/17.
 */
public class JourneyStartTest {

    private final UUID cardId_test = UUID.randomUUID();
    private final UUID readerId_test = UUID.randomUUID();

    private final long timeBeforeObjectCreated = System.currentTimeMillis();
    private JourneyEvent newEvent = new JourneyStart(cardId_test, readerId_test);
    private final long timeAfterObjectCreated = System.currentTimeMillis();

    @Test
    public void returnCardIdTest() {
        assertThat(newEvent.cardId(), is(cardId_test));
    }

    @Test
    public void returnReaderIdTest(){
        assertThat(newEvent.readerId(),is(readerId_test));
    }

    @Test
    public void returnTimeTest(){
        assertTrue(newEvent.time() >= timeBeforeObjectCreated);
        assertTrue(newEvent.time() <= timeAfterObjectCreated);
    }



}