# RESTwars

* [Server Changelog](server/CHANGELOG.md)

## Running

1. `./gradlew clean run`
1. Open browser at [http://localhost:7777/v1/restwars](http://localhost:7777/v1/restwars)

## Building the documentation

1. Install `asciidoc`
1. `cd doc/`
1. `asciidoc Webservice.adoc && asciidoc RESTwars.adoc`
1. Open `RESTwars.html` and `Webservice.html` in browser

## Building a distribution

1. Run `./gradlew clean build distTar`
1. Check `build/distributions/`

## Run with docker

1. `docker run -itp 7777:7777 docker.io/phxql/restwars`
1. Open browser at [http://localhost:7777/v1/restwars](http://localhost:7777/v1/restwars)