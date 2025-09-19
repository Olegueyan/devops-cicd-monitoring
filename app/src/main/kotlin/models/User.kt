package models

import java.util.UUID
import kotlinx.serialization.Serializable
import serializer.UUIDSerializer

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val fullname: String,
    val studyLevel: String,
    val age: Int
)
