/*
 * This file is generated by jOOQ.
*/
package restwars.storage.jooq.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;
import restwars.storage.jooq.tables.Planets;

import javax.annotation.Generated;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@Generated(
        value = {
                "http://www.jooq.org",
                "jOOQ version:3.9.0"
        },
        comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class PlanetsRecord extends UpdatableRecordImpl<PlanetsRecord> implements Record8<UUID, UUID, Integer, Integer, Integer, Integer, Integer, Integer> {

    private static final long serialVersionUID = -399338843;

    /**
     * Setter for <code>PUBLIC.PLANETS.ID</code>.
     */
    public void setId(UUID value) {
        set(0, value);
    }

    /**
     * Getter for <code>PUBLIC.PLANETS.ID</code>.
     */
    public UUID getId() {
        return (UUID) get(0);
    }

    /**
     * Setter for <code>PUBLIC.PLANETS.OWNER_ID</code>.
     */
    public void setOwnerId(UUID value) {
        set(1, value);
    }

    /**
     * Getter for <code>PUBLIC.PLANETS.OWNER_ID</code>.
     */
    public UUID getOwnerId() {
        return (UUID) get(1);
    }

    /**
     * Setter for <code>PUBLIC.PLANETS.GALAXY</code>.
     */
    public void setGalaxy(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>PUBLIC.PLANETS.GALAXY</code>.
     */
    public Integer getGalaxy() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>PUBLIC.PLANETS.SYSTEM</code>.
     */
    public void setSystem(Integer value) {
        set(3, value);
    }

    /**
     * Getter for <code>PUBLIC.PLANETS.SYSTEM</code>.
     */
    public Integer getSystem() {
        return (Integer) get(3);
    }

    /**
     * Setter for <code>PUBLIC.PLANETS.PLANET</code>.
     */
    public void setPlanet(Integer value) {
        set(4, value);
    }

    /**
     * Getter for <code>PUBLIC.PLANETS.PLANET</code>.
     */
    public Integer getPlanet() {
        return (Integer) get(4);
    }

    /**
     * Setter for <code>PUBLIC.PLANETS.CRYSTAL</code>.
     */
    public void setCrystal(Integer value) {
        set(5, value);
    }

    /**
     * Getter for <code>PUBLIC.PLANETS.CRYSTAL</code>.
     */
    public Integer getCrystal() {
        return (Integer) get(5);
    }

    /**
     * Setter for <code>PUBLIC.PLANETS.GAS</code>.
     */
    public void setGas(Integer value) {
        set(6, value);
    }

    /**
     * Getter for <code>PUBLIC.PLANETS.GAS</code>.
     */
    public Integer getGas() {
        return (Integer) get(6);
    }

    /**
     * Setter for <code>PUBLIC.PLANETS.ENERGY</code>.
     */
    public void setEnergy(Integer value) {
        set(7, value);
    }

    /**
     * Getter for <code>PUBLIC.PLANETS.ENERGY</code>.
     */
    public Integer getEnergy() {
        return (Integer) get(7);
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
    // Record8 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<UUID, UUID, Integer, Integer, Integer, Integer, Integer, Integer> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<UUID, UUID, Integer, Integer, Integer, Integer, Integer, Integer> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return Planets.PLANETS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field2() {
        return Planets.PLANETS.OWNER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return Planets.PLANETS.GALAXY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field4() {
        return Planets.PLANETS.SYSTEM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field5() {
        return Planets.PLANETS.PLANET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field6() {
        return Planets.PLANETS.CRYSTAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field7() {
        return Planets.PLANETS.GAS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field8() {
        return Planets.PLANETS.ENERGY;
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
        return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getGalaxy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value4() {
        return getSystem();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value5() {
        return getPlanet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value6() {
        return getCrystal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value7() {
        return getGas();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value8() {
        return getEnergy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord value1(UUID value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord value2(UUID value) {
        setOwnerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord value3(Integer value) {
        setGalaxy(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord value4(Integer value) {
        setSystem(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord value5(Integer value) {
        setPlanet(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord value6(Integer value) {
        setCrystal(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord value7(Integer value) {
        setGas(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord value8(Integer value) {
        setEnergy(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanetsRecord values(UUID value1, UUID value2, Integer value3, Integer value4, Integer value5, Integer value6, Integer value7, Integer value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached PlanetsRecord
     */
    public PlanetsRecord() {
        super(Planets.PLANETS);
    }

    /**
     * Create a detached, initialised PlanetsRecord
     */
    public PlanetsRecord(UUID id, UUID ownerId, Integer galaxy, Integer system, Integer planet, Integer crystal, Integer gas, Integer energy) {
        super(Planets.PLANETS);

        set(0, id);
        set(1, ownerId);
        set(2, galaxy);
        set(3, system);
        set(4, planet);
        set(5, crystal);
        set(6, gas);
        set(7, energy);
    }
}
