package models.services

import java.util.UUID

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future

/**
 * Handles actions to logininfos.
 */
trait LoginInfoService {

  /**
   * Retrieves a login info that matches the specified account ID.
   *
   * @param id The ID to retrieve an account.
   * @return The retrieved login info or None if no login info could be retrieved for the given ID.
   */
  def retrieve(id: UUID): Future[Option[Seq[LoginInfo]]]

  def getCredentialLoginInfo(user: User): Option[LoginInfo]

}
