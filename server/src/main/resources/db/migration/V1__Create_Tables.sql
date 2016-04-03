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
  player_id UUID   NOT NULL UNIQUE REFERENCES players (id),
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