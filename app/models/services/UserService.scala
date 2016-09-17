package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait UserService extends IdentityService[User] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[User]]

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param accountName The account name to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given account name.
   */
  def retrieveByAccountName(accountName: String): Future[Option[User]]

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param email The email address to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given email address.
   */
  def retrieveByEmail(email: String): Future[Option[User]]

  def hasCredentialLoginInfo(user: User): Boolean

  def hasTwitterLoginInfo(user: User): Boolean

  def hasGoogleLoginInfo(user: User): Boolean

  def hasFacebookLoginInfo(user: User): Boolean

  def hasClefLoginInfo(user: User): Boolean

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User): Future[User]

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile): Future[User]
}
