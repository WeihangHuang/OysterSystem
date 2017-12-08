package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;

import java.util.List;
import java.util.UUID;

/**
 * Created by apple on 12/4/17.
 */
public class CustomerCustomerDatabaseInterface implements CustomerDatabaseInterface {

    private CustomerDatabase database;

    private static CustomerCustomerDatabaseInterface instance = new CustomerCustomerDatabaseInterface();

    public static CustomerCustomerDatabaseInterface getInstance() { return instance;}

    @Override
    public List<Customer> getCustomers() {
        database = CustomerDatabase.getInstance();
        return database.getCustomers();
    }

    @Override
    public boolean isRegisteredId(UUID cardId) {
        return CustomerDatabase.getInstance().isRegisteredId(cardId);
    }
}
