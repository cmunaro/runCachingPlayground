# runCachingPlayground

In this repository I have tried some tricks to handle exceptions in a more kotlin way.

I have made http calls with Retrofit2 in different ways
First wrapping the calls inside a runCaching block and then creating a custom `CallAdapter` to make Retrofit2 return a kotlin.Result

With the second approach **the application can't crash from non handled exceptions**

## TIL
With my custom [CallAdapter](src/main/kotlin/network/ResultAdapter.kt) I can make a totally secure HTTP call like:
```
val successResult: List<Int> = Network.service.getRandomResult(
        fromNumber = 1,
        toNumber = 10,
        numbersOfResults = 3
    )
        .onFailure { println("Fail") }
        .onSuccess { println("Success") }
        .getOrNull()
```

