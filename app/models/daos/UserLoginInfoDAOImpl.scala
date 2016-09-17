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
 * Give access to the userlogininfo object.
 */
class UserLoginInfoDAOImpl @Inject() (
  protected val dbConfigProvider: DatabaseConfigProvider
) extends UserLoginInfoDAO with DAOSlick {

  import driver.api._

  /**
   * Finds user login infos by their account ID.
   *
   * @param accountId The account ID of login infos to find.
   * @return The found user login infos or None if no login info for the given account ID could be found.
   */
  def find(accountId: UUID) = {
    val query = for {
      dbUserLoginInfo <- slickUserLoginInfos.filter(_.accountId === UUIDHelper.toByteArray(accountId))
    } yield dbUserLoginInfo
    db.run(query.result).map { resultOption =>
      Some(resultOption.map {
        case userLoginInfo =>
          UserLoginInfo(
            UUIDHelper.fromByteArray(userLoginInfo.accountId),
            userLoginInfo.loginInfoId
          )
      })
    }
  }
}
