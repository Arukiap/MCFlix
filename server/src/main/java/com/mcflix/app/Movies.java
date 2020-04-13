package com.mcflix.app;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Defines a set of categories that a Movie/Video can have.
 */
@XmlRootElement
class Categories {
    public Set<String> categories;

    public Categories(){
        categories = new HashSet<String>();
    }

    public Categories(Set<String> categories){
        this.categories = categories;
    }

    public void addCategory(String category){
        this.categories.add(category);
    }

}

/*
 * Defines a Movie/Video with all the information that the cassandra DB stores.
 */
@XmlRootElement
class Movie {
    public int movie_id;
    public Set<String> categories;
    public int length_seconds;
    public String name;
    public Map<String,String> urls;
    public int year;

    public Movie(){}

    public Movie(int movie_id, Set<String> categories, int length_seconds, String name, Map<String,String> urls, int year){
        this.movie_id = movie_id;
        this.categories = categories;
        this.length_seconds = length_seconds;
        this.name = name;
        this.urls = urls;
        this.year = year;
    }
}

/*
 * Defines a Movie/Video set to be returned to the client when it queries for the available content.
 */
@XmlRootElement
class MoviesSet {
    public Set<Movie> movies;

    public MoviesSet(){
        movies = new HashSet<Movie>();
    }

    public void addMovie(Movie movie){
        this.movies.add(movie);
    }
}

/*
 * This class defines the VoD content stored in the cassandra DB and that is sent back to the client.
 */
@Path("/movies")
public class Movies {

    @DefaultValue("") @QueryParam("category") String category;


    //Queries the DB for the different content categories available and then returns them in a JSON file
    @GET
    @Path("categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Categories getCategories() {
        CqlSession session = null;
        String getCategoriesStatement = "SELECT category from mcflix.movies";
        Categories categories = new Categories();
        System.out.println("Getting categories...");
        try {
            //Connect to Cassandra - is this done for each request or should we have an open connection?
            session = CqlSession.builder().build();

            //Execute the query/statement and loop through it to get the current unique categories
            ResultSet rs = session.execute(getCategoriesStatement);


            //For each row of the query we add the resulting values we get from the categories Set to a new set to
            //avoid duplicates and store all the categories in one set.
            for(Row row: rs){
                Set<String> rowCategories = row.getSet("category",String.class);
                for(String category: rowCategories){
                    categories.addCategory(category);
                }
            }
        } catch(Exception ex){
            ex.printStackTrace(System.out);
        } finally {
            session.close();
        }
        return categories;
    }

    //Returns all the available content within a certain category.
    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public MoviesSet getMovies() {
        CqlSession session = null;
        String getMoviesStatement = "SELECT * FROM mcflix.movies WHERE category CONTAINS " +
                                    category +
                                    " ALLOW FILTERING";
        MoviesSet movies = new MoviesSet();
        System.out.println("Getting movies with category " + category );
        System.out.println("Query String: "+getMoviesStatement);
        try {
            //Connect to Cassandra
            session = CqlSession.builder().build();

            //Execute the query/statement and loop through it to get the movies
            ResultSet rs = session.execute(getMoviesStatement);

            for (Row row:rs){
                Movie movie = new Movie(
                  row.getInt("movie_id"),
                  row.getSet("category",String.class),
                  row.getInt("length_seconds"),
                  row.getString("name"),
                  row.getMap("url",String.class,String.class),
                  row.getInt("year")
                );
                movies.addMovie(movie);
            }


        } catch(Exception ex){
            ex.printStackTrace(System.out);
        } finally {
            session.close();
        }
        return movies;
    }
}
