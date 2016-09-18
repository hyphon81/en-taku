package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import models.services.LoginInfoService
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.dbio.DBIOAction
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import utils.UUIDHelper
import slick.driver.MySQLDriver.api._

import play.api.Logger

/**
 * Give access to the user object.
 */
class UserDAOImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider,
  loginInfoService: LoginInfoService
) extends UserDAO with DAOSlick {

  import driver.api._

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = {
    val userQuery = for {
      dbLoginInfo <- loginInfoQuery(loginInfo)
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.loginInfoId === dbLoginInfo.id)
      dbUser <- slickUsers.filter(_.id === dbUserLoginInfo.accountId)
    } yield dbUser
    db.run(userQuery.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        val loginInfos = loginInfoService.retrieve(UUIDHelper.fromByteArray(user.accountId)).map(_.getOrElse(throw new Exception(s"LoginInfos is empty")))
        User(
          UUIDHelper.fromByteArray(user.accountId),
          Await.result(loginInfos, Duration.Inf),
          user.accountName,
          user.email,
          user.userName,
          user.avatarURL,
          user.birthDay,
          user.openBirthDay,
          user.comment,
          user.residence,
          user.openResidence,
          user.openHistory,
          user.activated,
          user.isAdmin,
          user.changeableAccountName
        )
      }
    }
  }

  /**
   * Finds a user by its account ID.
   *
   * @param accountId The ID of the account to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(accountId: UUID) = {
    val query = for {
      dbUser <- slickUsers.filter(_.id === UUIDHelper.toByteArray(accountId))
    } yield dbUser

    db.run(query.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        val loginInfos = loginInfoService.retrieve(UUIDHelper.fromByteArray(user.accountId)).map(_.getOrElse(throw new Exception(s"LoginInfos is empty")))
        User(
          UUIDHelper.fromByteArray(user.accountId),
          Await.result(loginInfos, Duration.Inf),
          user.accountName,
          user.email,
          user.userName,
          user.avatarURL,
          user.birthDay,
          user.openBirthDay,
          user.comment,
          user.residence,
          user.openResidence,
          user.openHistory,
          user.activated,
          user.isAdmin,
          user.changeableAccountName
        )
      }
    }
  }

  /**
   * Finds a user by its account name.
   *
   * @param accountName The name of the account to find.
   * @return The found user or None if no user for the given name could be found.
   */
  def findByAccountName(accountName: String) = {
    val query = for {
      dbUser <- slickUsers.filter(_.accountName === accountName)
    } yield dbUser

    db.run(query.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        val loginInfos = loginInfoService.retrieve(UUIDHelper.fromByteArray(user.accountId)).map(_.getOrElse(throw new Exception(s"LoginInfos is empty")))
        User(
          UUIDHelper.fromByteArray(user.accountId),
          Await.result(loginInfos, Duration.Inf),
          user.accountName,
          user.email,
          user.userName,
          user.avatarURL,
          user.birthDay,
          user.openBirthDay,
          user.comment,
          user.residence,
          user.openResidence,
          user.openHistory,
          user.activated,
          user.isAdmin,
          user.changeableAccountName
        )
      }
    }
  }

  /**
   * Finds a user by its email.
   *
   * @param email The email address to find.
   * @return The found user or None if no user for the given email address could be found.
   */
  def findByEmail(email: String) = {
    val query = for {
      dbUser <- slickUsers.filter(_.email === email)
    } yield dbUser

    db.run(query.result.headOption).map { dbUserOption =>
      dbUserOption.map { user =>
        val loginInfos = loginInfoService.retrieve(UUIDHelper.fromByteArray(user.accountId)).map(_.getOrElse(throw new Exception(s"LoginInfos is empty")))
        User(
          UUIDHelper.fromByteArray(user.accountId),
          Await.result(loginInfos, Duration.Inf),
          user.accountName,
          user.email,
          user.userName,
          user.avatarURL,
          user.birthDay,
          user.openBirthDay,
          user.comment,
          user.residence,
          user.openResidence,
          user.openHistory,
          user.activated,
          user.isAdmin,
          user.changeableAccountName
        )
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    val dbUser = DBUser(
      UUIDHelper.toByteArray(user.accountId),
      user.accountName,
      user.email,
      user.userName,
      user.avatarURL,
      user.birthDay,
      user.openBirthDay,
      user.comment,
      user.residence,
      user.openResidence,
      user.openHistory,
      user.activated,
      user.isAdmin,
      user.changeableAccountName
    )

    // We don't have the LoginInfo id so we try to get it first.
    // If there is no LoginInfo yet for this user we retrieve the id on insertion.    
    val loginInfoActions = {
      def retrieveLoginInfo(id: String, key: String) =
        slickLoginInfos.filter(info =>
          info.providerID === id && info.providerKey === key
        ).result.headOption

      def insertLoginInfo(dbLoginInfo: DBLoginInfo) = {
        slickLoginInfos.returning(slickLoginInfos.map(_.id)).
          into((info, id) => info.copy(id = Some(id))) += dbLoginInfo
      }

      for {
        loginInfo <- user.loginInfos
      } yield for {
        loginInfoOption <- retrieveLoginInfo(loginInfo.providerID, loginInfo.providerKey)
        result <- loginInfoOption.map(DBIO.successful(_)).getOrElse(insertLoginInfo(DBLoginInfo(None, loginInfo.providerID, loginInfo.providerKey)))
      } yield {
        result
      }
    }
    // combine database actions to be run sequentially
    val loginInfoAction = DBIO.sequence(loginInfoActions)
    val userLoginInfoAction = for {
      loginInfos <- loginInfoAction
      _ <- DBIO.seq(loginInfos.map(loginInfo => slickUserLoginInfos.insertOrUpdate(DBUserLoginInfo(dbUser.accountId, loginInfo.id.get))): _*)
    } yield ()

    val actions = (
      for {
        _ <- slickUsers.insertOrUpdate(dbUser)
        _ <- userLoginInfoAction
      } yield ()
    ).transactionally

    Logger.info("save User")
    // run actions and return user afterwards
    db.run(actions).map(_ => user)
  }
}
