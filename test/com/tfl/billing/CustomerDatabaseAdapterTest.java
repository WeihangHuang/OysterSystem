package com.tfl.billing;

import com.oyster.OysterCard;
import com.tfl.external.Customer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by apple on 12/8/17.
 */
public class CustomerDatabaseAdapterTest {

    private CustomerDatabaseAdapter database = CustomerDatabaseAdapter.getInstance();

    @Test
    public void canRetrieved(){
        List<Customer> customers = database.getCustomers();

        assertFalse(customers.isEmpty());

    }

    @Test
    public void canCheckRegistered(){
        OysterCard unregisteredCard = new OysterCard("ffffffff-ffff-ffff-ffff-ffffffffffff");
        boolean card_state = database.isRegisteredId(unregisteredCard.id());

        assertFalse(card_state);
    }


}