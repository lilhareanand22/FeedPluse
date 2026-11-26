package android.ai.feedpluse.core.util

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Executes a suspending network call and maps exceptions into user-friendly [Resource.Error] states.
 */
suspend inline fun <T : Any> safeApiCall(crossinline block: suspend () -> T): Resource {
    return try {
        Resource.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (_: IOException) {
        Resource.Error("No internet connection. Please check your network and try again.")
    } catch (e: HttpException) {
        val message = when (e.code()) {
            401 -> "Unauthorized. Please check your API key."
            429 -> "Too many requests. Please try again later."
            in 500..599 -> "Server error (${e.code()}). Please try again later."
            else -> "Request failed with status ${e.code()}."
        }
        Resource.Error(message)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "An unknown error occurred.")
    }
}
