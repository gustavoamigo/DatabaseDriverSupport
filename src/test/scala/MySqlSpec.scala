import java.sql.{ DriverManager, Statement }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

import org.postgresql.ds.{ PGSimpleDataSource }
import org.scalatest.{ Ignore, FreeSpec, MustMatchers }

import com.github.mauricio.async.db.{ Configuration, Connection }
import com.github.mauricio.async.db.mysql.{ MySQLQueryResult, MySQLConnection }

class MySqlSpec extends FreeSpec with MustMatchers {
  Class.forName("com.mysql.jdbc.Driver")
  "MySql" - {
    "JDBC" - {
      val connection = DriverManager.getConnection("jdbc:mysql://mysql/quill_test","root", "")
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
    "MySql-async" - {
      "AUTO_INCREMENT" - {
        val configuration = new Configuration("root", "mysql", 3306, None, Some("quill_test"))
        val connection: Connection = new MySQLConnection(configuration)
        Await.result(connection.connect, 5 seconds)
        val randomName = Random.nextLong().toString
        val result1 =  Await.result(connection.sendPreparedStatement("INSERT INTO test1 (name) VALUES(?)", List(randomName)), 5 seconds)
        result1.rows mustBe None
        val result2 = Await.result(connection.sendQuery("SELECT last_insert_id() FROM test1"), 5 seconds)
        val generatedId = result2.rows.get(0)(0).asInstanceOf[Long]
        val result3 =  Await.result(connection.sendPreparedStatement("SELECT * FROM test1 WHERE id=?", List(generatedId)), 5 seconds)
        result3.rows.get(0)("name") mustBe  randomName
      }
      "AUTO_INCREMENT - Hack" - {
        val configuration = new Configuration("root", "mysql", 3306, None, Some("quill_test"))
        val connection: Connection = new MySQLConnection(configuration)
        Await.result(connection.connect, 5 seconds)
        val randomName = Random.nextLong().toString
        val result1 =  Await.result(connection.sendPreparedStatement("INSERT INTO test1 (name) VALUES(?)", List(randomName)), 5 seconds)
        val mySQLQueryResult = result1.asInstanceOf[MySQLQueryResult]
        val generatedId = mySQLQueryResult.lastInsertId
        val result3 =  Await.result(connection.sendPreparedStatement("SELECT * FROM test1 WHERE id=?", List(generatedId)), 5 seconds)
        result3.rows.get(0)("name") mustBe  randomName
      }
    }
  }
}
