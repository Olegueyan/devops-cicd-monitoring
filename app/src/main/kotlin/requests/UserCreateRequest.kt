package requests

import kotlinx.serialization.Serializable

@Serializable
data class UserCreateRequest(
    val fullname: String,
    val studyLevel: String,
    val age: Int
)
