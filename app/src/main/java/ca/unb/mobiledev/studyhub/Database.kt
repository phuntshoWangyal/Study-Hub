package ca.unb.mobiledev.studyhub

import java.sql.Connection
import java.sql.DriverManager

object Database {
    private val url = System.getenv("DB_URL") ?: "jdbc:mariadb://localhost:3306/your_db"
    private val user = System.getenv("DB_USER") ?: "user"
    private val pass = System.getenv("DB_PASS") ?: "pass"

    val connection: Connection by lazy {
        DriverManager.getConnection(url, user, pass)
    }
}
