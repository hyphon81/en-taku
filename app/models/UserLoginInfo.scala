package models

import java.util.UUID

/**
 * The userlogininfo object.
 */
case class UserLoginInfo(
  accountId: UUID,
  loginInfoId: Long
)
