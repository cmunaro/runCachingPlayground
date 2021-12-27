import kotlinx.coroutines.runBlocking
import network.Network
import retrofit2.HttpException
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
    println("getRandom response: $randoms")

    val nullResult: List<Int>? = runCatching { Network.service.brokenAPI() }
        .onFailure { println("brokenAPI Failed: $it") }
        .onSuccess { println("brokenAPI Success: $it") }
        .getOrNull()
    println("brokenAPI response: $nullResult")

    val failedResult = Network.service.brokenAPIResult()
        .onFailure { if(it is HttpException) println("Exception: $it") }
        .getOrNull()
    println("BrokenAPI with Result response: $failedResult")

    val successResult = Network.service.getRandomResult(
        fromNumber = 1,
        toNumber = 10,
        numbersOfResults = 3
    )
        .onFailure { println("Fail") }
        .onSuccess { println("Success") }
        .getOrNull()
    println("getRandom with Result response: $successResult")
}

@Throws(Exception::class)
fun throwableFunction(): Int {
    throw Exception()
}