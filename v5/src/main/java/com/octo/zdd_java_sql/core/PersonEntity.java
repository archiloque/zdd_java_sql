package com.octo.zdd_java_sql.core;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "com.octo.zdd_java_sql.core.PersonEntity.findAll",
                query = "from PersonEntity p"
        ),
        @NamedQuery(
                name = "com.octo.zdd_java_sql.core.PersonEntity.findAllWithLock",
                query = "from PersonEntity p",
                lockMode = LockModeType.PESSIMISTIC_WRITE
        ),
        @NamedQuery(
                name = "com.octo.zdd_java_sql.core.PersonEntity.findAllWithJoin",
                query = "from PersonEntity p left join p.addresses a"
        ),
        @NamedQuery(
                name = "com.octo.zdd_java_sql.core.PersonEntity.findByIdWithJoin",
                query = "from PersonEntity p left join p.addresses a where p.id = :personId"
        ),
})
@Table(name = "person")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String name;

    @OneToMany(mappedBy = "person")
    private List<AddressEntity> addresses;

    public PersonEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AddressEntity> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressEntity> addresses) {
        this.addresses = addresses;
    }
}
