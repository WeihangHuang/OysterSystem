package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by apple on 12/3/17.
 */
public class PaymentsSystemAdapter implements Adapter {

    private static PaymentsSystemAdapter instance = new PaymentsSystemAdapter();

    public static PaymentsSystemAdapter getInstance() { return instance;}

    @Override
    public void charge(Customer var1, List<Journey> var2, BigDecimal var3) {
        PaymentsSystem.getInstance().charge(var1, var2, var3);
    }
}
