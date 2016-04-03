/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;
import restwars.storage.jooq.tables.ConstructionSites;

import javax.annotation.Generated;
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
public class ConstructionSitesRecord extends UpdatableRecordImpl<ConstructionSitesRecord> implements Record5<UUID, UUID, String, Integer, Long> {

    private static final long serialVersionUID = -467417018;

    /**
     * Setter for <code>PUBLIC.CONSTRUCTION_SITES.ID</code>.
     */
    public void setId(UUID value) {
        setValue(0, value);
    }

    /**
     * Getter for <code>PUBLIC.CONSTRUCTION_SITES.ID</code>.
     */
    public UUID getId() {
        return (UUID) getValue(0);
    }

    /**
     * Setter for <code>PUBLIC.CONSTRUCTION_SITES.PLANET_ID</code>.
     */
    public void setPlanetId(UUID value) {
        setValue(1, value);
    }

    /**
     * Getter for <code>PUBLIC.CONSTRUCTION_SITES.PLANET_ID</code>.
     */
    public UUID getPlanetId() {
        return (UUID) getValue(1);
    }

    /**
     * Setter for <code>PUBLIC.CONSTRUCTION_SITES.TYPE</code>.
     */
    public void setType(String value) {
        setValue(2, value);
    }

    /**
     * Getter for <code>PUBLIC.CONSTRUCTION_SITES.TYPE</code>.
     */
    public String getType() {
        return (String) getValue(2);
    }

    /**
     * Setter for <code>PUBLIC.CONSTRUCTION_SITES.LEVEL</code>.
     */
    public void setLevel(Integer value) {
        setValue(3, value);
    }

    /**
     * Getter for <code>PUBLIC.CONSTRUCTION_SITES.LEVEL</code>.
     */
    public Integer getLevel() {
        return (Integer) getValue(3);
    }

    /**
     * Setter for <code>PUBLIC.CONSTRUCTION_SITES.DONE</code>.
     */
    public void setDone(Long value) {
        setValue(4, value);
    }

    /**
     * Getter for <code>PUBLIC.CONSTRUCTION_SITES.DONE</code>.
     */
    public Long getDone() {
        return (Long) getValue(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<UUID> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<UUID, UUID, String, Integer, Long> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<UUID, UUID, String, Integer, Long> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return ConstructionSites.CONSTRUCTION_SITES.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field2() {
        return ConstructionSites.CONSTRUCTION_SITES.PLANET_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return ConstructionSites.CONSTRUCTION_SITES.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field4() {
        return ConstructionSites.CONSTRUCTION_SITES.LEVEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field5() {
        return ConstructionSites.CONSTRUCTION_SITES.DONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID value2() {
        return getPlanetId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value4() {
        return getLevel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value5() {
        return getDone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConstructionSitesRecord value1(UUID value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConstructionSitesRecord value2(UUID value) {
        setPlanetId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConstructionSitesRecord value3(String value) {
        setType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConstructionSitesRecord value4(Integer value) {
        setLevel(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConstructionSitesRecord value5(Long value) {
        setDone(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConstructionSitesRecord values(UUID value1, UUID value2, String value3, Integer value4, Long value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ConstructionSitesRecord
     */
    public ConstructionSitesRecord() {
        super(ConstructionSites.CONSTRUCTION_SITES);
    }

    /**
     * Create a detached, initialised ConstructionSitesRecord
     */
    public ConstructionSitesRecord(UUID id, UUID planetId, String type, Integer level, Long done) {
        super(ConstructionSites.CONSTRUCTION_SITES);

        setValue(0, id);
        setValue(1, planetId);
        setValue(2, type);
        setValue(3, level);
        setValue(4, done);
    }
}
