package keyless.rest.client;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/rest/client")
public class ClientService {

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public Client getTrackInJSON() {
 
		Client client = new Client();
		client.setName("User1");
		client.setUuid("550e8400-e29b-11d4-a716-446655440000");
		client.setSecret("secret");
		client.setOtp("password");
 
		return client;
 
	}
 
	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createClientInJSON(Client client) {
 
		String result = "Client saved : " + client;
		return Response.status(201).entity(result).build();
 
	}

}
