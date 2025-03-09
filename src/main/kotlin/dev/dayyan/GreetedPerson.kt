package dev.dayyan

import io.quarkus.hibernate.orm.panache.PanacheEntity
import jakarta.persistence.Entity

@Entity
class GreetedPerson : PanacheEntity() {
    var name: String? = null
}
