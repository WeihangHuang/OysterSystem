package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;

import java.util.List;
import java.util.UUID;

/**
 * Created by apple on 12/4/17.
 */
public class CustomerDatabaseAdapter implements DatabaseAdapter {

    private CustomerDatabase database;

    private static CustomerDatabaseAdapter instance = new CustomerDatabaseAdapter();

    public static CustomerDatabaseAdapter getInstance() { return instance;}

    @Override
    public List<Customer> getCustomers() {
        database = CustomerDatabase.getInstance();
        return database.getCustomers();
    }

    @Override
    public boolean isRegisteredId(UUID cardId) {
        return database.isRegisteredId(cardId);
    }
}
