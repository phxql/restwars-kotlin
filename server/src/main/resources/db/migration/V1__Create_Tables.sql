CREATE TABLE players (
  id       UUID PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE planets (
  id       UUID PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES players (id),
  galaxy   INT  NOT NULL,
  system   INT  NOT NULL,
  planet   INT  NOT NULL,
  crystal  INT  NOT NULL,
  gas      INT  NOT NULL,
  energy   INT  NOT NULL
);

CREATE UNIQUE INDEX planets_location ON planets (galaxy, system, planet);

CREATE TABLE buildings (
  id        UUID PRIMARY KEY,
  planet_id UUID        NOT NULL REFERENCES planets (id),
  type      VARCHAR(50) NOT NULL,
  level     INT         NOT NULL
);

CREATE UNIQUE INDEX planets_type_planet ON buildings (type, planet_id);

CREATE TABLE construction_sites (
  id        UUID PRIMARY KEY,
  planet_id UUID        NOT NULL REFERENCES planets (id),
  type      VARCHAR(50) NOT NULL,
  level     INT         NOT NULL,
  done      BIGINT      NOT NULL
);

CREATE UNIQUE INDEX construction_sites_type_planet ON construction_sites (type, planet_id);

CREATE TABLE round (
  round BIGINT NOT NULL
);

CREATE TABLE ships_in_construction (
  id        UUID PRIMARY KEY,
  planet_id UUID        NOT NULL REFERENCES planets (id),
  type      VARCHAR(50) NOT NULL,
  done      BIGINT      NOT NULL
);

CREATE TABLE points (
  id        UUID PRIMARY KEY,
  player_id UUID   NOT NULL REFERENCES players (id),
  points    BIGINT NOT NULL,
  round     BIGINT NOT NULL
);

CREATE TABLE events (
  id        UUID PRIMARY KEY,
  type      VARCHAR(50) NOT NULL,
  round     BIGINT      NOT NULL,
  player_id UUID        NOT NULL REFERENCES players (id),
  planet_id UUID        NOT NULL REFERENCES planets (id)
);

CREATE TABLE hangar (
  id        UUID PRIMARY KEY,
  planet_id UUID NOT NULL UNIQUE REFERENCES planets (id)
);

CREATE TABLE hangar_ships (
  hangar_id UUID        NOT NULL REFERENCES hangar (id),
  type      VARCHAR(50) NOT NULL,
  amount    INT         NOT NULL
);

CREATE UNIQUE INDEX hangar_ships_hangar_type ON hangar_ships (hangar_id, type);

CREATE TABLE flights (
  id                 UUID PRIMARY KEY,
  player_id          UUID        NOT NULL REFERENCES players (id),
  start_galaxy       INT         NOT NULL,
  start_system       INT         NOT NULL,
  start_planet       INT         NOT NULL,
  destination_galaxy INT         NOT NULL,
  destination_system INT         NOT NULL,
  destination_planet INT         NOT NULL,
  started_in_round   BIGINT      NOT NULL,
  direction          VARCHAR(50) NOT NULL,
  type               VARCHAR(50) NOT NULL,
  cargo_crystal      INT         NOT NULL,
  cargo_gas          INT         NOT NULL,
  detected           BOOL        NOT NULL,
  speed              DOUBLE      NOT NULL
);

CREATE TABLE flight_ships (
  flight_id UUID        NOT NULL REFERENCES flights (id),
  type      VARCHAR(50) NOT NULL,
  amount    INT         NOT NULL
);

CREATE UNIQUE INDEX flight_ships_flight_type ON flight_ships (flight_id, type);

CREATE TABLE fights (
  id           UUID PRIMARY KEY,
  attacker_id  UUID   NOT NULL REFERENCES players (id),
  defender_id  UUID   NOT NULL REFERENCES players (id),
  planet_id    UUID   NOT NULL REFERENCES planets (id),
  round        BIGINT NOT NULL,
  loot_crystal INT    NOT NULL,
  loot_gas     INT    NOT NULL
);

CREATE TABLE fight_ships (
  fight_id  UUID        NOT NULL REFERENCES fights (id),
  ship_type VARCHAR(50) NOT NULL,
  type      VARCHAR(50) NOT NULL,
  amount    INT         NOT NULL
);

CREATE UNIQUE INDEX fight_ships_fight_type ON fight_ships (fight_id, ship_type, type);

CREATE TABLE detected_flights (
  id                       UUID PRIMARY KEY,
  flight_id                UUID UNIQUE NOT NULL REFERENCES flights (id),
  player_id                UUID        NOT NULL REFERENCES players (id),
  detected_in_round        BIGINT      NOT NULL,
  approximated_flight_size BIGINT      NOT NULL
);