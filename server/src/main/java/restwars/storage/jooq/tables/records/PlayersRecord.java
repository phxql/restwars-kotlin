/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables.records;


import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;
import restwars.storage.jooq.tables.Players;

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
public class PlayersRecord extends UpdatableRecordImpl<PlayersRecord> implements Record3<UUID, String, String> {

	private static final long serialVersionUID = 1532292638;

	/**
	 * Setter for <code>PUBLIC.PLAYERS.ID</code>.
	 */
	public void setId(UUID value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>PUBLIC.PLAYERS.ID</code>.
	 */
	public UUID getId() {
		return (UUID) getValue(0);
	}

	/**
	 * Setter for <code>PUBLIC.PLAYERS.USERNAME</code>.
	 */
	public void setUsername(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>PUBLIC.PLAYERS.USERNAME</code>.
	 */
	public String getUsername() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>PUBLIC.PLAYERS.PASSWORD</code>.
	 */
	public void setPassword(String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>PUBLIC.PLAYERS.PASSWORD</code>.
	 */
	public String getPassword() {
		return (String) getValue(2);
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
	// Record3 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row3<UUID, String, String> fieldsRow() {
		return (Row3) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row3<UUID, String, String> valuesRow() {
		return (Row3) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<UUID> field1() {
		return Players.PLAYERS.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return Players.PLAYERS.USERNAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field3() {
		return Players.PLAYERS.PASSWORD;
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
	public String value2() {
		return getUsername();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value3() {
		return getPassword();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlayersRecord value1(UUID value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlayersRecord value2(String value) {
		setUsername(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlayersRecord value3(String value) {
		setPassword(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlayersRecord values(UUID value1, String value2, String value3) {
		value1(value1);
		value2(value2);
		value3(value3);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached PlayersRecord
	 */
	public PlayersRecord() {
		super(Players.PLAYERS);
	}

	/**
	 * Create a detached, initialised PlayersRecord
	 */
	public PlayersRecord(UUID id, String username, String password) {
		super(Players.PLAYERS);

		setValue(0, id);
		setValue(1, username);
		setValue(2, password);
	}
}
