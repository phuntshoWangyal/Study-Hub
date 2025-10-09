package ca.unb.mobiledev.studyhub

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import android.util.Log

interface ConnectionCallback {
    fun onConnected(connection: java.sql.Connection)
}

object DatabaseManager{
    private var connection: Connection? = null

    fun connect(callback: ConnectionCallback): Connection? {
        Thread {
            if (connection == null || connection!!.isClosed) {
                try {
                    val url = "jdbc:mysql://10.0.2.2:3306/studyhub_db"
                    val user = "root"
                    val password = "DatabasePassword"

                    connection = DriverManager.getConnection(url, user, password)
                    Log.e("Database", "Connected to MySQL")
                    callback.onConnected(connection!!)
                } catch (e: SQLException) {
                    Log.e("Database", "Connection failed", e)
                }
            }

        }.start()
        return connection
    }

    fun closeConnection(){
        try {
            connection?.close()
            Log.e("Database","Connection closed successfully")
        } catch (e: SQLException) {
            Log.e("Database", "Connection was not closed")
        }
    }
}