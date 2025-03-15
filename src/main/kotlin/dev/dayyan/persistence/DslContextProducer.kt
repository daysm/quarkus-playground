package dev.dayyan.persistence

import jakarta.enterprise.context.RequestScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import javax.sql.DataSource

class DslContextProducer {
    // IntelliJ mistakenly claims that there is no bean that matches injection point
    @Inject
    private lateinit var dataSource: DataSource

    @Produces
    @RequestScoped
    fun getDslContext(): DSLContext {
        return try {
            DSL.using(getConfiguration())
        } catch (e: DataAccessException) {
            throw RuntimeException("Error creating DSLContext", e)
        }
    }

    private fun getConfiguration(): Configuration {
        return DefaultConfiguration()
            .set(dataSource)
            .set(SQLDialect.POSTGRES)
            .set(
                Settings()
                    .withExecuteLogging(true)
                    .withRenderFormatted(true)
                    .withRenderCatalog(false)
                    .withRenderSchema(false)
                    .withMaxRows(Integer.MAX_VALUE)
                    .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
                    .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED),
            )
    }
}
