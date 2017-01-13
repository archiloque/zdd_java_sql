package com.octo.zdd_java_sql.api.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class Addresses {

    private List<Address> addresses;

    public Addresses() {
    }

    @JsonProperty
    @NotNull
    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(@NotNull List<Address> addresses) {
        this.addresses = addresses;
    }
}
