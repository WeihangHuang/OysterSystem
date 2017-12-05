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

    private final OysterCard newCardOne = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    private final OysterCard newCardTwo = new OysterCard("3f1b3b55-f266-4426-ba1b-bcc506541866");;
    private final Customer customerOne = new Customer("Fred Bloggs", newCardOne);
    private final Customer customerTwo = new Customer("Shelly Cooper", newCardTwo);

    private List<Customer> CUSTOMERS = new ArrayList<>();
    private List<Journey> journey;
    private BigDecimal costTotal;

    ControllableClock clock = new ControllableClock();



    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    PaymentAdapter paymentAdapter = context.mock(PaymentAdapter.class);
    DatabaseAdapter database = context.mock(DatabaseAdapter.class);

    TravelTracker tracker = new TravelTracker(database, paymentAdapter, clock);


    OysterCardReader paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
    OysterCardReader bakerStreetReader = OysterReaderLocator.atStation(Station.BAKER_STREET);
    OysterCardReader kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);



    @Test
    public void oneCustomerNoJourneysTest(){ //also test getting and sending data

        CUSTOMERS.add(customerOne);
        journey = new ArrayList<>();
        costTotal= new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers(); will(returnValue(CUSTOMERS));

            exactly(1).of(paymentAdapter).charge(customerOne, journey, costTotal);
        }});

        tracker.chargeAccounts();

    }

    @Test
    public void oneCustomerOneOffPeakJourneysTest(){
        CUSTOMERS.add(customerOne);
        costTotal = new BigDecimal(1.6).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentAdapter).charge(with(equal(customerOne)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(newCardOne);

        clock.setTime(0, 5);
        bakerStreetReader.touch(newCardOne);


        tracker.chargeAccounts();

    }

    @Test
    public void oneCustomerOnePeakJourneysTest() {
        CUSTOMERS.add(customerOne);
        costTotal = new BigDecimal(2.9).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations() {{
            exactly(1).of(database).getCustomers();
            will(returnValue(CUSTOMERS));
            exactly(1).of(paymentAdapter).charge(with(equal(customerOne)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(8, 45);
        paddingtonReader.touch(newCardOne);

        clock.setTime(9, 5);
        bakerStreetReader.touch(newCardOne);


        tracker.chargeAccounts();
    }

    @Test
    public void oneCustomerTwoPeakJourneysTest() {
        CUSTOMERS.add(customerOne);
        costTotal = new BigDecimal(5.8).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations() {{
            exactly(1).of(database).getCustomers();
            will(returnValue(CUSTOMERS));
            exactly(1).of(paymentAdapter).charge(with(equal(customerOne)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader, kingsCrossReader);

        clock.setTime(7, 0);
        paddingtonReader.touch(newCardOne);

        clock.setTime(7, 5);
        bakerStreetReader.touch(newCardOne);

        clock.setTime(18,50);
        bakerStreetReader.touch(newCardOne);

        clock.setTime(19,0);
        kingsCrossReader.touch(newCardOne);

        tracker.chargeAccounts();
    }

    @Test
    public void twoCustomerOnePeakJourneysTest() {
        CUSTOMERS.add(customerOne);
        costTotal = new BigDecimal(2.9).setScale(2, BigDecimal.ROUND_HALF_UP);

        CUSTOMERS.add(customerTwo);

        context.checking(new Expectations() {{
            exactly(1).of(database).getCustomers();
            will(returnValue(CUSTOMERS));
            exactly(1).of(paymentAdapter).charge(with(equal(customerOne)), with(aNonNull(List.class)), with(equal(costTotal)));
            exactly(1).of(paymentAdapter).charge(with(equal(customerTwo)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader, kingsCrossReader);

        clock.setTime(8, 55);
        paddingtonReader.touch(newCardOne);
        kingsCrossReader.touch(newCardTwo);

        clock.setTime(9, 5);
        bakerStreetReader.touch(newCardOne);
        paddingtonReader.touch(newCardTwo);

        tracker.chargeAccounts();
    }

    @Test
    public void oneCustomerOffPeakLongJourneysTest(){
        CUSTOMERS.add(customerOne);
        costTotal = new BigDecimal(2.7).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentAdapter).charge(with(equal(customerOne)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(newCardOne);

        clock.setTime(0, 30);
        bakerStreetReader.touch(newCardOne);


        tracker.chargeAccounts();

    }

    @Test
    public void oneOffPeakLongOnePeakLongAndOneOffPeakShortJourneysTest(){
        CUSTOMERS.add(customerOne);
        costTotal = new BigDecimal(8.1).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentAdapter).charge(with(equal(customerOne)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(newCardOne);
        clock.setTime(0, 30);  //Off peak long
        bakerStreetReader.touch(newCardOne);

        clock.setTime(2, 30);
        bakerStreetReader.touch(newCardOne);
        clock.setTime(2,40); //Off peak short
        paddingtonReader.touch(newCardOne);

        clock.setTime(6,0);
        paddingtonReader.touch(newCardOne);
        clock.setTime(7, 30);  //Peak long
        bakerStreetReader.touch(newCardOne);

        tracker.chargeAccounts();

    }

    @Test
    public void peakJourneysCapTest(){
        CUSTOMERS.add(customerOne);
        costTotal = new BigDecimal(9).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentAdapter).charge(with(equal(customerOne)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(newCardOne);
        clock.setTime(0, 30);
        bakerStreetReader.touch(newCardOne);

        clock.setTime(2, 30);
        bakerStreetReader.touch(newCardOne);
        clock.setTime(2,40);
        paddingtonReader.touch(newCardOne);

        clock.setTime(6,0);
        paddingtonReader.touch(newCardOne);
        clock.setTime(7, 30);
        bakerStreetReader.touch(newCardOne);

        clock.setTime(12, 30);
        bakerStreetReader.touch(newCardOne);
        clock.setTime(12,40);
        paddingtonReader.touch(newCardOne);

        clock.setTime(15,0);
        paddingtonReader.touch(newCardOne);
        clock.setTime(15, 30);
        bakerStreetReader.touch(newCardOne);


        tracker.chargeAccounts();

    }

    @Test
    public void offPeakJourneysCapTest(){
        CUSTOMERS.add(customerOne);
        costTotal = new BigDecimal(7).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentAdapter).charge(with(equal(customerOne)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(newCardOne);
        clock.setTime(0, 30);
        bakerStreetReader.touch(newCardOne);

        clock.setTime(2, 30);
        bakerStreetReader.touch(newCardOne);
        clock.setTime(2,40);
        paddingtonReader.touch(newCardOne);

        clock.setTime(9,1);
        paddingtonReader.touch(newCardOne);
        clock.setTime(9, 30);
        bakerStreetReader.touch(newCardOne);

        clock.setTime(12, 30);
        bakerStreetReader.touch(newCardOne);
        clock.setTime(12,40);
        paddingtonReader.touch(newCardOne);

        clock.setTime(15,0);
        paddingtonReader.touch(newCardOne);
        clock.setTime(15, 30);
        bakerStreetReader.touch(newCardOne);


        tracker.chargeAccounts();

    }
}