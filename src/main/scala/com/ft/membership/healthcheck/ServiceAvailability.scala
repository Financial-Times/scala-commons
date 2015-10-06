package com.ft.membership.healthcheck

import com.ft.membership.api.Result
import com.ft.membership.logging.LoggingContext

trait ServiceAvailability {
  def goodToGo()(implicit loggingContext: LoggingContext): Result[String]
}
