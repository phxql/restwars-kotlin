package restwars.storage

import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import restwars.business.database.MigrationService
import javax.sql.DataSource

class FlywayMigrationService(val dataSource: DataSource) : MigrationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun migrate() {
        logger.info("Running database migrations")

        val flyway = Flyway()
        flyway.dataSource = dataSource
        flyway.migrate()

        logger.info("Database migrated")
    }
}