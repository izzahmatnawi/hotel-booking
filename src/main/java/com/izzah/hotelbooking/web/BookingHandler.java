package com.izzah.hotelbooking.web;

import com.izzah.hotelbooking.model.Booking;
import com.izzah.hotelbooking.service.BookingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.izzah.hotelbooking.Application.DATE_STORAGE_FORMAT;


public class BookingHandler implements HttpHandler {

  private final BookingService bookingService = new BookingService();
  private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    String method = exchange.getRequestMethod();
    if ("GET".equals(method)) {
      handleGet(exchange);
    } else if ("POST".equals(method)) {
      handlePut(exchange);
    } else {
      exchange.sendResponseHeaders(405, -1);
    }
  }

  private void handleGet(final HttpExchange exchange) throws IOException {
    final String path = exchange.getRequestURI().getPath();
    String[] pathParts = path.split("/");
    if (pathParts.length != 4 || !Arrays.asList("date", "guest").contains(pathParts[2])) {
      exchange.sendResponseHeaders(404, -1);
    }

    StringBuilder response = null;

    if ("date".equals(pathParts[2])) {
      final String rawDate = pathParts[3];
      String parsedDate = "";
      if (rawDate == null || rawDate.isEmpty()) {
        exchange.sendResponseHeaders(400, -1);
      } else {
        Date dateTemp = null;
        try {
          dateTemp = sdf.parse(rawDate);
        } catch (ParseException e) {
          exchange.sendResponseHeaders(400, -1);
        }
        parsedDate = DATE_STORAGE_FORMAT.format(dateTemp);
      }
      response = getAvailableRoomsByDate(parsedDate);
    } else {
      response = getBookingsByGuestName(pathParts[3]);
    }

    exchange.sendResponseHeaders(200, response.length());
    OutputStream os = exchange.getResponseBody();
    os.write(response.toString().getBytes());
    os.close();
  }

  private StringBuilder getBookingsByGuestName(final String guest) {
    StringBuilder response = new StringBuilder();
    List<Booking> bookings = bookingService.getBookingsByGuestName(guest);

    response.append("[");
    for (Booking booking : bookings) {
      response.append("{");
      response.append("\"name\":\"").append(booking.getGuestName()).append("\",");
      response.append("\"roomNumber\":").append(booking.getRoomNumber()).append(",");
      response.append("\"date\":").append(booking.getBookingDate());
      response.append("},");
    }
    if (!bookings.isEmpty()) {
      response.deleteCharAt(response.length() - 1);
    }
    response.append("]");

    return response;

  }

  private StringBuilder getAvailableRoomsByDate(final String date) {
    StringBuilder response = new StringBuilder();
    Set<Integer> roomNos = bookingService.getAvailableRoomsByDate(date);

    response.append("[");
    for (Integer roomNo : roomNos) {
      response.append(roomNo);
      response.append(",");
    }
    if (!roomNos.isEmpty()) {
      response.deleteCharAt(response.length() - 1);
    }
    response.append("]");

    return response;
  }


  private void handlePut(final HttpExchange exchange) throws IOException {
    final String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
        .lines().collect(Collectors.joining("\n"));

    String[] parts = requestBody.split("&");
    if (parts.length != 3) {
      exchange.sendResponseHeaders(400, -1);
    }

    Map<String, String> requestKeyValue = Arrays.stream(parts).collect(Collectors.toMap(
        p -> p.split("=")[0],
        p -> p.split("=")[1]));
    final String guestName = requestKeyValue.get("guestName");
    final String roomNo = requestKeyValue.get("roomNo");
    final String date = requestKeyValue.get("date");

    if (guestName == null || guestName.isEmpty()) {
      exchange.sendResponseHeaders(400, -1);
    }

    int parsedRoomNo = -1;
    if (roomNo == null || roomNo.isEmpty()) {
      exchange.sendResponseHeaders(400, -1);
    } else {
      try {
        parsedRoomNo = Integer.parseInt(roomNo);
      } catch (NumberFormatException e) {
        exchange.sendResponseHeaders(400, -1);
      }
    }

    String parsedDate = "";
    if (date == null || date.isEmpty()) {
      exchange.sendResponseHeaders(400, -1);
    } else {
      Date dateTemp = null;
      try {
        dateTemp = sdf.parse(date);
      } catch (ParseException e) {
        exchange.sendResponseHeaders(400, -1);
      }
      parsedDate = DATE_STORAGE_FORMAT.format(dateTemp);
    }

    if(bookingService.isValidRoomNo(parsedRoomNo) && bookingService.isAvailableByDateAndRoomNo(parsedDate, parsedRoomNo)) {
      bookingService.setBooking(guestName, parsedDate, parsedRoomNo);
      exchange.sendResponseHeaders(200, -1);
    } else {
      final String response = "room not available";
      exchange.sendResponseHeaders(500, response.length());
      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }

}
