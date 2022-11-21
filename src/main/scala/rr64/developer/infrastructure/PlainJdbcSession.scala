package rr64.developer.infrastructure

import akka.japi.function
import akka.projection.jdbc.JdbcSession

import java.sql.{Connection, DriverManager}

class PlainJdbcSession(
                        driverClass: String,
                        url: String,
                        user: String,
                        password: String
) extends JdbcSession {

  lazy val conn: Connection = {
    Class.forName(driverClass)
    val c = DriverManager.getConnection(url, user, password)
    c.setAutoCommit(false)
    c
  }

  override def withConnection[Result](func: function.Function[Connection, Result]): Result =
    func(conn)
  override def commit(): Unit = conn.commit()
  override def rollback(): Unit = conn.rollback()
  override def close(): Unit = conn.close()

}
