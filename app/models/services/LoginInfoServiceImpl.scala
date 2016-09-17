package models.services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.daos.LoginInfoDAO
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Handles actions to users.
 *
 * @param userDAO The user DAO implementation.
 */
class LoginInfoServiceImpl @Inject() (
  loginInfoDAO: LoginInfoDAO
) extends LoginInfoService {

  /**
   * Retrieves a login info that matches the specified ID.
   *
   * @param id The ID to retrieve an account.
   * @return The retrieved login infos or None if no login info could be retrieved for the given ID.
   */
  def retrieve(id: UUID) = loginInfoDAO.find(id)

  def getCredentialLoginInfo(user: User) = {
    Some(user.loginInfos.filter(_.providerID == CredentialsProvider.ID)(0))
  }
}
