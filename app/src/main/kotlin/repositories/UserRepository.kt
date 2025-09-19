package repositories

import models.User
import plugins.Database
import java.sql.ResultSet
import java.util.UUID

class UserRepository {

    private fun ResultSet.toUser() = User(
        uuid = UUID.fromString(getString("uuid")),
        fullname = getString("fullname"),
        studyLevel = getString("study_level"),
        age = getInt("age")
    )

    fun getAll(): List<User> = Database.getConnection().use { conn ->
        conn.prepareStatement("SELECT * FROM users").use { stmt ->
            stmt.executeQuery().use { rs ->
                generateSequence { if (rs.next()) rs.toUser() else null }.toList()
            }
        }
    }

    fun getById(uuid: UUID): User? = Database.getConnection().use { conn ->
        conn.prepareStatement("SELECT * FROM users WHERE uuid = ?").use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.toUser() else null
            }
        }
    }

    fun create(user: User) = Database.getConnection().use { conn ->
        conn.prepareStatement(
            "INSERT INTO users (uuid, fullname, study_level, age) VALUES (?, ?, ?, ?)"
        ).use { stmt ->
            stmt.setString(1, user.uuid.toString())
            stmt.setString(2, user.fullname)
            stmt.setString(3, user.studyLevel)
            stmt.setInt(4, user.age)
            stmt.executeUpdate()
        }
    }

    fun update(user: User) = Database.getConnection().use { conn ->
        conn.prepareStatement(
            "UPDATE users SET fullname=?, study_level=?, age=? WHERE uuid=?"
        ).use { stmt ->
            stmt.setString(1, user.fullname)
            stmt.setString(2, user.studyLevel)
            stmt.setInt(3, user.age)
            stmt.setString(4, user.uuid.toString())
            stmt.executeUpdate()
        }
    }

    fun delete(uuid: UUID) = Database.getConnection().use { conn ->
        conn.prepareStatement("DELETE FROM users WHERE uuid=?").use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeUpdate()
        }
    }
}
