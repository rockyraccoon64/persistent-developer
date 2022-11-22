package rr64.developer.infrastructure

import akka.japi.function
import akka.projection.jdbc.JdbcSession

import java.sql.{Connection, DriverManager}

/**
 * Простая JDBC-сессия для проекций
 * @param driverClass Класс JDBC-драйвера
 * @param url URL базы данных
 * @param user Имя пользователя
 * @param password Пароль
 * */
class PlainJdbcSession(
  driverClass: String,
  url: String,
  user: String,
  password: String
) extends JdbcSession {

  private lazy val conn: Connection = {
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
