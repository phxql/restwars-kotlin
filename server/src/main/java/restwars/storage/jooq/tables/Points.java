/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables;


import org.jooq.*;
import org.jooq.impl.TableImpl;
import restwars.storage.jooq.Keys;
import restwars.storage.jooq.Public;
import restwars.storage.jooq.tables.records.PointsRecord;

import javax.annotation.Generated;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


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
public class Points extends TableImpl<PointsRecord> {

    private static final long serialVersionUID = -796410160;

    /**
     * The reference instance of <code>PUBLIC.POINTS</code>
     */
    public static final Points POINTS = new Points();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PointsRecord> getRecordType() {
        return PointsRecord.class;
    }

    /**
     * The column <code>PUBLIC.POINTS.ID</code>.
     */
    public final TableField<PointsRecord, UUID> ID = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>PUBLIC.POINTS.PLAYER_ID</code>.
     */
    public final TableField<PointsRecord, UUID> PLAYER_ID = createField("PLAYER_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>PUBLIC.POINTS.POINTS</code>.
     */
    public final TableField<PointsRecord, Long> POINTS_ = createField("POINTS", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>PUBLIC.POINTS.ROUND</code>.
     */
    public final TableField<PointsRecord, Long> ROUND = createField("ROUND", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>PUBLIC.POINTS</code> table reference
     */
    public Points() {
        this("POINTS", null);
    }

    /**
     * Create an aliased <code>PUBLIC.POINTS</code> table reference
     */
    public Points(String alias) {
        this(alias, POINTS);
    }

    private Points(String alias, Table<PointsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Points(String alias, Table<PointsRecord> aliased, Field<?>[] parameters) {
        super(alias, Public.PUBLIC, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PointsRecord> getPrimaryKey() {
        return Keys.CONSTRAINT_8;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PointsRecord>> getKeys() {
        return Arrays.<UniqueKey<PointsRecord>>asList(Keys.CONSTRAINT_8, Keys.CONSTRAINT_8C);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<PointsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<PointsRecord, ?>>asList(Keys.CONSTRAINT_8CF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Points as(String alias) {
        return new Points(alias, this);
    }

    /**
     * Rename this table
     */
    public Points rename(String name) {
        return new Points(name, null);
    }
}
