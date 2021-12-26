package network

import okhttp3.Request
import okhttp3.ResponseBody
import okio.IOException
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class ResultAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (Call::class.java != getRawType(returnType)) return null

        check(returnType is ParameterizedType) {
            "return type must be parameterized as Call<Result<<Foo>> or Call<Result<out Foo>>"
        }

        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != Result::class.java) return null

        check(responseType is ParameterizedType) { "Response must be parameterized as Result<Foo> or Result<out Foo>" }

        val successBodyType = getParameterUpperBound(0, responseType)

        val errorBodyConverter =
            retrofit.nextResponseBodyConverter<Throwable>(null, Throwable::class.java, annotations)

        return ResultAdapter<Any, Throwable>(successBodyType, errorBodyConverter)
    }
}

class ResultAdapter<R : Any, T: Throwable>(
    private val successType: Type,
    private val errorBodyConverter: Converter<ResponseBody, T>
) : CallAdapter<R, Call<Result<R>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<R>): Call<Result<R>> {
        return NetworkResponseCall(call, errorBodyConverter)
    }

    internal class NetworkResponseCall<R : Any, T: Throwable>(
        private val delegate: Call<R>,
        private val errorConverter: Converter<ResponseBody, T>
    ) : Call<Result<R>> {

        override fun enqueue(callback: Callback<Result<R>>) {
            return delegate.enqueue(object : Callback<R> {
                override fun onResponse(call: Call<R>, response: Response<R>) {
                    val body = response.body()
                    val code = response.code()
                    val error = response.errorBody()

                    if (response.isSuccessful) {
                        if (body != null) {
                            callback.onResponse(
                                this@NetworkResponseCall,
                                Response.success(Result.success(body))
                            )
                        } else {
                            callback.onResponse(
                                this@NetworkResponseCall,
                                Response.success(Result.failure(KotlinNullPointerException()))
                            )
                        }
                    } else {
                        val errorBody = when {
                            error == null -> null
                            error.contentLength() == 0L -> null
                            else -> try {
                                errorConverter.convert(error)
                            } catch (ex: Exception) {
                                null
                            }
                        }
                        if (errorBody != null) {
                            callback.onResponse(
                                this@NetworkResponseCall,
                                Response.success(Result.failure(errorBody))
                            )
                        } else {
                            callback.onResponse(
                                this@NetworkResponseCall,
                                Response.success(Result.failure(KotlinNullPointerException()))
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<R>, throwable: Throwable) {
                    val networkResponse: Result<R> = when (throwable) {
                        is IOException -> Result.failure(throwable)
                        else -> Result.failure(throwable)
                    }
                    callback.onResponse(this@NetworkResponseCall, Response.success(networkResponse))
                }
            })
        }

        override fun isExecuted() = delegate.isExecuted

        override fun clone() = NetworkResponseCall(delegate.clone(), errorConverter)

        override fun isCanceled() = delegate.isCanceled

        override fun cancel() = delegate.cancel()

        override fun execute(): Response<Result<R>> {
            throw UnsupportedOperationException("NetworkResponseCall doesn't support execute")
        }

        override fun request(): Request = delegate.request()

        override fun timeout(): Timeout {
            return Timeout.NONE
        }
    }
}