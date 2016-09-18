package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc.Controller
import utils.auth.DefaultEnv
import forms.ProfileForm
import play.api.data.Form
import play.api.data.Forms._
import models.services.UserService
import scala.concurrent._
import scala.concurrent.duration.Duration

import java.sql.Date

import play.api.Logger

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param userService The user service implementation.
 * @param socialProviderRegistry The social provider registry.
 * @param webJarAssets The webjar assets implementation.
 */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  socialProviderRegistry: SocialProviderRegistry,
  implicit val webJarAssets: WebJarAssets
)
  extends Controller with I18nSupport {

  val openOptions = Seq(
    ("0" -> Messages("open.to.none")),
    ("1" -> Messages("open.to.cofollower")),
    ("2" -> Messages("open.to.follower")),
    ("3" -> Messages("open.to.all"))
  )

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  def profile = silhouette.SecuredAction.async { implicit request =>
    val form = Form(
      mapping(
        "accountName" -> optional(nonEmptyText(maxLength = 191)
          .verifying(
            Messages("error.did.not.change.name"),
            accountName => accountName != request.identity.accountName
          )
          .verifying(
            Messages("error.this.name.has.been.used"),
            accountName => Await.result(
              userService.retrieveByAccountName(accountName), Duration.Inf
            ).getOrElse(None) == None
          )),
        "userName" -> optional(text(maxLength = 191)),
        "email" -> optional(email
          .verifying(
            Messages("error.this.email.has.been.used"),
            email => Await.result(
              userService.retrieveByEmail(email), Duration.Inf
            ).getOrElse(None) == None
          )),
        "birthDay" -> optional(sqlDate),
        "openBirthDay" -> optional(number),
        "comment" -> optional(text),
        "residence" -> optional(text),
        "openResidence" -> optional(number),
        "openHistory" -> optional(number)
      )(ProfileForm.Data.apply)(ProfileForm.Data.unapply)
    )

    Future.successful(Ok(views.html.profile(
      request.identity,
      form.fill(ProfileForm.Data(
        Some(request.identity.accountName),
        request.identity.userName,
        request.identity.email,
        request.identity.birthDay,
        Some(request.identity.openBirthDay),
        request.identity.comment,
        request.identity.residence,
        Some(request.identity.openResidence),
        Some(request.identity.openHistory)
      )),
      openOptions
    )))
  }

  def submit = silhouette.SecuredAction.async { implicit request =>
    val form = Form(
      mapping(
        "accountName" -> optional(nonEmptyText(maxLength = 191)
          .verifying(
            Messages("error.did.not.change.name"),
            accountName => accountName != request.identity.accountName
          )
          .verifying(
            Messages("error.this.name.has.been.used"),
            accountName => Await.result(
              userService.retrieveByAccountName(accountName), Duration.Inf
            ).getOrElse(None) == None
          )),
        "userName" -> optional(text(maxLength = 191)),
        "email" -> optional(email.verifying(
          Messages("error.this.email.has.been.used"),
          email => Await.result(
            userService.retrieveByEmail(email), Duration.Inf
          ).getOrElse(None) == None
        )),
        "birthDay" -> optional(sqlDate),
        "openBirthDay" -> optional(number),
        "comment" -> optional(text),
        "residence" -> optional(text),
        "openResidence" -> optional(number),
        "openHistory" -> optional(number)
      )(ProfileForm.Data.apply)(ProfileForm.Data.unapply)
    )

    form.bindFromRequest.fold(
      form => {
        Logger.error("OK - BadRequest")
        Future.successful(BadRequest(views.html.profile(
          request.identity,
          form,
          openOptions
        )))
      },
      data => {
        data match {
          case data if data.accountName != None => {
            /* Change Account Name */
            val user = request.identity.copy(
              accountName = data.accountName.get,
              changeableAccountName = false
            )
            userService.save(user)
            Future.successful(Ok(views.html.profile(
              user,
              form,
              openOptions
            )))
          }

          case data if data.email != None => {
            /* Change Email Address */
            /* TODO: Send Email and confirm address */
            Future.successful(Ok(views.html.profile(
              request.identity,
              form,
              openOptions
            )))
          }

          case data if data.userName != None => {
            /* Change User Name */
            val user = request.identity.copy(
              userName = data.userName
            )
            userService.save(user)
            Future.successful(Ok(views.html.profile(
              user,
              form,
              openOptions
            )))
          }

          case data if data.birthDay != None => {
            /* Change Birthday */
            Logger.error("Ok birthday")
            val user = request.identity.copy(
              birthDay = data.birthDay
            )
            userService.save(user)
            Future.successful(Ok(views.html.profile(
              user,
              form,
              openOptions
            )))
          }

          case data if data.openBirthDay != None => {
            /* Change Publicational Range of Birthday */
            val user = request.identity.copy(
              openBirthDay = data.openBirthDay.get
            )
            userService.save(user)
            Future.successful(Ok(views.html.profile(
              user,
              form,
              openOptions
            )))
          }

          case data if data.comment != None => {
            /* Change Comment */
            val user = request.identity.copy(
              comment = data.comment
            )
            userService.save(user)
            Future.successful(Ok(views.html.profile(
              user,
              form,
              openOptions
            )))
          }

          case data if data.residence != None => {
            /* Change Residence */
            val user = request.identity.copy(
              residence = data.residence
            )
            userService.save(user)
            Future.successful(Ok(views.html.profile(
              user,
              form,
              openOptions
            )))
          }

          case data if data.openResidence != None => {
            /* Change Publicational Range of Residence */
            val user = request.identity.copy(
              openResidence = data.openResidence.get
            )
            userService.save(user)
            Future.successful(Ok(views.html.profile(
              user,
              form,
              openOptions
            )))
          }

          case data if data.openHistory != None => {
            /* Change Publicational Range of History */
            val user = request.identity.copy(
              openHistory = data.openHistory.get
            )
            userService.save(user)
            Future.successful(Ok(views.html.profile(
              user,
              form,
              openOptions
            )))
          }

          case _ =>
            /* Nothing to Do */
            Future.successful(Ok(views.html.profile(
              request.identity,
              form,
              openOptions
            )))
        }
      }
    )
  }
}
