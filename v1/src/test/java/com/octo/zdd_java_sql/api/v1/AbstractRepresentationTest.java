package com.octo.zdd_java_sql.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;

public abstract class AbstractRepresentationTest {

    static final ObjectMapper MAPPER = Jackson.newObjectMapper();

}
