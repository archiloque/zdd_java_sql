package com.octo.zdd_java_sql.db;

import org.hibernate.SessionFactory;

import javax.validation.constraints.NotNull;

public class AbstractDAO<E> extends io.dropwizard.hibernate.AbstractDAO<E> {

    AbstractDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public void delete(@NotNull E e) {
        currentSession().delete(e);
    }

    @NotNull
    public E update(@NotNull E e) {
        return persist(e);
    }


}
