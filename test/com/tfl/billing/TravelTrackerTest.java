package com.tfl.billing;

import com.oyster.OysterCard;
import com.oyster.OysterCardReader;
import com.tfl.external.Customer;
import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 12/4/17.
 */
public class TravelTrackerTest {


    private PaymentsSystemInterface paymentsSystemInterface;
    private CustomerDatabaseInterface database;
    private TravelTracker tracker;
    private List<Customer> CUSTOMERS;

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

    private final OysterCard CARD_OF_FRED_BLOGGS = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    private final Customer FRED_BLOGGS = new Customer("Fred Bloggs", CARD_OF_FRED_BLOGGS);

    private final OysterCard CARD_OF_SHELLY_COOPER = new OysterCard("3f1b3b55-f266-4426-ba1b-bcc506541866");
    private final Customer SHELLY_COOPER = new Customer("Shelly Cooper", CARD_OF_SHELLY_COOPER);

    private BigDecimal costTotal;

    private ControllableClock clock;

    private OysterCardReader paddingtonReader;
    private OysterCardReader bakerStreetReader;
    private OysterCardReader kingsCrossReader;



    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setup(){
        paymentsSystemInterface = context.mock(PaymentsSystemInterface.class);
        database = context.mock(CustomerDatabaseInterface.class);
        clock = new ControllableClock();

        paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
        bakerStreetReader = OysterReaderLocator.atStation(Station.BAKER_STREET);
        kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);

        tracker = new TravelTracker(database, paymentsSystemInterface, clock);
        CUSTOMERS = new ArrayList<>();
    }

    @After
    public void teardown(){
        paymentsSystemInterface = null;
        database = null;
        clock = null;

        tracker = null;
        CUSTOMERS = null;

        paddingtonReader = null;
        bakerStreetReader = null;
        kingsCrossReader = null;
    }


    @Test
    public void oneCustomerNoJourneysTest(){
        CUSTOMERS.add(FRED_BLOGGS);
        List<Journey> journeys = new ArrayList<>();
        costTotal= new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers(); will(returnValue(CUSTOMERS));
            exactly(1).of(paymentsSystemInterface).charge(FRED_BLOGGS, journeys, costTotal);
        }});

        tracker.chargeAccounts();

    }

    @Test
    public void oneCustomerOneOffPeakJourneysTest(){
        CUSTOMERS.add(FRED_BLOGGS);
        costTotal = new BigDecimal(1.6).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id()); will(returnValue(true));
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(FRED_BLOGGS)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(0, 5);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);


        tracker.chargeAccounts();

    }

    @Test
    public void oneCustomerOnePeakJourneysTest() {
        CUSTOMERS.add(FRED_BLOGGS);
        costTotal = new BigDecimal(2.9).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations() {{
            allowing(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id()); will(returnValue(true));
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(FRED_BLOGGS)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(8, 45);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(9, 5);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);


        tracker.chargeAccounts();
    }

    @Test
    public void oneCustomerTwoPeakJourneysTest() {
        CUSTOMERS.add(FRED_BLOGGS);
        costTotal = new BigDecimal(5.8).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations() {{
//            exactly(1).of(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id());will(returnValue(true));
            allowing(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id()); will(returnValue(true));
            exactly(1).of(database).getCustomers();
            will(returnValue(CUSTOMERS));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(FRED_BLOGGS)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader, kingsCrossReader);

        clock.setTime(7, 0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(7, 5);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(18,50);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(19,0);
        kingsCrossReader.touch(CARD_OF_FRED_BLOGGS);

        tracker.chargeAccounts();
    }

    @Test
    public void twoCustomerOnePeakJourneysTest() {

        CUSTOMERS.add(FRED_BLOGGS);
        CUSTOMERS.add(SHELLY_COOPER);
        costTotal = new BigDecimal(2.9).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations() {{
            allowing(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id());will(returnValue(true));
            allowing(database).isRegisteredId(CARD_OF_SHELLY_COOPER.id());will(returnValue(true));

            exactly(1).of(database).getCustomers();
            will(returnValue(CUSTOMERS));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(FRED_BLOGGS)), with(aNonNull(List.class)), with(equal(costTotal)));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(SHELLY_COOPER)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader, kingsCrossReader);

        clock.setTime(8, 55);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        kingsCrossReader.touch(CARD_OF_SHELLY_COOPER);

        clock.setTime(9, 5);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);
        paddingtonReader.touch(CARD_OF_SHELLY_COOPER);

        tracker.chargeAccounts();
    }

    @Test
    public void oneCustomerOffPeakLongJourneysTest(){
        CUSTOMERS.add(FRED_BLOGGS);
        costTotal = new BigDecimal(2.7).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id());will(returnValue(true));
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(FRED_BLOGGS)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(0, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);


        tracker.chargeAccounts();

    }

    @Test
    public void oneOffPeakLongOnePeakLongAndOneOffPeakShortJourneysTest(){
        CUSTOMERS.add(FRED_BLOGGS);
        costTotal = new BigDecimal(8.1).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id());will(returnValue(true));
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(FRED_BLOGGS)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(0, 30);  //Off peak long
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(2, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(2,40); //Off peak short
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(6,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(7, 30);  //Peak long
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);

        tracker.chargeAccounts();

    }

    @Test
    public void peakJourneysCapTest(){
        CUSTOMERS.add(FRED_BLOGGS);
        costTotal = new BigDecimal(9).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            allowing(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id());will(returnValue(true));

            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(FRED_BLOGGS)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(0, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(2, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(2,40);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(6,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(7, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(12, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(12,40);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(15,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(15, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);


        tracker.chargeAccounts();

    }

    @Test
    public void offPeakJourneysCapTest(){
        CUSTOMERS.add(FRED_BLOGGS);
        costTotal = new BigDecimal(7).setScale(2, BigDecimal.ROUND_HALF_UP);

        context.checking(new Expectations(){{
            exactly(1).of(database).getCustomers();will(returnValue(CUSTOMERS));
            allowing(database).isRegisteredId(CARD_OF_FRED_BLOGGS.id());will(returnValue(true));
            exactly(1).of(paymentsSystemInterface).charge(with(equal(FRED_BLOGGS)), with(aNonNull(List.class)), with(equal(costTotal)));
        }});


        tracker.connect(paddingtonReader, bakerStreetReader);

        clock.setTime(0,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(0, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(2, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(2,40);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(9,1);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(9, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(12, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(12,40);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);

        clock.setTime(15,0);
        paddingtonReader.touch(CARD_OF_FRED_BLOGGS);
        clock.setTime(15, 30);
        bakerStreetReader.touch(CARD_OF_FRED_BLOGGS);


        tracker.chargeAccounts();

    }


    @Test
    public void canThrownUnknownOysterCardException(){
        OysterCard unregisteredCard = new OysterCard("ffffffff-ffff-ffff-ffff-ffffffffffff");

        context.checking(new Expectations(){{
            exactly(1).of(database).isRegisteredId(unregisteredCard.id());
            will(returnValue(false));
        }});

        exception.expect(UnknownOysterCardException.class);
        tracker.cardScanned(unregisteredCard.id(), paddingtonReader.id());
//        paddingtonReader.touch(unregisteredCard);
//        tracker.chargeAccounts();
    }

}