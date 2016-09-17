package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import models.services.UserService
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{ Action, Controller }
import play.api.libs.ws._
import play.api.Configuration
import com.typesafe.config.ConfigFactory
import forms.{ SignInForm, SignOutForm }
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
 * The social auth controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info service implementation.
 * @param socialProviderRegistry The social provider registry.
 * @param webJarAssets The webjar assets implementation.
 */
class SocialAuthController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  socialProviderRegistry: SocialProviderRegistry,
  ws: WSClient,
  configuration: Configuration,
  implicit val webJarAssets: WebJarAssets
)
  extends Controller with I18nSupport with Logger {

  /**
   * Authenticates a user against a social provider.
   *
   * @param provider The ID of the provider to authenticate against.
   * @return The result to display.
   */
  def authenticate(provider: String) = Action.async { implicit request =>
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            value <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index()))
          } yield {
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            result
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Redirect(routes.SignInController.view()).flashing("error" -> Messages("could.not.authenticate"))
    }
  }

  import play.api.libs.json._

  def signOutWithClef = Action.async { implicit request =>
    play.api.Logger.error("Hai!")
    SignOutForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(SignInForm.form, socialProviderRegistry))),
      data => {
        play.api.Logger.error(data.logout_token)
        play.api.Logger.error(configuration.getString("silhouette.clef.clientID").getOrElse("Empty!"))
        play.api.Logger.error(configuration.getString("silhouette.clef.clientSecret").getOrElse("Empty!"))
        val jsonData = Json.obj(
          "logout_token" -> data.logout_token,
          //"app_id" -> configuration.getString("silhouette.clef.clientID"),
          //"app_secret" -> configuration.getString("silhouette.clef.clientSecret")
          "app_id" -> "45e4658cff646ce800f5e1439299a2cc",
          "app_secret" -> "d1bd23b0dca08233cd4bb8ba0eaceb8d"
        )
        play.api.Logger.error(jsonData.toString())
        ws.url(configuration.getString("silhouette.clef.webhookLogoutURL").getOrElse("Empty!")).withHeaders(
          "Accept" -> "*/*",
          "Content-Type" -> "application/x-www-form-urlencoded",
          "Accept-Encoding" -> "gzip, deflate",
          "User-Agent" -> "Clef/1.0 (https://getclef.com)"
        ).post(jsonData).map {
            case response if response.status == 200 =>
              play.api.Logger.error(response.json.toString())
            case response =>
              play.api.Logger.error(response.status.toString() + " " + response.json.toString())
          }
        Future.successful(Redirect(routes.ApplicationController.signOut))
      }
    )
  }
}
