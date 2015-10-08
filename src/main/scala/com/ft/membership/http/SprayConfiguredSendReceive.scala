package com.ft.membership.http

import java.net.URI
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import spray.can.client.HostConnectorSettings
import spray.client.pipelining._

import scala.beans.BeanProperty
import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration

object SprayConfiguredSendReceive {

  def configuredSendAndReceive(httpConfig: SprayConfig, maxTimeoutMillis: Long)
                              (implicit actorSystem: ActorSystem,
                              ec : ExecutionContext) = {
    implicit val timeout = Timeout(60, TimeUnit.SECONDS)
    Await.result[SendReceive](
    {
      val currentSettings = HostConnectorSettings(actorSystem)
      val connectorSettings = currentSettings.copy(
        maxConnections = httpConfig.maxConnections,
        maxRetries = 0,
        maxRedirects = 1,
        pipelining = false,
        idleTimeout = Duration(httpConfig.requestTimeout, TimeUnit.MILLISECONDS),
        connectionSettings = currentSettings.connectionSettings.copy(
          idleTimeout = Duration(httpConfig.requestTimeout, TimeUnit.MILLISECONDS),
          requestTimeout = Duration(httpConfig.requestTimeout, TimeUnit.MILLISECONDS),
          connectingTimeout = Duration(httpConfig.connectionTimeout, TimeUnit.MILLISECONDS)
        )

      )

      for (
        Http.HostConnectorInfo(connector, _) <- IO(Http) ?
          Http.HostConnectorSetup(
            host = httpConfig.baseUrl.getHost,
            port = Option(httpConfig.baseUrl.getPort).filter(_ > 0).getOrElse(443),
            sslEncryption = httpConfig.baseUrl.getScheme == "https",
            settings = Some(connectorSettings)
          )
      ) yield sendReceive(connector)
    },
    Duration(maxTimeoutMillis, TimeUnit.MILLISECONDS)
    )
  }
}
