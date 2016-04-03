/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables;


import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
import restwars.storage.jooq.Keys;
import restwars.storage.jooq.Public;
import restwars.storage.jooq.tables.records.PlayersRecord;

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
public class Players extends TableImpl<PlayersRecord> {

	private static final long serialVersionUID = -2047027445;

	/**
	 * The reference instance of <code>PUBLIC.PLAYERS</code>
	 */
	public static final Players PLAYERS = new Players();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<PlayersRecord> getRecordType() {
		return PlayersRecord.class;
	}

	/**
	 * The column <code>PUBLIC.PLAYERS.ID</code>.
	 */
	public final TableField<PlayersRecord, UUID> ID = createField("ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.PLAYERS.USERNAME</code>.
	 */
	public final TableField<PlayersRecord, String> USERNAME = createField("USERNAME", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.PLAYERS.PASSWORD</code>.
	 */
	public final TableField<PlayersRecord, String> PASSWORD = createField("PASSWORD", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

	/**
	 * Create a <code>PUBLIC.PLAYERS</code> table reference
	 */
	public Players() {
		this("PLAYERS", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.PLAYERS</code> table reference
	 */
	public Players(String alias) {
		this(alias, PLAYERS);
	}

	private Players(String alias, Table<PlayersRecord> aliased) {
		this(alias, aliased, null);
	}

	private Players(String alias, Table<PlayersRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<PlayersRecord> getPrimaryKey() {
		return Keys.CONSTRAINT_D;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<PlayersRecord>> getKeys() {
		return Arrays.<UniqueKey<PlayersRecord>>asList(Keys.CONSTRAINT_D, Keys.CONSTRAINT_D6);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Players as(String alias) {
		return new Players(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Players rename(String name) {
		return new Players(name, null);
	}
}
