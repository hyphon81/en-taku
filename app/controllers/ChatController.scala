package controllers

import javax.inject._

import actors.{ ChatRoom, UserSocket }
import akka.actor._
import akka.stream.Materializer
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import utils.auth.DefaultEnv

import models.services.UserService
import models.User
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class ChatController @Inject() (
  val messagesApi: MessagesApi,
  system: ActorSystem,
  mat: Materializer,
  silhouette: Silhouette[DefaultEnv],
  socialProviderRegistry: SocialProviderRegistry,
  implicit val webJarAssets: WebJarAssets
) extends Controller with I18nSupport {
  val User = "user"

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system

  val chatRoom = system.actorOf(Props[ChatRoom], "chat-room")

  def leave = silhouette.SecuredAction { implicit request =>
    Redirect(routes.ApplicationController.index()).withNewSession.flashing("success" -> "See you soon!")
  }

  def chat = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.chat(request.identity)))
  }

  def socket = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(r, None) => Left(Forbidden)
      case HandlerResult(r, Some(user)) => Right(ActorFlow.actorRef(UserSocket.props(user.name.getOrElse("None")) _))
    }
  }

  def test = Action { request =>
    val proto = request.headers("X-FORWARDED-PROTO")
    Ok("Got request [" + request + "] with schema: " + proto)
  }
}
