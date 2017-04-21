package com.octo.zdd_java_sql.resources.v1;

import com.octo.zdd_java_sql.api.v1.ErrorResult;
import com.octo.zdd_java_sql.api.v1.People;
import com.octo.zdd_java_sql.api.v1.Person;
import com.octo.zdd_java_sql.core.PersonEntity;
import com.octo.zdd_java_sql.db.PersonDAO;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/v1/people")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    @NotNull
    private final PersonDAO personDAO;

    public PersonResource(@NotNull PersonDAO personDAO) {
        this.personDAO = personDAO;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @UnitOfWork
    @NotNull
    public People list() {
        People people = new People();
        people.setPeople(
                personDAO.
                        findAll().
                        stream().
                        map(this::personFromEntity).
                        collect(Collectors.toList()));
        return people;
    }

    @GET
    @Path("{id : \\d+}")
    @UnitOfWork
    @NotNull
    public Response getPerson(@PathParam("id") LongParam personId) {
        PersonEntity personEntity = personDAO.findById(personId.get());
        if (personEntity != null) {
            return Response.status(Response.Status.OK).entity(personFromEntity(personEntity)).build();
        } else {
            return createPersonNotFoundResponse();
        }
    }

    @DELETE
    @Path("{id : \\d+}")
    @UnitOfWork
    @NotNull
    public Response deletePerson(@PathParam("id") LongParam personId) {
        PersonEntity personEntity = personDAO.findById(personId.get());
        if (personEntity != null) {
            personDAO.delete(personEntity);
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return createPersonNotFoundResponse();
        }
    }


    @PUT
    @Path("{id : \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @UnitOfWork
    @NotNull
    public Response updatePerson(@PathParam("id") LongParam personId, @NotNull Person person) {
        Optional<Response> validationResponse = validatePerson(person);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }

        PersonEntity personEntity = personDAO.findById(personId.get());
        if (personEntity != null) {
            personEntity.setName(person.getName());
            personEntity.setAddress(person.getAddress());
            personDAO.update(personEntity);
            return Response.status(Response.Status.OK).entity(personEntity).build();
        } else {
            return createPersonNotFoundResponse();
        }
    }

    @NotNull
    private Response createPersonNotFoundResponse() {
        return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResult(Response.Status.NOT_FOUND, "Person not found")).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @UnitOfWork
    @NotNull
    public Response addPerson(@NotNull Person person) {
        Optional<Response> validationResponse = validatePerson(person);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
        PersonEntity personEntity = personDAO.create(person.getName(), person.getAddress());
        return Response.created(URI.create("/v1/people/" + personEntity.getId())).entity(personFromEntity(personEntity)).build();
    }


    /**
     * Create the serialized Person from a PersonEntity
     */
    private
    @NotNull
    Person personFromEntity(@NotNull PersonEntity personEntity) {
        return new Person(personEntity.getId(), personEntity.getName(), personEntity.getAddress());
    }

    private
    @NotNull
    Optional<Response> validatePerson(@NotNull Person person) {
        if (person.getName() == null) {
            return Optional.of(Response.status(Response.Status.BAD_REQUEST).entity(new Error("Name is missing")).build());
        } else {
            return Optional.empty();
        }
    }

}
