/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq.tables;


import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.TableImpl;
import restwars.storage.jooq.Keys;
import restwars.storage.jooq.Public;
import restwars.storage.jooq.tables.records.FightShipsRecord;

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
public class FightShips extends TableImpl<FightShipsRecord> {

	private static final long serialVersionUID = 1818766738;

	/**
	 * The reference instance of <code>PUBLIC.FIGHT_SHIPS</code>
	 */
	public static final FightShips FIGHT_SHIPS = new FightShips();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<FightShipsRecord> getRecordType() {
		return FightShipsRecord.class;
	}

	/**
	 * The column <code>PUBLIC.FIGHT_SHIPS.FIGHT_ID</code>.
	 */
	public final TableField<FightShipsRecord, UUID> FIGHT_ID = createField("FIGHT_ID", org.jooq.impl.SQLDataType.UUID.nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FIGHT_SHIPS.SHIP_TYPE</code>.
	 */
	public final TableField<FightShipsRecord, String> SHIP_TYPE = createField("SHIP_TYPE", org.jooq.impl.SQLDataType.VARCHAR.length(50).nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FIGHT_SHIPS.TYPE</code>.
	 */
	public final TableField<FightShipsRecord, String> TYPE = createField("TYPE", org.jooq.impl.SQLDataType.VARCHAR.length(50).nullable(false), this, "");

	/**
	 * The column <code>PUBLIC.FIGHT_SHIPS.AMOUNT</code>.
	 */
	public final TableField<FightShipsRecord, Integer> AMOUNT = createField("AMOUNT", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>PUBLIC.FIGHT_SHIPS</code> table reference
	 */
	public FightShips() {
		this("FIGHT_SHIPS", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.FIGHT_SHIPS</code> table reference
	 */
	public FightShips(String alias) {
		this(alias, FIGHT_SHIPS);
	}

	private FightShips(String alias, Table<FightShipsRecord> aliased) {
		this(alias, aliased, null);
	}

	private FightShips(String alias, Table<FightShipsRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<FightShipsRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<FightShipsRecord, ?>>asList(Keys.CONSTRAINT_B);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FightShips as(String alias) {
		return new FightShips(alias, this);
	}

	/**
	 * Rename this table
	 */
	public FightShips rename(String name) {
		return new FightShips(name, null);
	}
}
