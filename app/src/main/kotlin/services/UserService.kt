package services

import models.User
import repositories.UserRepository
import java.util.UUID

class UserService(private val repo: UserRepository = UserRepository())
{
    fun listUsers(): List<User> = repo.getAll()

    fun getUser(uuid: UUID): User? = repo.getById(uuid)

    fun createUser(fullname: String, studyLevel: String, age: Int): User {
        val user = User(UUID.randomUUID(), fullname, studyLevel, age)
        repo.create(user)
        return user
    }

    fun updateUser(user: User) = repo.update(user)

    fun deleteUser(uuid: UUID) = repo.delete(uuid)
}
