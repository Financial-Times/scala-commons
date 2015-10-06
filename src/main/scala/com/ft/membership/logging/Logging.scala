package com.ft.membership.logging

import java.util.UUID
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.container.ContainerRequestContext

import org.apache.commons.lang3.StringEscapeUtils._
import org.slf4j.{Logger, LoggerFactory, Marker}

trait Logging {
  def log(implicit transactionId:LoggingContext) = new ContextLogger(transactionId, LoggerFactory.getLogger(this.getClass))

  def fromRequest(request:HttpServletRequest) = {
    LoggingContext(
      Option(request.getHeader("FT-Transaction-Id"))
        .getOrElse(UUID.randomUUID().toString)
    )
  }

  def fromRequest(request:ContainerRequestContext) = {
    LoggingContext(
      Option(request.getHeaderString("FT-Transaction-Id"))
        .getOrElse(UUID.randomUUID().toString)
    )
  }
}

trait NoContextLogging {
  val log = LoggerFactory.getLogger(this.getClass)

}


case class LoggingContext(transactionId:String) {
  def this() = this(UUID.randomUUID().toString)
}



class ContextLogger(val context:LoggingContext, var logger:Logger) extends Logger{
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

  def messageWithContext(msg: String) = sanitiseMessage(s"transaction_id=${context.transactionId} ${msg}")

  private val passwordRegExp = """((?:client_secret|password_value|password|FT_Api_Key|FT-Api-Key)\s*?[=:]\s*["']?).*?(["'&\s])""".r
  private val sessionTokenRegExp = """([-_A-Za-z0-9]{20,}\.)[-_A-Za-z0-9]{90,}""".r
  def sanitiseMessage(message: String) = {
    sessionTokenRegExp.replaceAllIn(
      passwordRegExp.replaceAllIn(
        message,
        "$1xxxxxxxx$2"
      ),
      "$1xxxxxxxx"
    )
  }

  def debug(keyValues:(String, Object)* ): Unit = debug(getParamsAsString(keyValues))

  def warn(keyValues:(String, Object)* ): Unit = warn(getParamsAsString(keyValues))

  def error(keyValues:(String, Object)* ): Unit = error(getParamsAsString(keyValues))

  def info(keyValues:(String, Object)* ): Unit = info(getParamsAsString(keyValues))

  override def isInfoEnabled(marker: Marker): Boolean = logger.isInfoEnabled(marker)

  override def warn(marker: Marker, format: String, arguments: AnyRef*): Unit = logger.warn(marker, messageWithContext(format), arguments)

  override def isWarnEnabled(marker: Marker): Boolean = logger.isWarnEnabled(marker)

  override def info(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.info(marker, messageWithContext(format), arg1, arg2)

  override def debug(msg: String, t: Throwable): Unit = logger.debug(messageWithContext(msg), t)

  override def trace(marker: Marker, msg: String): Unit = logger.trace(marker, messageWithContext(msg))

  override def debug(format: String, arguments: AnyRef*): Unit = logger.debug(messageWithContext(format), arguments)

  override def warn(msg: String, t: Throwable): Unit = logger.warn(messageWithContext(msg), t)

  override def isErrorEnabled: Boolean = logger.isErrorEnabled

  override def info(marker: Marker, format: String, arg: scala.Any): Unit = logger.info(marker, messageWithContext(format), arg)

  override def isInfoEnabled: Boolean = logger.isInfoEnabled

  override def isDebugEnabled(marker: Marker): Boolean = logger.isDebugEnabled(marker)

  override def error(marker: Marker, format: String, arg: scala.Any): Unit = logger.error(marker, messageWithContext(format), arg)

  override def debug(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.debug(messageWithContext(format), arg1, arg2)

  override def info(marker: Marker, format: String, arguments: AnyRef*): Unit = logger.info(marker, messageWithContext(format), arguments)

  override def error(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.error(messageWithContext(format), arg1, arg2)

  override def warn(format: String, arg: scala.Any): Unit = logger.warn(messageWithContext(format), arg)

  override def warn(format: String, arguments: AnyRef*): Unit = logger.warn(messageWithContext(format), arguments)

  override def trace(marker: Marker, format: String, arg: scala.Any): Unit = logger.trace(marker, messageWithContext(format), arg)

  override def trace(format: String, arg: scala.Any): Unit = logger.trace(messageWithContext(format), arg)

  override def warn(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.warn(marker, messageWithContext(format), arg1, arg2)

  override def error(marker: Marker, msg: String): Unit = logger.error(marker, messageWithContext(msg))

  override def trace(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.trace(marker, messageWithContext(format), arg1, arg2)

  override def info(msg: String, t: Throwable): Unit = logger.info(messageWithContext(msg), t)

  override def info(msg: String): Unit = logger.info(messageWithContext(msg))

  override def info(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.info(messageWithContext(format), arg1, arg2)

  override def warn(marker: Marker, msg: String): Unit = logger.warn(marker, messageWithContext(msg))

  override def debug(marker: Marker, format: String, arguments: AnyRef*): Unit = logger.debug(marker, messageWithContext(format), arguments)

  override def error(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.error(marker, messageWithContext(format), arg1, arg2)

  override def trace(msg: String): Unit = logger.trace(messageWithContext(msg))

  override def warn(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.warn(messageWithContext(format), arg1, arg2)

  override def trace(format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.trace(messageWithContext(format), arg1, arg2)

  override def warn(marker: Marker, format: String, arg: scala.Any): Unit = logger.warn(marker, messageWithContext(format), arg)

  override def trace(marker: Marker, format: String, argArray: AnyRef*): Unit = logger.trace(marker, messageWithContext(format), argArray)

  override def isDebugEnabled: Boolean = logger.isDebugEnabled

  override def error(msg: String, t: Throwable): Unit = logger.error(messageWithContext(msg), t)

  override def info(marker: Marker, msg: String, t: Throwable): Unit = logger.info(marker, messageWithContext(msg), t)

  override def trace(msg: String, t: Throwable): Unit = logger.trace(messageWithContext(msg), t)

  override def isTraceEnabled(marker: Marker): Boolean = logger.isTraceEnabled(marker)

  override def debug(msg: String): Unit = logger.debug(messageWithContext(msg))

  override def warn(marker: Marker, msg: String, t: Throwable): Unit = logger.warn(marker, messageWithContext(msg), t)

  override def getName: String = logger.getName

  override def trace(format: String, arguments: AnyRef*): Unit = logger.trace(messageWithContext(format), arguments)

  override def debug(marker: Marker, msg: String): Unit = logger.debug(marker, messageWithContext(msg))

  override def error(msg: String): Unit = logger.error(messageWithContext(msg))

  override def isErrorEnabled(marker: Marker): Boolean = logger.isErrorEnabled(marker)

  override def debug(format: String, arg: scala.Any): Unit = logger.debug(messageWithContext(format), arg)

  override def error(format: String, arguments: AnyRef*): Unit = logger.error(messageWithContext(format), arguments)

  override def isWarnEnabled: Boolean = logger.isWarnEnabled

  override def error(marker: Marker, format: String, arguments: AnyRef*): Unit = logger.error(marker, messageWithContext(format), arguments)

  override def info(marker: Marker, msg: String): Unit = logger.info(marker, messageWithContext(msg))

  override def info(format: String, arguments: AnyRef*): Unit = logger.info(messageWithContext(format), arguments)

  override def debug(marker: Marker, format: String, arg1: scala.Any, arg2: scala.Any): Unit = logger.debug(marker, messageWithContext(format), arg1, arg2)

  override def debug(marker: Marker, msg: String, t: Throwable): Unit = logger.debug(marker, messageWithContext(msg), t)

  override def info(format: String, arg: scala.Any): Unit = logger.info(messageWithContext(format), arg)

  override def warn(msg: String): Unit = logger.warn(messageWithContext(msg))

  override def error(format: String, arg: scala.Any): Unit = logger.error(messageWithContext(format), arg)

  override def error(marker: Marker, msg: String, t: Throwable): Unit = logger.error(marker, messageWithContext(msg), t)

  override def trace(marker: Marker, msg: String, t: Throwable): Unit = logger.trace(marker, messageWithContext(msg), t)

  override def isTraceEnabled: Boolean = logger.isTraceEnabled

  override def debug(marker: Marker, format: String, arg: scala.Any): Unit = logger.debug(marker, messageWithContext(format), arg)
}

