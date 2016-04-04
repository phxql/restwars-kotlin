/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq;


import org.jooq.Table;
import org.jooq.impl.SchemaImpl;
import restwars.storage.jooq.tables.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
public class Public extends SchemaImpl {

	private static final long serialVersionUID = 1268509244;

	/**
	 * The reference instance of <code>PUBLIC</code>
	 */
	public static final Public PUBLIC = new Public();

	/**
	 * No further instances allowed
	 */
	private Public() {
		super("PUBLIC");
	}

	@Override
	public final List<Table<?>> getTables() {
		List result = new ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final List<Table<?>> getTables0() {
		return Arrays.<Table<?>>asList(
				Players.PLAYERS,
				Planets.PLANETS,
				Buildings.BUILDINGS,
				ConstructionSites.CONSTRUCTION_SITES,
				Round.ROUND,
				ShipsInConstruction.SHIPS_IN_CONSTRUCTION,
				Points.POINTS,
				Events.EVENTS,
				Hangar.HANGAR,
				HangarShips.HANGAR_SHIPS,
				Flights.FLIGHTS,
				FlightShips.FLIGHT_SHIPS,
				Fights.FIGHTS,
				FightShips.FIGHT_SHIPS,
				DetectedFlights.DETECTED_FLIGHTS);
	}
}
