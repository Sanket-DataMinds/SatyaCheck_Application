package com.satyacheck.android.domain.model

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Result<T>(data)
    class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
    class Loading<T>(data: T? = null) : Result<T>(data)
    
    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$message]"
            is Loading<*> -> "Loading"
        }
    }
}
