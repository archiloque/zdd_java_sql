package com.octo.zdd_java_sql.resources.v2;

import com.octo.zdd_java_sql.api.v2.People;
import com.octo.zdd_java_sql.api.v2.Person;
import com.octo.zdd_java_sql.core.PersonEntity;
import com.octo.zdd_java_sql.db.AddressDAO;
import com.octo.zdd_java_sql.db.PersonDAO;
import com.octo.zdd_java_sql.resources.ResourceHelper;
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

@Path("/v2/people")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource extends BaseResource {

    @NotNull
    private final PersonDAO personDAO;

    @NotNull
    private final AddressDAO addressDAO;

    public PersonResource(@NotNull PersonDAO personDAO, @NotNull AddressDAO addressDAO) {
        this.personDAO = personDAO;
        this.addressDAO = addressDAO;
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
        Optional<PersonEntity> optionalPersonEntity = personDAO.findById(personId.get());
        if (optionalPersonEntity.isPresent()) {
            return Response.status(Response.Status.OK).entity(personFromEntity(optionalPersonEntity.get())).build();
        } else {
            return createPersonNotFoundResponse();
        }
    }

    @DELETE
    @Path("{id : \\d+}")
    @UnitOfWork
    @NotNull
    public Response deletePerson(@PathParam("id") LongParam personId) {
        Optional<Response> response = ResourceHelper.deletePerson(personId, personDAO, addressDAO);
        if (response.isPresent()) {
            return response.get();
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

        Optional<PersonEntity> optionalPersonEntity = personDAO.findById(personId.get());
        if (optionalPersonEntity.isPresent()) {
            PersonEntity personEntity = optionalPersonEntity.get();
            personEntity.setName(person.getName());
            personDAO.update(personEntity);
            return Response.status(Response.Status.OK).entity(personFromEntity(personEntity)).build();
        } else {
            return createPersonNotFoundResponse();
        }
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
        PersonEntity personEntity = personDAO.create(person.getName());
        return Response.created(URI.create("/v2/people/" + personEntity.getId())).entity(personFromEntity(personEntity)).build();
    }

    /**
     * Create the serialized Person from a PersonEntity
     */
    private
    @NotNull
    Person personFromEntity(@NotNull PersonEntity personEntity) {
        return new Person(personEntity.getId(), personEntity.getName());
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
