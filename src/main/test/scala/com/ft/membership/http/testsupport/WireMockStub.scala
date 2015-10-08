package com.ft.membership.http.testsupport

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.Notifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

abstract class WireMockStub(port: Int, verboseLogging: Boolean) extends TestRule {
  protected var wireMockServer: WireMockServer = null
  protected var wireMock: WireMock = null

  def apply(base : Statement, description : Description) ={
     new Statement() {
      override def evaluate()  {
        WireMockStub.this.createAndStartIfNull()

        try {
          WireMockStub.this.configureDefaults()
          base.evaluate();
        } finally {
          WireMockStub.this.stopIfNotNull()
        }

      }
    };
  }

  private def createAndStartIfNull() {
        if(this.wireMockServer == null) {
            this.wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
              .port(this.port)
              .notifier(new NullNotifier))
            this.wireMock = new WireMock("localhost", this.port)
            this.wireMockServer.start()
        }

    }

  private def stopIfNotNull() {
        if(this.wireMockServer != null) {
            this.stop()
        }

    }

  def stop() {
    this.wireMockServer.stop()
    this.wireMockServer = null
  }

  def reset() {
    this.wireMock.resetMappings()
    this.configureDefaults()
  }

  def configureDefaults()
}

class NullNotifier extends Notifier {
  override def error(s: String): Unit = {}

  override def error(s: String, throwable: Throwable): Unit = {}

  override def info(s: String): Unit = {}
}
