package com.izzah.hotelbooking.model;

public class Booking {

  private String bookingDate;
  private String guestName;
  private Integer roomNumber;

  public Booking(String guestName, String bookingDate, int roomNumber) {
    this.guestName = guestName;
    this.bookingDate = bookingDate;
    this.roomNumber = roomNumber;
  }

  public Integer getRoomNumber() {
    return roomNumber;
  }

  public void setRoomNumber(int roomNumber) {
    this.roomNumber = roomNumber;
  }

  public String getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(String bookingDate) {
    this.bookingDate = bookingDate;
  }

  public String getGuestName() {
    return guestName;
  }

  public void setGuestName(String guestName) {
    this.guestName = guestName;
  }
}
