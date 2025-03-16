package dev.dayyan

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach

@QuarkusTest
abstract class PostgresTestBase {
    // IntelliJ mistakenly claims that there is no bean that matches the injection point
    @Inject
    @Suppress("CdiInjectionPointsInspection")
    lateinit var flyway: Flyway

    @BeforeEach
    fun setup() {
        flyway.clean()
        flyway.migrate()
    }
}
