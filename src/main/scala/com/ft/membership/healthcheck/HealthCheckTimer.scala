package com.ft.membership.healthcheck

import java.time.Duration.between
import java.time.Instant.now

object HealthCheckTimer {

  def time[R](f: => R): (R, Long) = {
    val startTime = now()
    (f, between(startTime, now()).toMillis)
  }
}
