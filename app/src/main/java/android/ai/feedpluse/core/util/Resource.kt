package android.ai.feedpluse.core.util

/**
 * Wrapper representing the state of an asynchronous operation.
 */
sealed class Resource {
    data object Loading : Resource()
    data class Success(val data: Any) : Resource()
    data class Error(val message: String, val data: Any? = null) : Resource()
}
