import kotlinx.coroutines.runBlocking
import network.Network
import kotlin.jvm.Throws

fun main() = runBlocking {

    runCatching { throwableFunction() }.getOrNull()

    val randoms: List<Int> = runCatching {
        Network.service.getRandom(
            fromNumber = 1,
            toNumber = 10,
            numbersOfResults = 3
        )
    }.mapCatching { res -> res.map { it + 100 } }
        .getOrElse { emptyList() }
    println("Randoms: $randoms")

    val nullResult: List<Int>? = runCatching { Network.service.brokenAPI() }
        .onFailure { println("Failed: $it") }
        .onSuccess { println("Success: $it") }
        .getOrNull()
    println("Result: $nullResult")

//    val failedResult = Network.service.brokenAPIResult()
//    println("Result: $failedResult")

}

@Throws(Exception::class)
fun throwableFunction(): Int {
    throw Exception()
}