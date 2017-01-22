package restwars.storage

import org.h2.jdbcx.JdbcDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL

abstract class AbstractJooqTest {
    protected lateinit var jooq: DSLContext
        private set

    open fun setUp() {
        val dataSource = JdbcDataSource()
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        dataSource.user = ""
        dataSource.password = ""
        FlywayMigrationService(dataSource).migrate()

        val dialect = SQLDialect.H2
        jooq = DSL.using(dataSource, dialect)
    }
}