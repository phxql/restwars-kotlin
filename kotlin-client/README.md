# RESTwars Kotlin Client

This is a client written in Kotlin for RESTwars.

## Usage

### Maven
```xml
<repositories>
    <repository>
        <id>bintray-phxql-maven</id>
        <name>bintray</name>
        <url>https://dl.bintray.com/phxql/maven</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.mkammerer.restwars</groupId>
        <artifactId>kotlin-client</artifactId>
        <version>0.0.1</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    // ... your other repositories ...

    maven {
        url  "https://dl.bintray.com/phxql/maven"
    }
}

dependencies {
    compile 'de.mkammerer.restwars:kotlin-client:0.0.1'
}
```

### Code

```kotlin
val client = RestWarsClient("localhost", 7777)

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