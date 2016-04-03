/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables.records;


import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.TableRecordImpl;
import restwars.storage.jooq.tables.HangarShips;

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
public class HangarShipsRecord extends TableRecordImpl<HangarShipsRecord> implements Record3<UUID, String, Integer> {

    private static final long serialVersionUID = 1609674250;

    /**
     * Setter for <code>PUBLIC.HANGAR_SHIPS.HANGAR_ID</code>.
     */
    public void setHangarId(UUID value) {
        setValue(0, value);
    }

    /**
     * Getter for <code>PUBLIC.HANGAR_SHIPS.HANGAR_ID</code>.
     */
    public UUID getHangarId() {
        return (UUID) getValue(0);
    }

    /**
     * Setter for <code>PUBLIC.HANGAR_SHIPS.TYPE</code>.
     */
    public void setType(String value) {
        setValue(1, value);
    }

    /**
     * Getter for <code>PUBLIC.HANGAR_SHIPS.TYPE</code>.
     */
    public String getType() {
        return (String) getValue(1);
    }

    /**
     * Setter for <code>PUBLIC.HANGAR_SHIPS.AMOUNT</code>.
     */
    public void setAmount(Integer value) {
        setValue(2, value);
    }

    /**
     * Getter for <code>PUBLIC.HANGAR_SHIPS.AMOUNT</code>.
     */
    public Integer getAmount() {
        return (Integer) getValue(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<UUID, String, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<UUID, String, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return HangarShips.HANGAR_SHIPS.HANGAR_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return HangarShips.HANGAR_SHIPS.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return HangarShips.HANGAR_SHIPS.AMOUNT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID value1() {
        return getHangarId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getAmount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HangarShipsRecord value1(UUID value) {
        setHangarId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HangarShipsRecord value2(String value) {
        setType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HangarShipsRecord value3(Integer value) {
        setAmount(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HangarShipsRecord values(UUID value1, String value2, Integer value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached HangarShipsRecord
     */
    public HangarShipsRecord() {
        super(HangarShips.HANGAR_SHIPS);
    }

    /**
     * Create a detached, initialised HangarShipsRecord
     */
    public HangarShipsRecord(UUID hangarId, String type, Integer amount) {
        super(HangarShips.HANGAR_SHIPS);

        setValue(0, hangarId);
        setValue(1, type);
        setValue(2, amount);
    }
}
