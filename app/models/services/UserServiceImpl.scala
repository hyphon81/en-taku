package models.services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User
import models.daos.UserDAO
import play.api.libs.concurrent.Execution.Implicits._

import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import scala.concurrent.Future

import play.api.Logger

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 */
class UserServiceImpl @Inject() (
  userDAO: UserDAO
) extends UserService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(id: UUID) = userDAO.find(id)

  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param accountName The account name to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given account name.
   */
  def retrieveByAccountName(accountName: String) =
    userDAO.findByAccountName(accountName)

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param email The email address to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given email address.
   */
  def retrieveByEmail(email: String) = userDAO.findByEmail(email)

  def hasCredentialLoginInfo(user: User) = {
    user.loginInfos.exists(loginInfo => loginInfo.providerID == CredentialsProvider.ID)
  }

  def hasTwitterLoginInfo(user: User) = {
    user.loginInfos.exists(loginInfo => loginInfo.providerID == "twitter")
  }

  def hasGoogleLoginInfo(user: User) = {
    user.loginInfos.exists(loginInfo => loginInfo.providerID == "google")
  }

  def hasFacebookLoginInfo(user: User) = {
    user.loginInfos.exists(loginInfo => loginInfo.providerID == "facebook")
  }

  def hasClefLoginInfo(user: User) = {
    user.loginInfos.exists(loginInfo => loginInfo.providerID == "clef")
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = userDAO.save(user)

  /**
   * Saves the social profile for a user.
   *
   * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
   *
   * @param profile The social profile to save.
   * @return The user for whom the profile was saved.
   */
  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        userDAO.save(user)

      case None =>
        def insertNewUser = {
          val uuid = UUID.randomUUID()
          userDAO.save(User(
            accountId = uuid,
            loginInfos = Seq(profile.loginInfo),
            accountName = uuid.toString(),
            email = profile.email,
            userName = profile.fullName,
            avatarURL = profile.avatarURL,
            birthDay = None,
            openBirthDay = 0,
            comment = None,
            residence = None,
            openResidence = 0,
            openHistory = 3,
            activated = false,
            isAdmin = false,
            changeableAccountName = true
          ))
        }

        profile.email match {
          case Some(email) =>
            userDAO.findByEmail(email).flatMap {
              case Some(user) =>
                userDAO.save(user.copy(
                  loginInfos = user.loginInfos :+ profile.loginInfo
                ))
              case None =>
                insertNewUser
            }

          case None =>
            insertNewUser
        }

    }
  }
}
