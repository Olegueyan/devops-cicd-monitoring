package requests

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(
    val fullname: String? = null,
    val studyLevel: String? = null,
    val age: Int? = null
)
