package com.tfl.billing;

import com.tfl.external.Customer;

import java.util.List;

/**
 * Created by apple on 12/4/17.
 */
public interface DatabaseAdapter {

    List<Customer> getCustomers();

}
