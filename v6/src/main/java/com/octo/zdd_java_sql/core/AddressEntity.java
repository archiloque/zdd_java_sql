package com.octo.zdd_java_sql.core;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "com.octo.zdd_java_sql.core.AddressEntity.findByPerson",
                query = "from AddressEntity a where a.person = :person"
        ),
        @NamedQuery(
                name = "com.octo.zdd_java_sql.core.AddressEntity.deleteByPerson",
                query = "delete FROM AddressEntity a where a.person = :person"
        ),
        @NamedQuery(
                name = "com.octo.zdd_java_sql.core.AddressEntity.findByIdAndPersonId",
                query = "from AddressEntity a where a.id = :addressId and a.person.id = :personId"
        ),
})
@Table(name = "address")
public class AddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    @NotNull
    private PersonEntity person;

    @NotNull
    private String address;

    public AddressEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NotNull
    public String getAddress() {
        return address;
    }

    public void setAddress(@NotNull String address) {
        this.address = address;
    }

    @NotNull
    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(@NotNull PersonEntity person) {
        this.person = person;
    }
}
