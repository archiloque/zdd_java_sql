package com.octo.zdd_java_sql.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;

public abstract class AbstractRepresentationTest {

    protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();

}
