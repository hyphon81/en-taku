package models

import java.util.UUID
import java.sql.Date

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

/**
 * The user object.
 *
 * @param accountId The unique ID of the account.
 * @param loginInfo The linked login info.
 * @param accountName The unique name of the authenticated user.
 * @param email Maybe the email of the authenticated provider.
 * @param userName The changeable name of the authenticated user.
 * @param avatarURL Maybe the avatar URL of the authenticated provider.
 * @param activated Indicates that the user has activated its registration.
 * @param isAdmin Indicates that the user has administrator permissions.
 */
case class User(
  accountId: UUID,
  loginInfos: Seq[LoginInfo],
  accountName: String,
  email: Option[String],
  userName: Option[String],
  avatarURL: Option[String],
  birthDay: Option[Date],
  openBirthDay: Int,
  comment: Option[String],
  residence: Option[String],
  openResidence: Int,
  openHistory: Int,
  activated: Boolean,
  isAdmin: Boolean,
  changeableAccountName: Boolean
) extends Identity {

  /**
   * Tries to construct a name.
   *
   * @return Maybe a name.
   */
  def name = userName.orElse {
    Some(accountName)
  }
}
