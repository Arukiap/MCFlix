package com.mcflix.app;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;


/*
 * Used to reply to stream requests
 */
@XmlRootElement
class StreamResponse {

    public static int STREAM_FOUND = 1;
    public static int STREAM_NOT_FOUND = 2;
    public static int STREAM_ALREADY_EXISTS = 3;
    public static int STREAM_CREATED = 4;
    public static int STREAM_DELETED = 5;

    public int status;
    public String streamID;

    public StreamResponse(){}

    public StreamResponse(int status){
        this.status = status;
    }

    public StreamResponse(int status,String streamID){
        this.status = status;
        this.streamID = streamID;
    }
}

/*
 * Used to request a delition or creation of a stream
 */
@XmlRootElement
class StreamRequest {
    public String streamID;
    public StreamRequest(){}
}

/*
 * Used to reply a set with all available streams
 */
@XmlRootElement
class StreamsResponse {
    public Set<String> streams;

    public StreamsResponse(){
        streams = new HashSet<String>();
    }

    public StreamsResponse(Set<String> streams){
        this.streams = streams;
    }

}

/*
 * This class defines the Streaming content stored in the server context.
 */
@Path("/stream")
public class Stream {

    private static String liveServerURL = "rtmp://35.205.234.126/live/";

    private Set<String> streams;

    @Context ServletContext servletContext; //Stores the set of streams available

    @DefaultValue("") @QueryParam("id") String id; //Stores the queried id

    private String getStreamURL(String id){
        return liveServerURL + id;
    }

    //Retrieve server stream context. If it doesn't exit, initializes it.
    private void getStreamContext(){
        System.out.println("Getting stream context...");
        if(servletContext.getAttribute("streams") == null){
            streams = new HashSet<String>();
            setStreamContext();
        } else {
            streams = (HashSet<String>)servletContext.getAttribute("streams");
        }
    }

    private void setStreamContext(){
        servletContext.setAttribute("streams",streams);
    }

    //Retrieves all current available streams
    @GET
    @Path("/streams")
    @Produces(MediaType.APPLICATION_JSON)
    public StreamsResponse getStreams(){
        getStreamContext();
        System.out.println("Returning all available streams");
        System.out.println(streams.toString());
        return new StreamsResponse(streams);
    }

    //Retrieves data relevant to the queried stream id
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public StreamResponse getStream()
    {
        getStreamContext();
        if(streams.contains(id)){
            //String found, return its url
            System.out.println("Returning url of stream "+id);
            return new StreamResponse(StreamResponse.STREAM_FOUND,getStreamURL(id));
        } else {
            System.out.println("Error: Stream with id "+id+ "not found");
            return new StreamResponse(StreamResponse.STREAM_NOT_FOUND);
        }
    }

    //Creates a new stream given a certain id
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public StreamResponse createStream(StreamRequest request){
        getStreamContext();
        String streamID = request.streamID;
        System.out.println("Creating stream");
        if(streams.contains(streamID)){
            //Already a stream with the same id, return error
            System.out.println("Error: Tried to create stream with id "+streamID+" but it already exists.");
            return new StreamResponse(StreamResponse.STREAM_ALREADY_EXISTS);
        } else {
            String url = getStreamURL(streamID);
            System.out.println("Creating new stream with id "+streamID);
            System.out.println("Creating new stream with url "+url);
            streams.add(streamID);
            setStreamContext();
            return new StreamResponse(StreamResponse.STREAM_CREATED,url);
        }
    }

    //Deletes a certain stream given its id
    @POST
    @Path("/delete") //Android volley has no body for delete request, so we need to do a post on a delete path
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public StreamResponse deleteStream(StreamRequest request){
        getStreamContext();
        String streamID = request.streamID;
        if(streams.contains(streamID)){
            //If stream exists, delete it
            System.out.println("Deleting stream with id "+streamID);
            streams.remove(streamID);
            setStreamContext();
            return new StreamResponse(StreamResponse.STREAM_DELETED);
        } else {
            System.out.println("Error: Tried to delete a non existent stream with id "+streamID);
            return new StreamResponse(StreamResponse.STREAM_NOT_FOUND);
        }
    }
}
