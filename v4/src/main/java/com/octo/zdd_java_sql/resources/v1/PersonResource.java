package com.octo.zdd_java_sql.resources.v1;

import com.octo.zdd_java_sql.api.v1.ErrorResult;
import com.octo.zdd_java_sql.api.v1.People;
import com.octo.zdd_java_sql.api.v1.Person;
import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import com.octo.zdd_java_sql.db.AddressDAO;
import com.octo.zdd_java_sql.db.PersonDAO;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.jersey.params.LongParam;

import javax.annotation.Nullable;
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
import java.util.List;
import java.util.stream.Collectors;

@Path("/v1/people")
@Produces(MediaType.APPLICATION_JSON)

public class PersonResource {

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
                        findAllWithJoin().
                        stream().
                        map(personEntity -> this.personFromEntity(personEntity, null)).
                        collect(Collectors.toList()));
        return people;
    }

    @GET
    @Path("{id : \\d+}")
    @UnitOfWork
    @NotNull
    public Response getPerson(@PathParam("id") LongParam personId) {
        // lock to ensure the address are not updated by the migration script
        PersonEntity personEntity = personDAO.findByIdWithJoin(personId.get());
        if (personEntity != null) {
            return Response.status(Response.Status.OK).entity(personFromEntity(personEntity, null)).build();
        } else {
            return createPersonNotFoundResponse();
        }
    }

    @DELETE
    @Path("{id : \\d+}")
    @UnitOfWork
    @NotNull
    public Response deletePerson(@PathParam("id") LongParam personId) {
        // lock to ensure the address are not updated / inserted
        PersonEntity personEntity = personDAO.findByIdWithLock(personId.get());
        if (personEntity != null) {
            addressDAO.deleteByPerson(personEntity);
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
    public Response updatePerson(
            @PathParam("id") LongParam personId,
            @NotNull Person person) {
        Response validationResponse = validatePerson(person);
        if (validationResponse != null) {
            return validationResponse;
        }

        PersonEntity personEntity = personDAO.findByIdWithLock(personId.get());
        if (personEntity != null) {
            personEntity.setName(person.getName());
            personEntity.setAddress(null);
            personEntity = personDAO.update(personEntity);

            addressDAO.deleteByPerson(personEntity);

            AddressEntity addressEntity = null;
            if (person.getAddress() != null) {
                addressEntity = addressDAO.create(person.getAddress(), personEntity);
            }

            return Response.status(Response.Status.OK).entity(personFromEntity(personEntity, addressEntity)).build();
        } else {
            return createPersonNotFoundResponse();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @UnitOfWork
    @NotNull
    public Response addPerson(@NotNull Person person) {
        Response validationResponse = validatePerson(person);
        if (validationResponse != null) {
            return validationResponse;
        }
        PersonEntity personEntity = personDAO.create(person.getName());
        AddressEntity addressEntity = null;
        if (person.getAddress() != null) {
            addressEntity = addressDAO.create(person.getAddress(), personEntity);
        }
        return Response.created(URI.create("/v1/people/" + personEntity.getId())).entity(personFromEntity(personEntity, addressEntity)).build();
    }


    /**
     * Create the serialized Person from a PersonEntity
     * Look for addresses, if not found look in the address column
     */
    private
    @NotNull
    Person personFromEntity(@NotNull PersonEntity personEntity, @Nullable AddressEntity addressEntity) {
        String address;
        if (addressEntity != null) {
            address = addressEntity.getAddress();
        } else {
            addressEntity = getFirstAddress(personEntity);
            if (addressEntity != null) {
                address = addressEntity.getAddress();
            } else {
                address = personEntity.getAddress();
            }
        }
        return new Person(personEntity.getId(), personEntity.getName(), address);
    }

    private
    @Nullable
    Response validatePerson(@NotNull Person person) {
        if (person.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new Error("Name is missing")).build();
        } else {
            return null;
        }
    }

    @NotNull
    private Response createPersonNotFoundResponse() {
        return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResult(Response.Status.NOT_FOUND, "Person not found")).build();
    }

    @Nullable
    private AddressEntity getFirstAddress(PersonEntity person) {
        List<AddressEntity> addressesEntities = person.getAddresses();
        return addressesEntities.isEmpty() ? null : addressesEntities.get(0);
    }


}
