package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.UserLoginInfo
import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.dbio.DBIOAction
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import utils.UUIDHelper
import slick.driver.MySQLDriver.api._

/**
 * Give access to the logininfo object.
 */
class LoginInfoDAOImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
) extends LoginInfoDAO with DAOSlick {

  import driver.api._

  /**
   * Finds login infos by their account ID.
   *
   * @param accountId The account ID of login infos to find.
   * @return The found login infos or None if no login info for the given account ID could be found.
   */
  def find(accountId: UUID) = {
    val query = for {
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.accountId === UUIDHelper.toByteArray(accountId))
      dbLoginInfo <- slickLoginInfos.filter(_.id === dbUserLoginInfo.loginInfoId)
    } yield dbLoginInfo
    db.run(query.result).map { resultOption =>
      Some(resultOption.map {
        case loginInfo =>
          LoginInfo(
            loginInfo.providerID,
            loginInfo.providerKey
          )
      })
    }
  }
}
