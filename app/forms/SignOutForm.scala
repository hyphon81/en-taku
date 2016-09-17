package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the sign out webhook.
 */
object SignOutForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "logout_token" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param logout token of the webhook.
   */
  case class Data(
    logout_token: String
  )
}
