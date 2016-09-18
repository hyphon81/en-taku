package forms

import play.api.data.Form
import play.api.data.Forms._

object SignUpForm {

  /**
   * The form data.
   *
   * @param accountName The account name of a user.
   * @param userName The user name of a user.
   * @param email The email of the user.
   * @param password The password of the user.
   */
  case class Data(
    accountName: String,
    userName: String,
    email: String,
    password: (String, String)
  )
}
