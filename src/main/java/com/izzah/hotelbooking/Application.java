package com.izzah.hotelbooking;

import com.izzah.hotelbooking.web.BookingHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Application {

  public static final Map<String, Map<String, Set<Integer>>> GUESTROOMS_BY_DATE = new ConcurrentHashMap<>();
  public static final SimpleDateFormat DATE_STORAGE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
  public static int maxNoOfRooom = 100;

  public static void main(String[] args) throws IOException {
    if (args != null && args.length ==1 && !args[0].isEmpty()) {
      maxNoOfRooom = Integer.parseInt(args[0]);
    }

    HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
    server.createContext("/bookings", new BookingHandler());
    server.setExecutor(null);
    server.start();
  }
}
