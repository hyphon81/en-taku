package controllers

import javax.inject.Inject

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.i18n.{ MessagesApi, I18nSupport }

import play.api.db.slick._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.concurrent.{ Future, Await }

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._

class DashboardController @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val messagesApi: MessagesApi
) extends Controller
  with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  // profiles
  def profiles = TODO

}
