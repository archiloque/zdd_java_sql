package com.octo.zdd_java_sql.resources;

import com.octo.zdd_java_sql.core.PersonEntity;
import com.octo.zdd_java_sql.db.AddressDAO;
import com.octo.zdd_java_sql.db.PersonDAO;
import io.dropwizard.jersey.params.LongParam;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

public final class ResourceHelper {

    private ResourceHelper() {
    }

    public static
    @Nullable
    Response deletePerson(
            @NotNull LongParam personId,
            @NotNull PersonDAO personDAO,
            @NotNull AddressDAO addressDAO) {
        // lock to ensure the address are not updated / inserted
        PersonEntity personEntity = personDAO.findByIdWithLock(personId.get());
        if (personEntity != null) {
            addressDAO.deleteByPerson(personEntity);
            personDAO.delete(personEntity);
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return null;
        }
    }

}
