package com.tfl.billing;

import com.tfl.external.Customer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by apple on 12/3/17.
 */
public interface PaymentsSystemInterface {

    void charge(Customer var1, List<Journey> var2, BigDecimal var3);
}
