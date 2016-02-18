import java.sql._

import scala.util.Random

import org.postgresql.ds.{ PGSimpleDataSource }
import org.scalatest.{ FreeSpec, MustMatchers }

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
  }
}
