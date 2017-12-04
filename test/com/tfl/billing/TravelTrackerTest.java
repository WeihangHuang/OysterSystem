package com.tfl.billing;

import com.oyster.OysterCard;
import com.oyster.OysterCardReader;
import com.tfl.external.Customer;
import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by apple on 12/4/17.
 */
public class TravelTrackerTest {

    private class ControllableClock implements Clock{
        long time;

        @Override
        public long getTime() {
            return time;
        }

        public void setTime(int hour, int minutes){
            time = (hour * 60 + minutes) * 60 * 1000;
        }
    }

    private final OysterCard newCard = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    private final Customer customer = new Customer("Fred Bloggs", newCard);

    private List<Customer> CUSTOMERS = new ArrayList<>();
    private List<Journey> journey;
    private BigDecimal costTotal;

    ControllableClock clock = new ControllableClock();



    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    DatabaseAdapter database = context.mock(DatabaseAdapter.class);
    Adapter adapter = context.mock(Adapter.class);

    TravelTracker tracker = new TravelTracker(database, adapter, clock);


    OysterCardReader paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
    OysterCardReader bakerStreetReader = OysterReaderLocator.atStation(Station.BAKER_STREET);
    OysterCardReader kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);



    @Test
    public void oneCustomerNoJourneysTest(){ //also test getting and sending data

        CUSTOMERS.add(customer);
        journey = new ArrayList<>();
        costTotal= new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers(); will(returnValue(CUSTOMERS));

            exactly(1).of(adapter).charge(customer, journey, costTotal);
        }});

        tracker.chargeAccounts();

    }

    @Test
    public void oneCustomerOneNonPeakJourneysTest(){
        CUSTOMERS.add(customer);
        costTotal = new BigDecimal(2.4).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(adapter).charge(with(equal(customer)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(newCard);

        clock.setTime(0, 5);
        bakerStreetReader.touch(newCard);


        tracker.chargeAccounts();

    }

    @Test
    public void oneCustomerOnePeakJourneysTest() {
        CUSTOMERS.add(customer);
        costTotal = new BigDecimal(3.2).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations() {{
            exactly(1).of(database).getCustomers();
            will(returnValue(CUSTOMERS));
            exactly(1).of(adapter).charge(with(equal(customer)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(7, 30);
        paddingtonReader.touch(newCard);

        clock.setTime(10, 5);
        bakerStreetReader.touch(newCard);


        tracker.chargeAccounts();
    }

    @Test
    public void oneCustomerTwoPeakJourneysTest() {
        CUSTOMERS.add(customer);
        costTotal = new BigDecimal(6.4).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations() {{
            exactly(1).of(database).getCustomers();
            will(returnValue(CUSTOMERS));
            exactly(1).of(adapter).charge(with(equal(customer)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader, kingsCrossReader);

        clock.setTime(7, 0);
        paddingtonReader.touch(newCard);

        clock.setTime(9, 5);
        bakerStreetReader.touch(newCard);

        clock.setTime(18,0);
        bakerStreetReader.touch(newCard);

        clock.setTime(20,0);
        kingsCrossReader.touch(newCard);

        tracker.chargeAccounts();
    }

    @Test
    public void twoCustomerOnePeakJourneysTest() {
        CUSTOMERS.add(customer);
        costTotal = new BigDecimal(3.2).setScale(2, BigDecimal.ROUND_HALF_UP);

        OysterCard newCardTwo = new OysterCard("3f1b3b55-f266-4426-ba1b-bcc506541866");
        Customer customer2 = new Customer("Shelly Cooper", newCardTwo);
        CUSTOMERS.add(customer2);

        context.checking(new Expectations() {{
            exactly(1).of(database).getCustomers();
            will(returnValue(CUSTOMERS));
            exactly(1).of(adapter).charge(with(equal(customer)), with(aNonNull(List.class)), with(equal(costTotal)));
            exactly(1).of(adapter).charge(with(equal(customer2)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader, kingsCrossReader);

        clock.setTime(7, 0);
        paddingtonReader.touch(newCard);
        kingsCrossReader.touch(newCardTwo);

        clock.setTime(9, 5);
        bakerStreetReader.touch(newCard);
        paddingtonReader.touch(newCardTwo);

        tracker.chargeAccounts();
    }

}