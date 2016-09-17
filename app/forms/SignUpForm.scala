package forms

import play.api.data.Form
import play.api.data.Forms._

object SignUpForm {

  /**
   * The form data.
   *
   * @param firstName The first name of a user.
   * @param lastName The last name of a user.
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
