package com.ft.membership.healthcheck

import java.net.URI
import java.util.concurrent.TimeUnit

import com.ft.membership.logging.{LoggingContext, Logging}
import com.ft.platform.dropwizard.{AdvancedHealthCheck, AdvancedResult}
import com.netflix.hystrix.HystrixCommand.Setter
import com.netflix.hystrix._
import com.ft.membership.healthcheck.ServiceAvailabilityHealthCheck.Timeout

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

abstract class ServiceAvailabilityHealthCheck(val serviceAvailabilityClient: ServiceAvailability,
                                              val serviceName: String,
                                              val baseUri: URI,
                                              val panicGuideUri: String)
                                             (implicit ec:ExecutionContext)
  extends AdvancedHealthCheck(s"${serviceName}AvailabilityHealthCheck") with Logging {

  override def checkAdvanced() = {
    implicit val loggingContext = new LoggingContext()
    try {
      HealthCheckTimer.time(new GoodToGoCommand().execute()) match {
        case (true, duration) =>
          AdvancedResult.healthy(s"Received 'true' from ${baseUri.toString} in $duration ms")
        case (_, duration) =>
          AdvancedResult.error(this, s"$serviceName /__gtg endpoint at ${baseUri.toString} has returned 'false'. " +
            s"DurationMs=$duration TimeoutMs=$Timeout")
      }
    }
    catch {
      case e: Exception =>
        AdvancedResult.error(this, s"$serviceName /__gtg endpoint at ${baseUri.toString} has returned 'false'. " +
          s"TimeoutMs=$Timeout. Exception=$e")
    }
  }

  private class GoodToGoCommand(implicit loggingContext: LoggingContext) extends HystrixCommand[Boolean](
    Setter.withGroupKey(
      HystrixCommandGroupKey.Factory.asKey(s"$serviceName Health Check"))
      .andCommandKey(HystrixCommandKey.Factory.asKey("Availability Health Check"))
      .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
        .withExecutionTimeoutInMilliseconds(Timeout))
      .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
        .withCoreSize(1)
        .withMaxQueueSize(10)
        .withQueueSizeRejectionThreshold(10))
  )
  {
    def run = Await.result(serviceAvailabilityClient
      .goodToGo()
      .fold(_ => false, _ => true),
      Duration(1000L, TimeUnit.MILLISECONDS))
  }

  override def panicGuideUrl() = panicGuideUri
}

object ServiceAvailabilityHealthCheck {
  val Timeout = 10000
}
