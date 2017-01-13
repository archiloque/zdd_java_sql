package com.octo.zdd_java_sql.resources;

import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public abstract class AbstractResourceTest {

    @NotNull
    protected PersonEntity createPersonEntity(@Nullable Long id, @Nullable String name, @Nullable String address) {
        PersonEntity personEntity = new PersonEntity();
        if (id != null) {
            personEntity.setId(id);
        }
        if (name != null) {
            personEntity.setName(name);
        }
        if (address != null) {
            personEntity.setAddress(address);
        }
        return personEntity;
    }


    @NotNull
    protected AddressEntity createAddressEntity(@Nullable Long id, @Nullable String address, @Nullable PersonEntity personEntity) {
        AddressEntity addressEntity = new AddressEntity();
        if (id != null) {
            addressEntity.setId(id);
        }
        if (address != null) {
            addressEntity.setAddress(address);
        }
        if (personEntity != null) {
            addressEntity.setPerson(personEntity);
        }
        return addressEntity;
    }
}
