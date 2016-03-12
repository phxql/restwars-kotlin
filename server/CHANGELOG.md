# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added
- Started writing a changelog.
- Implemented endpoint which blocks until the next round starts.
- Implemented player points.
- Added readme for server.
- Implemented tournament mode.
- Added game metadata endpoint.
- Added `since` parameter to fights endpoint.
- Added HTTP response to `/`.

### Changed
- Command center now produces 5 energy per round.
- Server now returns `422` instead of `500` when the client sent invalid JSON.
- The configuration endpoint now includes the universe size.
- The fight responses now contain the loot.
- Ships with amount 0 are now ommited from responses.
- Added building types to documentation.

### Fixed
- Players now need a shipyard to build ships.
- Shipyard level now influences ship build time.
- Players now need a telescope to scan.
- Fixed a bug where a fight produced a negative amounts of ships.
- Fixed the HTTP method in the documentation of the telescope scan endpoint.

## [0.1.0] - 2016-03-05
- First version