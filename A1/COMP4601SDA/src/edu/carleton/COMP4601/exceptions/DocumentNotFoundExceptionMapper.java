package edu.carleton.COMP4601.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DocumentNotFoundExceptionMapper implements ExceptionMapper<DocumentNotFoundException> {
	@Override
	public Response toResponse(DocumentNotFoundException docNotFound) {
		return Response.status(204).entity(docNotFound.getMessage()).build();
	}

}
