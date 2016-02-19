import java.sql._
import org.joda.time.LocalDateTime

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

import org.postgresql.ds.{ PGSimpleDataSource }
import org.scalatest.{ FreeSpec, MustMatchers }

import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.{ Connection, Configuration }

class PostgresSpec extends FreeSpec with MustMatchers {
  "Postgres" - {
    "JDBC" - {
      "Bigserial" - {
        val randomName = Random.nextString(10)
        val source = new PGSimpleDataSource()
        source.setServerName("postgres")
        source.setDatabaseName("quill_test")
        source.setUser("postgres")
        source.setPassword("")
        val connection = source.getConnection()
        val statement = connection.prepareStatement("INSERT INTO test1 (name) VALUES(?)", Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, randomName)
        val affectedRows = statement.executeUpdate
        affectedRows mustBe 1
        val generatedKeys = statement.getGeneratedKeys
        generatedKeys.next() mustBe true
        val generatedId = generatedKeys.getLong(1)
        val query = connection.prepareStatement("SELECT * FROM test1 WHERE id=?");
        query.setLong(1, generatedId)
        val result = query.executeQuery
        result.next()  mustBe true
        val name = result.getString("name")
        name mustBe randomName
      }

      "Bigserial and Timestamp" - {
        val randomName = Random.nextString(10)
        val source = new PGSimpleDataSource()
        source.setServerName("postgres")
        source.setDatabaseName("quill_test")
        source.setUser("postgres")
        source.setPassword("")
        val connection = source.getConnection()
        val columnNames = List("id", "createdat").toArray
        val statement = connection.prepareStatement("INSERT INTO test1 (name) VALUES(?)", columnNames)
        statement.setString(1, randomName)
        val affectedRows = statement.executeUpdate
        affectedRows mustBe 1
        val generatedKeys = statement.getGeneratedKeys
        generatedKeys.next() mustBe true
        val (generatedId, createdAt) = (generatedKeys.getLong(1), generatedKeys.getTimestamp(2))
        val query = connection.prepareStatement("SELECT * FROM test1 WHERE id=?");
        query.setLong(1, generatedId)
        val result = query.executeQuery
        result.next() mustBe true
        val name = result.getString("name")
        name mustBe randomName
        val createdAtFromQuery = result.getTimestamp("createdat")
        createdAtFromQuery mustBe createdAt
      }

      "Sequence and Default text" - {
        val randomName = Random.nextString(10)
        val source = new PGSimpleDataSource()
        source.setServerName("postgres")
        source.setDatabaseName("quill_test")
        source.setUser("postgres")
        source.setPassword("")
        val connection = source.getConnection()
        val columnNames = List("id", "othername").toArray
        val statement = connection.prepareStatement("INSERT INTO test2 (name) VALUES(?)", columnNames)
        statement.setString(1, randomName)
        val affectedRows = statement.executeUpdate
        affectedRows mustBe 1
        val generatedKeys = statement.getGeneratedKeys
        generatedKeys.next() mustBe true
        val (generatedId, otherName) = (generatedKeys.getLong(1), generatedKeys.getString(2))
        val query = connection.prepareStatement("SELECT * FROM test2 WHERE id=?");
        query.setLong(1, generatedId)
        val result = query.executeQuery
        result.next() mustBe true
        val name = result.getString("name")
        name mustBe randomName
        val otherNameFromQuery = result.getString("othername")
        otherNameFromQuery mustBe otherName
      }
    }

    "Postgres-async" - {
      "Bigserial" - {
        val configuration = new Configuration("postgres", "postgres", 5432, None, Some("quill_test"))
        val connection: Connection = new PostgreSQLConnection(configuration)
        Await.result(connection.connect, 5 seconds)
        val randomName = Random.nextLong().toString
        val result1 =  Await.result(connection.sendPreparedStatement("INSERT INTO test1 (name) VALUES(?) RETURNING ID", List(randomName)), 5 seconds)
        result1.rows mustNot be(None)
        val generatedId = result1.rows.get(0)("id").asInstanceOf[Long]
        val result2 =  Await.result(connection.sendPreparedStatement("SELECT * FROM test1 WHERE id=?", List(generatedId)), 5 seconds)
        result2.rows.get(0)("name") mustBe  randomName
      }

      "Bigserial and Timestamp" - {
        val configuration = new Configuration("postgres", "postgres", 5432, None, Some("quill_test"))
        val connection: Connection = new PostgreSQLConnection(configuration)
        Await.result(connection.connect, 5 seconds)
        val randomName = Random.nextLong().toString
        val result1 =  Await.result(connection.sendPreparedStatement("INSERT INTO test1 (name) VALUES(?) RETURNING ID, createdat", List(randomName)), 5 seconds)
        result1.rows mustNot be(None)
        val generatedId = result1.rows.get(0)("id").asInstanceOf[Long]
        val createdAt = result1.rows.get(0)("createdat").asInstanceOf[LocalDateTime]

        val result2 =  Await.result(connection.sendPreparedStatement("SELECT * FROM test1 WHERE id=?", List(generatedId)), 5 seconds)
        result2.rows.get(0)("name") mustBe  randomName
        result2.rows.get(0)("createdat") mustBe  createdAt
      }

      "Sequence and Default text" - {
        val configuration = new Configuration("postgres", "postgres", 5432, None, Some("quill_test"))
        val connection: Connection = new PostgreSQLConnection(configuration)
        Await.result(connection.connect, 5 seconds)
        val randomName = Random.nextLong().toString
        val result1 =  Await.result(connection.sendPreparedStatement("INSERT INTO test2 (name) VALUES(?) RETURNING ID, othername", List(randomName)), 5 seconds)
        result1.rows mustNot be(None)
        val generatedId = result1.rows.get(0)("id").asInstanceOf[Int]
        val othername = result1.rows.get(0)("othername").asInstanceOf[String]

        val result2 =  Await.result(connection.sendPreparedStatement("SELECT * FROM test2 WHERE id=?", List(generatedId)), 5 seconds)
        result2.rows.get(0)("name") mustBe randomName
        result2.rows.get(0)("othername") mustBe othername

      }
    }
  }


}
