package com.octo.zdd_java_sql.db;

import com.octo.zdd_java_sql.core.PersonEntity;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public class PersonDAO extends AbstractDAO<PersonEntity> {

    public PersonDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @NotNull
    public List<PersonEntity> findAll() {
        return list(namedQuery("com.octo.zdd_java_sql.core.PersonEntity.findAll"));
    }

    @NotNull
    public Optional<PersonEntity> findById(@NotNull Long id) {
        return Optional.ofNullable(get(id));
    }

    @NotNull
    public PersonEntity create(@NotNull String name, @Nullable String address) {
        PersonEntity person = new PersonEntity();
        person.setName(name);
        person.setAddress(address);
        return persist(person);
    }

    @NotNull
    public PersonEntity update(@NotNull PersonEntity person) {
        return persist(person);
    }

    public void delete(@NotNull PersonEntity person) {
        currentSession().delete(person);
    }

}
