package dev.dayyan.persistence

import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.Produces
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import javax.sql.DataSource

class DslContextProducer(
    private val dataSource: DataSource,
) {
    @get:RequestScoped
    @get:Produces
    val dslContext: DSLContext
        get() {
            try {
                return DSL.using(configuration)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

    private val configuration: Configuration
        get() =
            DefaultConfiguration()
                .set(dataSource)
                .set(
                    Settings()
                        .withExecuteLogging(true)
                        .withRenderFormatted(true)
                        .withRenderCatalog(false)
                        .withRenderSchema(false)
                        .withMaxRows(Int.MAX_VALUE)
                        .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_UNQUOTED)
                        .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED),
                )
}
