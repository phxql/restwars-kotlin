package restwars.client

fun main(args: Array<String>) {
    val client = RestWarsClient("http://localhost:7777")
    // client.createPlayer("moe", "moe")
    val client2 = client.withCredentials("moe", "moe")
    println(client2.listPlanets())
}