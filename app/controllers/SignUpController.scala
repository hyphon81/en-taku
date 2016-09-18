package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import models.User
import models.services.{ AuthTokenService, UserService }
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.Controller
import utils.auth.DefaultEnv
import play.api.data.Form
import play.api.data.Forms._
import scala.concurrent._
import scala.concurrent.duration.Duration

import play.api.Logger

/**
 * The `Sign Up` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param avatarService          The avatar service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 * @param mailerClient           The mailer client.
 * @param webJarAssets           The webjar assets implementation.
 */
class SignUpController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasherRegistry: PasswordHasherRegistry,
  mailerClient: MailerClient,
  implicit val webJarAssets: WebJarAssets
)
  extends Controller with I18nSupport {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "accountName" -> nonEmptyText(maxLength = 191).verifying(
        Messages("error.this.name.has.been.used"),
        accountName => Await.result(
          userService.retrieveByAccountName(accountName), Duration.Inf
        ).getOrElse(None) == None
      ),
      "userName" -> nonEmptyText(maxLength = 191),
      "email" -> email,
      "password" -> tuple(
        "main" -> nonEmptyText(minLength = 8, maxLength = 191),
        "confirm" -> nonEmptyText(maxLength = 191)
      ).verifying(
          Messages("error.password.do.not.match"), password => password._1 == password._2
        )
    )(SignUpForm.Data.apply)(SignUpForm.Data.unapply)
  )

  /**
   * Views the `Sign Up` page.
   *
   * @return The result to display.
   */
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signUp(form)))
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        val result = Redirect(routes.SignUpController.view()).flashing("info" -> Messages("sign.up.email.sent", data.email))
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            val url = routes.SignInController.view().absoluteURL()
            mailerClient.send(Email(
              subject = Messages("email.already.signed.up.subject"),
              from = Messages("email.from"),
              to = Seq(data.email),
              bodyText = Some(views.txt.emails.alreadySignedUp(user, url).body),
              bodyHtml = Some(views.html.emails.alreadySignedUp(user, url).body)
            ))

            Future.successful(result)
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(data.password._1)
            userService.retrieveByEmail(data.email).flatMap {
              case Some(user) =>
                for {
                  user <- userService.save(user.copy(
                    loginInfos = user.loginInfos :+ loginInfo
                  ))
                  authInfo <- authInfoRepository.add(loginInfo, authInfo)
                  authToken <- authTokenService.create(user.accountId)
                } yield {
                  val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
                  mailerClient.send(Email(
                    subject = Messages("email.sign.up.subject"),
                    from = Messages("email.from"),
                    to = Seq(data.email),
                    bodyText = Some(views.txt.emails.signUp(user, url).body),
                    bodyHtml = Some(views.html.emails.signUp(user, url).body)
                  ))

                  silhouette.env.eventBus.publish(SignUpEvent(user, request))
                  result
                }
              case None =>
                val user = User(
                  accountId = UUID.randomUUID(),
                  loginInfos = Seq(loginInfo),
                  accountName = data.accountName,
                  email = Some(data.email),
                  userName = Some(data.userName),
                  avatarURL = None,
                  birthDay = None,
                  openBirthDay = 0,
                  comment = None,
                  residence = None,
                  openResidence = 0,
                  openHistory = 3,
                  activated = false,
                  isAdmin = false,
                  changeableAccountName = false
                )
                for {
                  avatar <- avatarService.retrieveURL(data.email)
                  user <- userService.save(user.copy(avatarURL = avatar))
                  authInfo <- authInfoRepository.add(loginInfo, authInfo)
                  authToken <- authTokenService.create(user.accountId)
                } yield {
                  val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
                  mailerClient.send(Email(
                    subject = Messages("email.sign.up.subject"),
                    from = Messages("email.from"),
                    to = Seq(data.email),
                    bodyText = Some(views.txt.emails.signUp(user, url).body),
                    bodyHtml = Some(views.html.emails.signUp(user, url).body)
                  ))

                  silhouette.env.eventBus.publish(SignUpEvent(user, request))
                  result
                }
            }
        }
      }
    )
  }
}
