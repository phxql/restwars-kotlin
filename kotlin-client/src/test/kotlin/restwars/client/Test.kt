package restwars.client

fun main(args: Array<String>) {
    val client = RestWarsClient("http://localhost:7777")
    val applicationInformation = client.applicationInformation()
    println(applicationInformation)

    val restwarsConfiguration = client.restwarsConfiguration()
    println(restwarsConfiguration)

    val roundInformation = client.roundInformation()
    println(roundInformation)

    // client.createPlayer("moe", "moe")
    val client2 = client.withCredentials("moe", "moe")

    val fights = client2.listFights()
    println(fights)

    val flights = client2.listFlights()
    println(flights)

    for (planet in client2.listPlanets().planets) {
        println(planet)

        val buildings = client2.listBuildings(planet.location.toString())
        println(buildings)

        val constructionSites = client2.listConstructionSites(planet.location.toString())
        println(constructionSites)

        val shipsInHangar = client2.listShipsInHangar(planet.location.toString())
        println(shipsInHangar)

        val shipsInConstruction = client2.listShipsInConstruction(planet.location.toString())
        println(shipsInConstruction)
    }
}