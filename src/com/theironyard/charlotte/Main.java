package com.theironyard.charlotte;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS restaurants (id IDENTITY, name VARCHAR, city VARCHAR, type VARCHAR, rating INT)"); // create restaurants database

        Spark.get(
                "/",
                ((request, response) -> {

                    HashMap m = new HashMap<>();
                    m.put("restaurantsAll", Restaurant.selectRestaurant(conn)); // run method to select * from restaurants, put in model hashmap

                    return new ModelAndView(m, "home.html");

                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/create-restaurant",
                ((request, response) -> {
                    String restaurantName = request.queryParams("restaurantName");
                    String restaurantCity = request.queryParams("restaurantCity");
                    String restaurantType = request.queryParams("restaurantType");
                    int restaurantRating = Integer.valueOf(request.queryParams("restaurantRating"));

                    Restaurant.insertRestaurant(conn, restaurantName, restaurantCity, restaurantType, restaurantRating);

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/edit-restaurant/:restaurantId",
                ((request, response) -> {
                    int id = Integer.valueOf(request.params("restaurantId"));
                    String restaurantName = request.queryParams("restaurantName");
                    String restaurantCity = request.queryParams("restaurantCity");
                    String restaurantType = request.queryParams("restaurantType");
                    int restaurantRating = Integer.valueOf(request.queryParams("restaurantRating"));

                    Restaurant.updateRestaurant(conn, restaurantName, restaurantCity, restaurantType, restaurantRating, id);

                    response.redirect("/");
                    return "";

                })
        );

        Spark.get("/edit/:restaurantId",
                ((request, response) -> {

                    int restaurantId = Integer.valueOf(request.params("restaurantId")); // get restaurant id

                    String name = Restaurant.specificRestaurant(conn, restaurantId).name; // use my specificRestaurant method to find the specific restaurant we're looking at
                    String city = Restaurant.specificRestaurant(conn, restaurantId).city;
                    String type = Restaurant.specificRestaurant(conn, restaurantId).type;
                    int rating = Restaurant.specificRestaurant(conn, restaurantId).rating;

                    HashMap p = new HashMap<>(); // create model hashmap
                    p.put("restaurantId", restaurantId);
                    p.put("restaurantName", name);
                    p.put("restaurantCity", city);
                    p.put("restaurantType", type);
                    p.put("restaurantRating", rating); // could just put in object, but not too sure of how to pull out individual fields afterwards?

                    return new ModelAndView(p, "edit.html");

                }),
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/delete/:restaurantId",
                ((request, response) -> {
                    int restaurantId = Integer.valueOf(request.params("restaurantId"));

                    Restaurant.deleteRestaurant(conn, restaurantId);

                    response.redirect("/");
                    return "";
                })

        );

        Spark.get(
                "/delete/:restaurantId",
                ((request, response) -> {

                    int restaurantId = Integer.valueOf(request.params("restaurantId"));

                    HashMap p = new HashMap<>(); // create model hashmap
                    p.put("restaurantId", restaurantId); // in order to retrieve in the post you need to put it in the model in the get
                    p.put("restaurantName", Restaurant.specificRestaurant(conn, restaurantId).name);
                    p.put("restaurantName", Restaurant.specificRestaurant(conn, restaurantId).city);
                    p.put("restaurantName", Restaurant.specificRestaurant(conn, restaurantId).type);
                    p.put("restaurantName", Restaurant.specificRestaurant(conn, restaurantId).rating);

                    return new ModelAndView(p, "delete.html");

                }),
                new MustacheTemplateEngine()
        );


        Spark.post(
                "/No-delete/:restaurantId",
                ((request, response) -> {

                    response.redirect("/");
                    return "";
                })
        );

// maybe all i need is a get method for the search...i just can't seem to figure out how to route after I have appended the search param into the URL in home.html
        Spark.get(
                "/search",
                ((request, response) -> {

                    String restaurantSearch = request.params("restaurantSearch").toLowerCase();

                    HashMap m = new HashMap<>();
                    m.put("search", Restaurant.searchRestaurant(conn, restaurantSearch));

                    return new ModelAndView(m, "search.html");

                }),
                new MustacheTemplateEngine()
        );

    }
}
