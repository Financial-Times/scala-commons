package com.ft.membership.healthcheck

import java.net.InetAddress

import com.ft.membership.logging.{LoggingContext, Logging}
import com.ft.platform.dropwizard.{AdvancedResult, AdvancedHealthCheck}
import com.netflix.hystrix._
import com.netflix.hystrix.HystrixCommand.Setter
import com.ft.membership.healthcheck.DnsHealthCheck.Timeout

abstract class DnsHealthCheck(val domain: String, val panicGuideUri: String, val serviceName: String)
  extends AdvancedHealthCheck(s"${serviceName}DnsHealthCheck") with Logging {

  override def checkAdvanced = {
    implicit val context = new LoggingContext()
    try {
      val result = HealthCheckTimer.time(new DnsHealthCheckCommand().execute())
      AdvancedResult.healthy(s"Successfully resolved ${domain} in ${result._2} ms")
    } catch {
      case e: Exception =>
        val message = s"${serviceName} DNS lookup failed or timed out for ${domain}. Exception=${e.getMessage} timeoutMs=${Timeout}"
        log.error(message)
        AdvancedResult.error(this, message)
    }
  }

  private class DnsHealthCheckCommand extends HystrixCommand[InetAddress](
    Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(s"${serviceName} Health Check"))
      .andCommandKey(HystrixCommandKey.Factory.asKey("DNS Check"))
      .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
        .withExecutionTimeoutInMilliseconds(Timeout))
      .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
        .withCoreSize(1)
        .withMaxQueueSize(10)
        .withQueueSizeRejectionThreshold(10))
  ) {
    override def run = {
      if(domain == null)
        throw new IllegalArgumentException(s"$serviceName domain cannot be null")
      InetAddress.getByName(domain)
    }
  }

  override def panicGuideUrl() = panicGuideUri
}

object DnsHealthCheck {
  val Timeout = 1000
}
