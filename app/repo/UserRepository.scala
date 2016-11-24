package repo

import javax.inject.{Inject, Singleton}
import models.User
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import scala.concurrent.Future


/**
  * Created by chlr on 11/23/16.
  */


@Singleton()
class UserRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends
  UserRepository.UserRepoHelper with HasDatabaseConfigProvider[JdbcProfile] {

  private val logger = Logger(this.getClass)

  import driver.api._

  def insert(user: User): Future[Long] = db.run {
    logger.debug(s"inserting Job: ${user.toString}")
    userTableQueryInc += user
  }

  def update(user: User): Future[Int] = db.run {
    logger.debug(s"updating user ${user.toString}")
    userTableQuery.filter(_.id === user.id).update(user)
  }

  def delete(id: Int): Future[Int] = db.run {
    logger.debug(s"deleting user_id $id")
    userTableQuery.filter(_.id === id).delete
  }

  def getAll: Future[List[User]] = db.run {
    logger.debug(s"retrieving all users")
    userTableQuery.to[List].result
  }

  def getById(jobId: Int): Future[Option[User]] = db.run {
    userTableQuery.filter(_.id === jobId).result.headOption
  }

  def ddl = userTableQuery.schema

}

object UserRepository {

  trait UserRepoHelper {

    self: HasDatabaseConfigProvider[JdbcProfile]  =>

    import driver.api._

    lazy protected val userTableQuery: TableQuery[UserTable] = TableQuery[UserTable]
    lazy protected val userTableQueryInc = userTableQuery returning userTableQuery.map(_.id)

    protected class UserTable(tag: Tag) extends Table[User](tag, "user") {

      val id = column[Long]("id", O.AutoInc, O.PrimaryKey)
      val email = column[String]("email", O.SqlType("VARCHAR(100)"))
      val nickName = column[String]("nickname", O.SqlType("VARCHAR(40)"))
      val firstName = column[String]("first_name", O.SqlType("VARCHAR(40)"))
      val lastName = column[String]("last_name", O.SqlType("VARCHAR(40)"))
      val activated = column[Boolean]("activated")
      val roleId = column[Byte]("role_id")


      def emailUnique = index("email_unique_key", email, unique = true)

      def * = (id.?, email, nickName, firstName, lastName, activated, roleId) <> (User.tupled, User.unapply)
    }
  }



}