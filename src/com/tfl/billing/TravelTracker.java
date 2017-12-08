package com.tfl.billing;

import com.oyster.*;
import com.tfl.external.Customer;

import java.math.BigDecimal;
import java.util.*;

public class TravelTracker implements ScanListener {

    static final BigDecimal OFF_PEAK_SHORT_JOURNEY_PRICE = new BigDecimal(1.60);
    static final BigDecimal OFF_PEAK_LONG_JOURNEY_PRICE = new BigDecimal(2.70);
    static final BigDecimal PEAK_SHORT_JOURNEY_PRICE = new BigDecimal(2.90);
    static final BigDecimal PEAK_LONG_JOURNEY_PRICE = new BigDecimal(3.80);

    private final List<JourneyEvent> eventLog = new ArrayList<JourneyEvent>();
    private final Set<UUID> currentlyTravelling = new HashSet<UUID>();

    private CustomerDatabaseInterface customerDatabase;
    private PaymentsSystemInterface payments_instance;
    private Clock clock;

    public TravelTracker(){
        this.customerDatabase = CustomerCustomerDatabaseInterface.getInstance();
        this.payments_instance = PaymentsSystemAdapter.getInstance();
        this.clock = new SystemClock();
    }


    public TravelTracker(CustomerDatabaseInterface database, PaymentsSystemInterface paymentsSystemInterface, Clock clock) {
        this.customerDatabase = database;
        this.payments_instance = paymentsSystemInterface;
        this.clock = clock;
    }


    public void chargeAccounts() {

        List<Customer> customers = customerDatabase.getCustomers();
        for (Customer customer : customers) {
            totalJourneysFor(customer);
        }
    }

    private void totalJourneysFor(Customer customer) {

        List<JourneyEvent> customerJourneyEvents = new ArrayList<JourneyEvent>();
        for (JourneyEvent journeyEvent : eventLog) {
            if (journeyEvent.cardId().equals(customer.cardId())) {
                customerJourneyEvents.add(journeyEvent);
            }
        }

        List<Journey> journeys = new ArrayList<Journey>();

        JourneyEvent start = null;
        for (JourneyEvent event : customerJourneyEvents) {
            if (event instanceof JourneyStart) {
                start = event;
            }
            if (event instanceof JourneyEnd && start != null) {
                journeys.add(new Journey(start, event));
                start = null;
            }
        }

        BigDecimal customerTotal = calculateTotalPrice(journeys);

        payments_instance.charge(customer, journeys, roundToNearestPenny(customerTotal));
    }

     private BigDecimal calculateTotalPrice(List<Journey> journeys){
        BigDecimal customerTotal = new BigDecimal(0);
        boolean peak_flag = false;
        for (Journey journey : journeys) {
            int minutePass = (journey.durationSeconds() / 60);
            boolean longJourney = false;
            if (minutePass >= 25) {longJourney = true;}

            BigDecimal journeyPrice = OFF_PEAK_SHORT_JOURNEY_PRICE;

            if (peak(journey) && longJourney) {journeyPrice = PEAK_LONG_JOURNEY_PRICE; peak_flag = true;}
            if (peak(journey) && !longJourney) {journeyPrice = PEAK_SHORT_JOURNEY_PRICE; peak_flag = true;}
            if (!peak(journey) && longJourney) {journeyPrice = OFF_PEAK_LONG_JOURNEY_PRICE;}
            customerTotal = customerTotal.add(journeyPrice);
        }

        if (peak_flag && (customerTotal.doubleValue() > 9)) {customerTotal = new BigDecimal(9.00);}
        if (!peak_flag && (customerTotal.doubleValue() > 7)) {customerTotal = new BigDecimal(7.00);}


        return customerTotal;

    }

    private BigDecimal roundToNearestPenny(BigDecimal poundsAndPence) {
        return poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private boolean peak(Journey journey) {
        return peak(journey.startTime()) || peak(journey.endTime());
    }

    private boolean peak(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 19);
    }

    public void connect(OysterCardReader... cardReaders) {
        for (OysterCardReader cardReader : cardReaders) {
            cardReader.register(this);
        }
    }

    @Override
    public void cardScanned(UUID cardId, UUID readerId) {
        if (currentlyTravelling.contains(cardId)) {
            eventLog.add(new JourneyEnd(cardId, readerId, clock));
            currentlyTravelling.remove(cardId);
        } else {
            if (customerDatabase.isRegisteredId(cardId)) {
                currentlyTravelling.add(cardId);
                eventLog.add(new JourneyStart(cardId, readerId, clock));
            } else {
                throw new UnknownOysterCardException(cardId);
            }
        }
    }

}
