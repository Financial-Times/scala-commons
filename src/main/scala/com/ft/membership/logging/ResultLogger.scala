package com.ft.membership.logging

import java.time.Duration.between
import java.time.Instant.now

import com.codahale.metrics.MetricRegistry
import com.ft.membership.api.Result
import org.apache.commons.lang3.StringEscapeUtils.escapeJava
import org.slf4j.Logger

import scala.concurrent.ExecutionContext

class ResultLogger(metricRegistry: MetricRegistry)(implicit val ec: ExecutionContext) {
  import ResultLogger.getParamsAsString

  def debug[T](operation: String, params: List[(String, Any)], logger: Logger, errorCodes: List[String], fn: => Result[T])(implicit lc: LoggingContext) = {
    log(Debug, operation, params, logger, errorCodes, fn)
  }

  def info[T](operation: String, params: List[(String, Any)], logger: Logger, errorCodes: List[String], fn: => Result[T])(implicit lc: LoggingContext) = {
    log(Info, operation, params, logger, errorCodes, fn)
  }

  def log[T](level: LogLevel, operation: String, params: List[(String, Any)], logger: Logger, codesToLogAsErrors: List[String], fn: => Result[T])(implicit lc: LoggingContext) = {
    val time = metricRegistry.timer("operation." + operation).time()
    val startTime = now()
    val result = fn

    result.map { value =>
      time.stop()
      val durationMs = between(startTime, now()).toMillis
      val paramsToLog =
        ("operation"   -> operation) ::
          ("returned"    -> value) ::
          ("duration_ms" -> durationMs) ::
          ("outcome"     -> "success") ::
          params

      level match {
        case Info => logger.info(getParamsAsString(paramsToLog))
        case Debug => logger.debug(getParamsAsString(paramsToLog))
      }

      value
    }.mapFailure { errors =>
      time.stop()
      val durationMs = between(startTime, now()).toMillis
      val errorsAsParams = errors.errors.flatMap(error =>
        List("error_code" -> error.code,
          "error_message"   -> error.message)
      )
      val paramsToLog: List[(String, Any)] =
        "operation"    -> operation ::
          "duration_ms"  -> durationMs ::
          "outcome"      -> "failure" ::
          errorsAsParams :::
          params

      if(errors.errors.exists(error => codesToLogAsErrors.contains(error.code))) {
        logger.error(getParamsAsString(paramsToLog))
      } else {
        level match {
          case Info => logger.info(getParamsAsString(paramsToLog))
          case Debug => logger.debug(getParamsAsString(paramsToLog))
        }
      }
      errors
    }
  }

  sealed abstract class LogLevel
  object Debug extends LogLevel
  object Info extends LogLevel
}

object ResultLogger {
  def getParamsAsString(seq: Seq[(String, Any)]): String = {
    seq.map {
      case (key, value: Number) => (key, value.toString)
      case (key, value: Boolean) => (key, value.toString)
      case (key, null) => (key, "null")
      case (key, value) => (key, "\"%s\"".format(escapeJava(value.toString)))
    }
      .map(_.productIterator.mkString("="))
      .mkString(" ")
  }

}
