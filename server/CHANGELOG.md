# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

### Added
- Implemented COLONIZE_FAILED event.
- Added docker images.
- Added level parameter to building metadata endpoint.
- Added CORS.
- Added balancing configuration.

### Changed
- Implemented a storage backend based on a H2 database.
- Ships in flights are now calculated into the points.
- \#53 Fixed a typo in ship metadata response: `attackPoint` -> `attackPoints`
- \#59 Lowered the resource generation rate for all buildings by 50%

### Fixed
- \#51 Fixed a bug where detected flights could not be retrieved
- \#54 Fixed a bug where the server stuck in an endless loop when the universe is full and a new player is created.
- \#61 Fixed a bug where concurrent requests were not handled correctly.
- Fixed a bug where the server generated a 500 if a malformed `Authorization` header was sent.

## [0.2.0] - 2016-04-03
### Added
- Started writing a changelog.
- Implemented endpoint which blocks until the next round starts.
- Implemented player points.
- Added readme for server.
- Implemented tournament mode.
- Added game metadata endpoint.
- Added `since` parameter to fights endpoint.
- Added HTTP response to `/`.
- Added flight detection.
- Added events.
- Added cargo and direction to flight responses.
- Added endpoints to fetch request metrics.
- Added machine readable reasons to failed requests.
- Added building types to documentation.

### Changed
- Command center now produces 5 energy per round.
- Server now returns `422` instead of `500` when the client sent invalid JSON.
- The configuration endpoint now includes the universe size.
- The fight responses now contain the loot.
- Ships with amount 0 are now ommited from responses.
- Changed distance calculation.

### Fixed
- Players now need a shipyard to build ships.
- Shipyard level now influences ship build time.
- Players now need a telescope to scan.
- Fixed a bug where a fight produced a negative amounts of ships.
- Fixed the HTTP method in the documentation of the telescope scan endpoint.

## [0.1.0] - 2016-03-05
- First version