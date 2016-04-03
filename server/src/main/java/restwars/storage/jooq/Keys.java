/**
 * This class is generated by jOOQ
 */
package restwars.storage.jooq;


import org.jooq.ForeignKey;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;
import restwars.storage.jooq.tables.*;
import restwars.storage.jooq.tables.records.*;

import javax.annotation.Generated;


/**
 * A class modelling foreign key relationships between tables of the <code>PUBLIC</code> 
 * schema
 */
@Generated(
		value = {
				"http://www.jooq.org",
				"jOOQ version:3.7.3"
		},
		comments = "This class is generated by jOOQ"
)
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Keys {

	// -------------------------------------------------------------------------
	// IDENTITY definitions
	// -------------------------------------------------------------------------


	// -------------------------------------------------------------------------
	// UNIQUE and PRIMARY KEY definitions
	// -------------------------------------------------------------------------

	public static final UniqueKey<PlayersRecord> CONSTRAINT_D = UniqueKeys0.CONSTRAINT_D;
	public static final UniqueKey<PlayersRecord> CONSTRAINT_D6 = UniqueKeys0.CONSTRAINT_D6;
	public static final UniqueKey<PlanetsRecord> CONSTRAINT_D5 = UniqueKeys0.CONSTRAINT_D5;
	public static final UniqueKey<BuildingsRecord> CONSTRAINT_5 = UniqueKeys0.CONSTRAINT_5;
	public static final UniqueKey<ConstructionSitesRecord> CONSTRAINT_7 = UniqueKeys0.CONSTRAINT_7;
	public static final UniqueKey<ShipsInConstructionRecord> CONSTRAINT_3 = UniqueKeys0.CONSTRAINT_3;
	public static final UniqueKey<PointsRecord> CONSTRAINT_8 = UniqueKeys0.CONSTRAINT_8;
	public static final UniqueKey<PointsRecord> CONSTRAINT_8C = UniqueKeys0.CONSTRAINT_8C;
	public static final UniqueKey<EventsRecord> CONSTRAINT_7A = UniqueKeys0.CONSTRAINT_7A;

	// -------------------------------------------------------------------------
	// FOREIGN KEY definitions
	// -------------------------------------------------------------------------

	public static final ForeignKey<PlanetsRecord, PlayersRecord> CONSTRAINT_D5B = ForeignKeys0.CONSTRAINT_D5B;
	public static final ForeignKey<BuildingsRecord, PlanetsRecord> CONSTRAINT_52 = ForeignKeys0.CONSTRAINT_52;
	public static final ForeignKey<ConstructionSitesRecord, PlanetsRecord> CONSTRAINT_7E = ForeignKeys0.CONSTRAINT_7E;
	public static final ForeignKey<ShipsInConstructionRecord, PlanetsRecord> CONSTRAINT_31 = ForeignKeys0.CONSTRAINT_31;
	public static final ForeignKey<PointsRecord, PlayersRecord> CONSTRAINT_8CF = ForeignKeys0.CONSTRAINT_8CF;
	public static final ForeignKey<EventsRecord, PlayersRecord> CONSTRAINT_7A9 = ForeignKeys0.CONSTRAINT_7A9;
	public static final ForeignKey<EventsRecord, PlanetsRecord> CONSTRAINT_7A9A = ForeignKeys0.CONSTRAINT_7A9A;

	// -------------------------------------------------------------------------
	// [#1459] distribute members to avoid static initialisers > 64kb
	// -------------------------------------------------------------------------

	private static class UniqueKeys0 extends AbstractKeys {
		public static final UniqueKey<PlayersRecord> CONSTRAINT_D = createUniqueKey(Players.PLAYERS, Players.PLAYERS.ID);
		public static final UniqueKey<PlayersRecord> CONSTRAINT_D6 = createUniqueKey(Players.PLAYERS, Players.PLAYERS.USERNAME);
		public static final UniqueKey<PlanetsRecord> CONSTRAINT_D5 = createUniqueKey(Planets.PLANETS, Planets.PLANETS.ID);
		public static final UniqueKey<BuildingsRecord> CONSTRAINT_5 = createUniqueKey(Buildings.BUILDINGS, Buildings.BUILDINGS.ID);
		public static final UniqueKey<ConstructionSitesRecord> CONSTRAINT_7 = createUniqueKey(ConstructionSites.CONSTRUCTION_SITES, ConstructionSites.CONSTRUCTION_SITES.ID);
		public static final UniqueKey<ShipsInConstructionRecord> CONSTRAINT_3 = createUniqueKey(ShipsInConstruction.SHIPS_IN_CONSTRUCTION, ShipsInConstruction.SHIPS_IN_CONSTRUCTION.ID);
		public static final UniqueKey<PointsRecord> CONSTRAINT_8 = createUniqueKey(Points.POINTS, Points.POINTS.ID);
		public static final UniqueKey<PointsRecord> CONSTRAINT_8C = createUniqueKey(Points.POINTS, Points.POINTS.PLAYER_ID);
		public static final UniqueKey<EventsRecord> CONSTRAINT_7A = createUniqueKey(Events.EVENTS, Events.EVENTS.ID);
	}

	private static class ForeignKeys0 extends AbstractKeys {
		public static final ForeignKey<PlanetsRecord, PlayersRecord> CONSTRAINT_D5B = createForeignKey(restwars.storage.jooq.Keys.CONSTRAINT_D, Planets.PLANETS, Planets.PLANETS.OWNER_ID);
		public static final ForeignKey<BuildingsRecord, PlanetsRecord> CONSTRAINT_52 = createForeignKey(restwars.storage.jooq.Keys.CONSTRAINT_D5, Buildings.BUILDINGS, Buildings.BUILDINGS.PLANET_ID);
		public static final ForeignKey<ConstructionSitesRecord, PlanetsRecord> CONSTRAINT_7E = createForeignKey(restwars.storage.jooq.Keys.CONSTRAINT_D5, ConstructionSites.CONSTRUCTION_SITES, ConstructionSites.CONSTRUCTION_SITES.PLANET_ID);
		public static final ForeignKey<ShipsInConstructionRecord, PlanetsRecord> CONSTRAINT_31 = createForeignKey(restwars.storage.jooq.Keys.CONSTRAINT_D5, ShipsInConstruction.SHIPS_IN_CONSTRUCTION, ShipsInConstruction.SHIPS_IN_CONSTRUCTION.PLANET_ID);
		public static final ForeignKey<PointsRecord, PlayersRecord> CONSTRAINT_8CF = createForeignKey(restwars.storage.jooq.Keys.CONSTRAINT_D, Points.POINTS, Points.POINTS.PLAYER_ID);
		public static final ForeignKey<EventsRecord, PlayersRecord> CONSTRAINT_7A9 = createForeignKey(restwars.storage.jooq.Keys.CONSTRAINT_D, Events.EVENTS, Events.EVENTS.PLAYER_ID);
		public static final ForeignKey<EventsRecord, PlanetsRecord> CONSTRAINT_7A9A = createForeignKey(restwars.storage.jooq.Keys.CONSTRAINT_D5, Events.EVENTS, Events.EVENTS.PLANET_ID);
	}
}
