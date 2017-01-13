package com.octo.zdd_java_sql.db;

import com.octo.zdd_java_sql.core.AddressEntity;
import com.octo.zdd_java_sql.core.PersonEntity;
import org.hibernate.SessionFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public class AddressDAO extends AbstractDAO<AddressEntity> {

    public AddressDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @NotNull
    public List<AddressEntity> findByPerson(@NotNull PersonEntity person) {
        return list(namedQuery("com.octo.zdd_java_sql.core.AddressEntity.findByPerson").
                setParameter("person", person)
        );
    }

    @NotNull
    public void deleteByPerson(@NotNull PersonEntity person) {
        namedQuery("com.octo.zdd_java_sql.core.AddressEntity.deleteByPerson").
                setParameter("person", person).
                executeUpdate();
    }

    @NotNull
    public Optional<AddressEntity> findByIdAndPersonId(@NotNull Long addressId, @NotNull Long personId) {
        return Optional.ofNullable(
                uniqueResult(
                        namedQuery("com.octo.zdd_java_sql.core.AddressEntity.findByIdAndPersonId").
                                setParameter("personId", personId).
                                setParameter("addressId", addressId)));
    }

    @NotNull
    public AddressEntity create(@NotNull String addressContent, @NotNull PersonEntity person) {
        AddressEntity address = new AddressEntity();
        address.setPerson(person);
        address.setAddress(addressContent);
        return persist(address);
    }

}
