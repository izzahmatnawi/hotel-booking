package com.izzah.hotelbooking.service;

import com.izzah.hotelbooking.model.Booking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static com.izzah.hotelbooking.Application.GUESTROOMS_BY_DATE;
import static com.izzah.hotelbooking.Application.maxNoOfRooom;

public class BookingService {

  public List<Booking> getBookingsByGuestName(final String guestName) {
    Map<String, Set<Integer>> roomNosByDates = GUESTROOMS_BY_DATE.get(guestName);
    List<Booking> bookings = new ArrayList<>();

    if (roomNosByDates == null) {
      return bookings;
    }

    for (Map.Entry<String, Set<Integer>> roomNos : roomNosByDates.entrySet()) {
      Set<Integer> roomList = roomNos.getValue();
      String date = roomNos.getKey();
      bookings.addAll(roomList.stream().map(r -> new Booking(guestName, date, r)).collect(Collectors.toList()));
    }
    return bookings;
  }

  public Set<Integer> getAvailableRoomsByDate(final String date) {
    Set<Integer> bookedRooms = getBookedRoomsByDate(date);
    Set<Integer> availableRooms = new HashSet<>();

    for (int i = 1; i <= maxNoOfRooom; i++) {
      if (!bookedRooms.contains(i)) {
        availableRooms.add(i);
      }
    }

    return availableRooms;
  }

  public boolean isValidRoomNo(final Integer roomNo) {
    return !(roomNo < 1 || roomNo > maxNoOfRooom);
  }

  public boolean isAvailableByDateAndRoomNo(final String date, final Integer roomNo) {
    Set<Integer> bookedRooms = getBookedRoomsByDate(date);
    return !bookedRooms.contains(roomNo);
  }

  private Set<Integer> getBookedRoomsByDate(String date) {
    Set<Integer> bookedRooms = new HashSet<>();
    for (Map.Entry<String, Map<String, Set<Integer>>> guestEntry : GUESTROOMS_BY_DATE.entrySet()) {
      Map<String, Set<Integer>> datesMap = guestEntry.getValue();
      Set<Integer> rooms = datesMap.get(date);
      if (rooms == null) {
        continue;
      } else {
        bookedRooms.addAll(rooms);
      }
    }
    return bookedRooms;
  }

  public void setBooking(final String guestName, final String date, final Integer roomNo) {
    Map<String, Set<Integer>> roomsByDate = GUESTROOMS_BY_DATE.computeIfAbsent(guestName, s -> new ConcurrentHashMap<>());
    Set<Integer> rooms = roomsByDate.computeIfAbsent(date, s -> new ConcurrentSkipListSet<>());
    rooms.add(roomNo);
  }

}
