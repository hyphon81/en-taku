package forms

import java.sql.Date

import play.api.data.Form
import play.api.data.Forms._

object ProfileForm {

  /**
   * The form data.
   *
   * @param accountName The account name of a user.
   * @param userName The name of a user.
   * @param email The email of the user.
   * @param birthDay The birth day of the user.
   * @param openBirthDay
   * @param comment The comment of the user.
   * @param residence The residence of the user.
   * @param openResidence
   */
  case class Data(
    accountName: Option[String],
    userName: Option[String],
    email: Option[String],
    birthDay: Option[Date],
    openBirthDay: Option[Int],
    comment: Option[String],
    residence: Option[String],
    openResidence: Option[Int],
    openHistory: Option[Int]
  )
}
