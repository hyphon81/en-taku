package models.daos

import java.util.UUID
import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.Future

/**
 * Give access to the logininfo object.
 */
trait LoginInfoDAO {
  /**
   * Finds login infos by their account ID.
   *
   * @param accountId The account ID of login infos to find.
   * @return The found login infos or None if no login info for the given account ID could be found.
   */
  def find(accountId: UUID): Future[Option[Seq[LoginInfo]]]

}
