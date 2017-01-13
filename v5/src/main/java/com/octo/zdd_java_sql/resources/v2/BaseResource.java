package com.octo.zdd_java_sql.resources.v2;

import com.octo.zdd_java_sql.api.v2.ErrorResult;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

abstract class BaseResource {

    @NotNull
    Response createPersonNotFoundResponse() {
        return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResult(Response.Status.NOT_FOUND, "Person not found")).build();
    }

}
