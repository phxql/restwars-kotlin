/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;
import restwars.storage.jooq.tables.DetectedFlights;

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
public class DetectedFlightsRecord extends UpdatableRecordImpl<DetectedFlightsRecord> implements Record5<UUID, UUID, UUID, Long, Long> {

    private static final long serialVersionUID = 1387539761;

    /**
     * Setter for <code>PUBLIC.DETECTED_FLIGHTS.ID</code>.
     */
    public void setId(UUID value) {
        setValue(0, value);
    }

    /**
     * Getter for <code>PUBLIC.DETECTED_FLIGHTS.ID</code>.
     */
    public UUID getId() {
        return (UUID) getValue(0);
    }

    /**
     * Setter for <code>PUBLIC.DETECTED_FLIGHTS.FLIGHT_ID</code>.
     */
    public void setFlightId(UUID value) {
        setValue(1, value);
    }

    /**
     * Getter for <code>PUBLIC.DETECTED_FLIGHTS.FLIGHT_ID</code>.
     */
    public UUID getFlightId() {
        return (UUID) getValue(1);
    }

    /**
     * Setter for <code>PUBLIC.DETECTED_FLIGHTS.PLAYER_ID</code>.
     */
    public void setPlayerId(UUID value) {
        setValue(2, value);
    }

    /**
     * Getter for <code>PUBLIC.DETECTED_FLIGHTS.PLAYER_ID</code>.
     */
    public UUID getPlayerId() {
        return (UUID) getValue(2);
    }

    /**
     * Setter for <code>PUBLIC.DETECTED_FLIGHTS.DETECTED_IN_ROUND</code>.
     */
    public void setDetectedInRound(Long value) {
        setValue(3, value);
    }

    /**
     * Getter for <code>PUBLIC.DETECTED_FLIGHTS.DETECTED_IN_ROUND</code>.
     */
    public Long getDetectedInRound() {
        return (Long) getValue(3);
    }

    /**
     * Setter for <code>PUBLIC.DETECTED_FLIGHTS.APPROXIMATED_FLIGHT_SIZE</code>.
     */
    public void setApproximatedFlightSize(Long value) {
        setValue(4, value);
    }

    /**
     * Getter for <code>PUBLIC.DETECTED_FLIGHTS.APPROXIMATED_FLIGHT_SIZE</code>.
     */
    public Long getApproximatedFlightSize() {
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
    public Row5<UUID, UUID, UUID, Long, Long> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<UUID, UUID, UUID, Long, Long> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field1() {
        return DetectedFlights.DETECTED_FLIGHTS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field2() {
        return DetectedFlights.DETECTED_FLIGHTS.FLIGHT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<UUID> field3() {
        return DetectedFlights.DETECTED_FLIGHTS.PLAYER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field4() {
        return DetectedFlights.DETECTED_FLIGHTS.DETECTED_IN_ROUND;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field5() {
        return DetectedFlights.DETECTED_FLIGHTS.APPROXIMATED_FLIGHT_SIZE;
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
        return getFlightId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID value3() {
        return getPlayerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value4() {
        return getDetectedInRound();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value5() {
        return getApproximatedFlightSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetectedFlightsRecord value1(UUID value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetectedFlightsRecord value2(UUID value) {
        setFlightId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetectedFlightsRecord value3(UUID value) {
        setPlayerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetectedFlightsRecord value4(Long value) {
        setDetectedInRound(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetectedFlightsRecord value5(Long value) {
        setApproximatedFlightSize(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DetectedFlightsRecord values(UUID value1, UUID value2, UUID value3, Long value4, Long value5) {
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
     * Create a detached DetectedFlightsRecord
     */
    public DetectedFlightsRecord() {
        super(DetectedFlights.DETECTED_FLIGHTS);
    }

    /**
     * Create a detached, initialised DetectedFlightsRecord
     */
    public DetectedFlightsRecord(UUID id, UUID flightId, UUID playerId, Long detectedInRound, Long approximatedFlightSize) {
        super(DetectedFlights.DETECTED_FLIGHTS);

        setValue(0, id);
        setValue(1, flightId);
        setValue(2, playerId);
        setValue(3, detectedInRound);
        setValue(4, approximatedFlightSize);
    }
}
