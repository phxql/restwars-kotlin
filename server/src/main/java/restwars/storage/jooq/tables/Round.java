/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables;


import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import restwars.storage.jooq.Public;
import restwars.storage.jooq.tables.records.RoundRecord;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
        value = {
                "http://www.jooq.org",
                "jOOQ version:3.7.3"
        },
        comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Round extends TableImpl<RoundRecord> {

    private static final long serialVersionUID = 1265609495;

    /**
     * The reference instance of <code>PUBLIC.ROUND</code>
     */
    public static final Round ROUND = new Round();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RoundRecord> getRecordType() {
        return RoundRecord.class;
    }

    /**
     * The column <code>PUBLIC.ROUND.ROUND</code>.
     */
    public final TableField<RoundRecord, Long> ROUND_ = createField("ROUND", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>PUBLIC.ROUND</code> table reference
     */
    public Round() {
        this("ROUND", null);
    }

    /**
     * Create an aliased <code>PUBLIC.ROUND</code> table reference
     */
    public Round(String alias) {
        this(alias, ROUND);
    }

    private Round(String alias, Table<RoundRecord> aliased) {
        this(alias, aliased, null);
    }

    private Round(String alias, Table<RoundRecord> aliased, Field<?>[] parameters) {
        super(alias, Public.PUBLIC, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Round as(String alias) {
        return new Round(alias, this);
    }

    /**
     * Rename this table
     */
    public Round rename(String name) {
        return new Round(name, null);
    }
}