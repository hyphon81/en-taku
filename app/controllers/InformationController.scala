package controllers

import javax.inject.Inject

import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{ Controller, Action }

import scala.concurrent.Future

class InformationController @Inject() (
  val messagesApi: MessagesApi,
  configuration: Configuration,
  implicit val webJarAssets: WebJarAssets
)
  extends Controller with I18nSupport {

  def termsOfUse = Action.async { implicit request =>
    Future.successful(Ok(views.html.termsOfUse()))
  }

  def privacyPolicy = Action.async { implicit request =>
    Future.successful(Ok(views.html.privacyPolicy()))
  }

}
