/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables;


import org.jooq.*;
import org.jooq.impl.TableImpl;
import restwars.storage.jooq.Keys;
import restwars.storage.jooq.Public;
import restwars.storage.jooq.tables.records.FlightsRecord;

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
public class Flights extends TableImpl<FlightsRecord> {

	private static final long serialVersionUID = 1680358782;

	/**
	 * The reference instance of <code>PUBLIC.FLIGHTS</code>
	 */
	public static final Flights FLIGHTS = new Flights();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<FlightsRecord> getRecordType() {
		return FlightsRecord.class;
	}

	/**
	 * The column <code>PUBLIC.FLIGHTS.ID</code>.
	 */
	public final TableField<FlightsRecord, UUID> ID = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.PLAYER_ID</code>.
	 */
	public final TableField<FlightsRecord, UUID> PLAYER_ID = createField("PLAYER_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.START_GALAXY</code>.
	 */
	public final TableField<FlightsRecord, Integer> START_GALAXY = createField("START_GALAXY", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.START_SYSTEM</code>.
	 */
	public final TableField<FlightsRecord, Integer> START_SYSTEM = createField("START_SYSTEM", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.START_PLANET</code>.
	 */
	public final TableField<FlightsRecord, Integer> START_PLANET = createField("START_PLANET", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.DESTINATION_GALAXY</code>.
	 */
	public final TableField<FlightsRecord, Integer> DESTINATION_GALAXY = createField("DESTINATION_GALAXY", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.DESTINATION_SYSTEM</code>.
	 */
	public final TableField<FlightsRecord, Integer> DESTINATION_SYSTEM = createField("DESTINATION_SYSTEM", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.DESTINATION_PLANET</code>.
	 */
	public final TableField<FlightsRecord, Integer> DESTINATION_PLANET = createField("DESTINATION_PLANET", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.STARTED_IN_ROUND</code>.
	 */
	public final TableField<FlightsRecord, Long> STARTED_IN_ROUND = createField("STARTED_IN_ROUND", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.ARRIVAL_IN_ROUND</code>.
	 */
	public final TableField<FlightsRecord, Long> ARRIVAL_IN_ROUND = createField("ARRIVAL_IN_ROUND", org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.DIRECTION</code>.
	 */
	public final TableField<FlightsRecord, String> DIRECTION = createField("DIRECTION", org.jooq.impl.SQLDataType.VARCHAR.length(50).nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.TYPE</code>.
	 */
	public final TableField<FlightsRecord, String> TYPE = createField("TYPE", org.jooq.impl.SQLDataType.VARCHAR.length(50).nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.CARGO_CRYSTAL</code>.
	 */
	public final TableField<FlightsRecord, Integer> CARGO_CRYSTAL = createField("CARGO_CRYSTAL", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.CARGO_GAS</code>.
	 */
	public final TableField<FlightsRecord, Integer> CARGO_GAS = createField("CARGO_GAS", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.DETECTED</code>.
	 */
	public final TableField<FlightsRecord, Boolean> DETECTED = createField("DETECTED", org.jooq.impl.SQLDataType.BOOLEAN.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FLIGHTS.SPEED</code>.
	 */
	public final TableField<FlightsRecord, Double> SPEED = createField("SPEED", org.jooq.impl.SQLDataType.DOUBLE.nullable(false), this, "");

	/**
	 * Create a <code>PUBLIC.FLIGHTS</code> table reference
	 */
	public Flights() {
		this("FLIGHTS", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.FLIGHTS</code> table reference
	 */
	public Flights(String alias) {
		this(alias, FLIGHTS);
	}

	private Flights(String alias, Table<FlightsRecord> aliased) {
		this(alias, aliased, null);
	}

	private Flights(String alias, Table<FlightsRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<FlightsRecord> getPrimaryKey() {
		return Keys.CONSTRAINT_F;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<FlightsRecord>> getKeys() {
		return Arrays.<UniqueKey<FlightsRecord>>asList(Keys.CONSTRAINT_F);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<FlightsRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<FlightsRecord, ?>>asList(Keys.CONSTRAINT_FC);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Flights as(String alias) {
		return new Flights(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Flights rename(String name) {
		return new Flights(name, null);
	}
}
