package com.octo.zdd_java_sql.resources;

import com.octo.zdd_java_sql.core.PersonEntity;
import com.octo.zdd_java_sql.db.AddressDAO;
import com.octo.zdd_java_sql.db.PersonDAO;
import io.dropwizard.jersey.params.LongParam;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 *
 */
public final class ResourceHelper {

    private ResourceHelper() {
    }

    public static
    @NotNull
    Optional<Response> deletePerson(
            @NotNull LongParam personId,
            @NotNull PersonDAO personDAO,
            @NotNull AddressDAO addressDAO) {
        // lock to ensure the address are not updated / inserted
        Optional<PersonEntity> optionalPersonEntity = personDAO.findByIdWithLock(personId.get());
        if (optionalPersonEntity.isPresent()) {
            PersonEntity personEntity = optionalPersonEntity.get();
            addressDAO.deleteByPerson(personEntity);
            personDAO.delete(personEntity);
            return Optional.of(Response.status(Response.Status.NO_CONTENT).build());
        } else {
            return Optional.empty();
        }
    }

}
