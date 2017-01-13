package com.octo.zdd_java_sql.resources.v2;

import com.octo.zdd_java_sql.api.v2.Address;
import com.octo.zdd_java_sql.api.v2.Addresses;
import com.octo.zdd_java_sql.api.v2.ErrorResult;
import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import com.octo.zdd_java_sql.db.AddressDAO;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/v2/people")
@Produces(MediaType.APPLICATION_JSON)
public class AddressResource extends BaseResource {

    @NotNull
    private final PersonDAO personDAO;

    @NotNull
    private final AddressDAO addressDAO;

    public AddressResource(@NotNull PersonDAO personDAO, @NotNull AddressDAO addressDAO) {
        this.personDAO = personDAO;
        this.addressDAO = addressDAO;
    }

    @GET
    @Path("{personId : \\d+}/addresses")
    @Produces(MediaType.APPLICATION_JSON)
    @UnitOfWork
    @NotNull
    public Response list(@PathParam("personId") LongParam personId) {
        Optional<PersonEntity> optionalPersonEntity = personDAO.findByIdWithLock(personId.get());
        if (optionalPersonEntity.isPresent()) {
            PersonEntity personEntity = optionalPersonEntity.get();

            List<Address> addressesList;
            if (personEntity.getAddress() != null) {
                addressDAO.create(personEntity.getAddress(), personEntity);
                personEntity.setAddress(null);
                personEntity = personDAO.update(personEntity);
            }

            addressesList = addressDAO.
                    findByPerson(personEntity).
                    stream().
                    map(this::addressFromEntity).
                    collect(Collectors.toList());

            Addresses addresses = new Addresses();
            addresses.setAddresses(addressesList);
            return Response.status(Response.Status.OK).entity(addresses).build();
        } else {
            return createPersonNotFoundResponse();
        }
    }

    @GET
    @Path("{personId : \\d+}/addresses/{addressId : \\d+}")
    @UnitOfWork
    @NotNull
    public Response getAddress(@PathParam("personId") LongParam personId, @PathParam("addressId") LongParam addressId) {
        Optional<AddressEntity> optionalAddressEntity = addressDAO.findByIdAndPersonId(addressId.get(), personId.get());
        if (optionalAddressEntity.isPresent()) {
            return Response.status(Response.Status.OK).entity(addressFromEntity(optionalAddressEntity.get())).build();
        } else {
            return createAddressNotFoundResponse();
        }
    }

    @DELETE
    @Path("{personId : \\d+}/addresses/{addressId : \\d+}")
    @UnitOfWork
    @NotNull
    public Response deleteAddress(@PathParam("personId") LongParam personId, @PathParam("addressId") LongParam addressId) {
        Optional<AddressEntity> optionalAddressEntity = addressDAO.findByIdAndPersonId(addressId.get(), personId.get());
        if (optionalAddressEntity.isPresent()) {
            addressDAO.delete(optionalAddressEntity.get());
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return createAddressNotFoundResponse();
        }
    }


    @PUT
    @Path("{personId : \\d+}/addresses/{addressId : \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    @UnitOfWork
    @NotNull
    public Response updateAddress(
            @PathParam("personId") LongParam personId,
            @PathParam("addressId") LongParam addressId,
            @NotNull Address address) {
        Optional<Response> validationResponse = validateAddress(address);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }

        Optional<AddressEntity> optionalAddressEntity = addressDAO.findByIdAndPersonId(addressId.get(), personId.get());

        if (optionalAddressEntity.isPresent()) {
            AddressEntity addressEntity = optionalAddressEntity.get();
            addressEntity.setAddress(address.getAddress());
            addressEntity = addressDAO.update(addressEntity);
            return Response.status(Response.Status.OK).entity(addressFromEntity(addressEntity)).build();
        } else {
            return createAddressNotFoundResponse();
        }
    }

    @POST
    @Path("{personId : \\d+}/addresses")
    @Consumes(MediaType.APPLICATION_JSON)
    @UnitOfWork
    @NotNull
    public Response addAddress(
            @PathParam("personId") LongParam personId,
            @NotNull Address address) {
        Optional<Response> validationResponse = validateAddress(address);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }

        Optional<PersonEntity> optionalPersonEntity = personDAO.findById(personId.get());
        if (optionalPersonEntity.isPresent()) {
            AddressEntity addressEntity = addressDAO.create(address.getAddress(), optionalPersonEntity.get());
            return Response.created(URI.create("/v2/people/" + personId.get())).entity(addressFromEntity(addressEntity)).build();
        } else {
            return createPersonNotFoundResponse();
        }
    }

    /**
     * Create the serialized Person from a PersonEntity
     */
    private
    @NotNull
    Address addressFromEntity(@NotNull AddressEntity addressEntity) {
        return new Address(addressEntity.getId(), addressEntity.getAddress(), addressEntity.getPerson().getId());
    }

    @NotNull
    private Response createAddressNotFoundResponse() {
        return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResult(Response.Status.NOT_FOUND, "Address not found")).build();
    }

    private
    @NotNull
    Optional<Response> validateAddress(@NotNull Address address) {
        if (address.getAddress() == null) {
            return Optional.of(Response.status(Response.Status.BAD_REQUEST).entity(new Error("Address is missing")).build());
        } else {
            return Optional.empty();
        }
    }

}
