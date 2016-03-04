# RESTwars Kotlin Client

This is a client written in Kotlin for RESTwars.

## Usage

```kotlin
val client = RestWarsClient("http://localhost:7777")

// Retrieve application information
val applicationInformation = client.applicationInformation()
println("RESTwars version ${applicationInformation.version} running")

// Create player
client.createPlayer("player1", "secret")

// With an AuthenticatingRestWarsClient it's possible to call the endpoints which needs authentication
val authedClient = client.withCredentials("player1", "secret")

// List planets
val planets = authedClient.listPlanets()
println(planets)
```