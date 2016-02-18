import java.sql.{ DriverManager, Statement }

import scala.util.Random

import org.postgresql.ds.{ PGSimpleDataSource }
import org.scalatest.{ Ignore, FreeSpec, MustMatchers }

class MySqlSpec extends FreeSpec with MustMatchers {
  Class.forName("com.mysql.jdbc.Driver")
  val connection = DriverManager.getConnection("jdbc:mysql://mysql/quill_test","root", "")
  "MySql" - {
    "JDBC" - {
      "AUTO_INCREMENT" - {
        val randomName = Random.nextLong().toString
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

      // Fails
      "AUTO_INCREMENT and Timestamp" ignore {
        val randomName = Random.nextLong().toString
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

      // Fails
      "AUTO_INCREMENT and Default text" ignore {
        val randomName = Random.nextString(10)
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
