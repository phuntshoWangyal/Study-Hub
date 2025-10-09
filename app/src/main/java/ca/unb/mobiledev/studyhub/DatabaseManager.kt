package ca.unb.mobiledev.studyhub

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import android.util.Log

object DatabaseManager{
    private var connection: Connection ?= null

    fun connect(): Connection? {
        if (connection == null || connection!!.isClosed) {
            try {
                val url = "jdbc:mariadb://10.0.2.2:3306/studyhub_db"
                val user = "root"
                val password = "DatabasePassword"
                connection = DriverManager.getConnection(url, user, password)
                Log.e("Database","Connected to MariaDB")
            } catch (e: SQLException) {

                Log.e("Database", "Connection failed")
            }
        }
        return connection
    }

    fun closeConnection(){
        try {
            connection?.close()
            Log.e("Database","Connection closed succesfully")
        } catch (e: SQLException) {
            Log.e("Database", "Connection was not closed")
        }
    }

}