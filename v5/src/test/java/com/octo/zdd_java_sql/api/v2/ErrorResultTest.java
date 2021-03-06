package com.octo.zdd_java_sql.api.v2;

import com.octo.zdd_java_sql.api.AbstractRepresentationTest;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResultTest extends AbstractRepresentationTest {

    @Test
    public void serializesToJSON() throws Exception {
        final ErrorResult errorResult = createErrorResult();
        final String expected = MAPPER.writeValueAsString(readErrorResultFixture());
        assertThat(MAPPER.writeValueAsString(errorResult)).isEqualTo(expected);
    }

    private com.octo.zdd_java_sql.api.v1.ErrorResult readErrorResultFixture() throws java.io.IOException {
        return MAPPER.readValue(fixture("fixtures/v2/error_result.json"), com.octo.zdd_java_sql.api.v1.ErrorResult.class);
    }


    @Test
    public void deserializesFromJSON() throws Exception {
        final ErrorResult errorResult = createErrorResult();
        com.octo.zdd_java_sql.api.v1.ErrorResult actual = readErrorResultFixture();
        assertThat(actual.getCode()).isEqualTo(errorResult.getCode());
        assertThat(actual.getMessage()).isEqualTo(errorResult.getMessage());
    }

    private ErrorResult createErrorResult() {
        return new ErrorResult(Response.Status.NOT_FOUND, "Person not found");
    }


}
