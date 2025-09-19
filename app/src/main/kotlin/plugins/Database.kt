package plugins

import java.sql.Connection
import java.sql.DriverManager

object Database
{
    private val jdbcUrl = System.getenv("DB_JDBC_URL") ?: ""
    private val user = System.getenv("DB_USER") ?: ""
    private val password = System.getenv("DB_PASSWORD") ?: ""

    fun getConnection(): Connection = DriverManager.getConnection(jdbcUrl, user, password)
}
