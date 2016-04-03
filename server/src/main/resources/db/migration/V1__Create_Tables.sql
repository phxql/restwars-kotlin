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
