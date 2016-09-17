package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User]]

  /**
   * Finds a user by its account ID.
   *
   * @param accountId The ID of the account to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(accountId: UUID): Future[Option[User]]

  /**
   * Finds a user by its account name.
   *
   * @param accountId The name of the account to find.
   * @return The found user or None if no user for the given name could be found.
   */
  def findByAccountName(accountName: String): Future[Option[User]]

  /**
   * Finds a user by its email.
   *
   * @param email The email address to find.
   * @return The found user or None if no user for the given email address could be found.
   */
  def findByEmail(email: String): Future[Option[User]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]
}
