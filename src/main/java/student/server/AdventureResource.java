package student.server;

import org.glassfish.grizzly.http.server.HttpServer;
import student.server.AdventureServer;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class AdventureResource {
    /**
     * The single static adventure service instance used for this API.
     */
    private static AdventureService service = new MyAdventureService();

    public void createServer() {

    }

    /**
     * The API endpoint to test connectivity.
     * @return the string "pong" if connection was successful
     */
    @GET
    @Path("ping")
    public String ping() {
        return "pong";
    }

    /**
     * The API endpoint to clear all instances of the adventure game.
     * @return a success response
     */
    @POST
    @Path("reset")
    public Response reset() {
        service.reset();
        return Response.ok().build();
    }

    /**
     * The API endpoint to create a new instance of the adventure game.
     * @return the state of the newly created game
     * @throws AdventureException if a game could not be created
     */
    @POST
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    public Response create() throws AdventureException {
        int id = service.newGame();
        return getGame(id);
    }

    /**
     * The API endpoint to query the state of a game instance.
     * @param id the ID of the game instance
     * @return a valid game state if found; an error response if not found
     */
    @GET
    @Path("instance/{id: \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGame(@PathParam("id") int id) {
        GameStatus status = service.getGame(id);
        if (status == null) {
            return instanceNotFound(id);
        }
        return Response.ok(status).build();
    }

    /**
     * The API endpoint to delete an instance of a game.
     * @param id the ID of the game instance to destroy
     * @return whether the operation was a success
     */
    @DELETE
    @Path("instance/{id: \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response destroyGame(@PathParam("id") int id) {
        if (!service.destroyGame(id)) {
            return instanceNotFound(id);
        }

        return Response.ok().build();
    }

    /**
     * The API endpoint to handle a command issued to the game engine.
     * @param id the ID of the game instance currently being played
     * @param command the command issued by the client
     * @return the result of the issued command
     */
    @POST
    @Path("instance/{id: \\d+}/command")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleCommand(@PathParam("id") int id, Command command) {
        service.executeCommand(id, command);

        return getGame(id);
    }

    /**
     * The API endpoint to return an ordered mapping of players to "high" scores.
     * @return a response with a sorted map of "high" scores
     */
    @GET
    @Path("leaderboard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLeaderboard() {
        return Response.ok(service.fetchLeaderboard()).build();
    }

    /**
     * Helper method to build an `instanceNotFound` error.
     * @param id the instance ID
     */
    private Response instanceNotFound(int id) {
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new Error("No game found with id '" + id + "'."))
                .build();
    }

}
