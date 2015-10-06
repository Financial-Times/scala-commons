package com.ft.membership.http.testsupport

import com.github.tomakehurst.wiremock.client.WireMock._


class SimpleStub(port: Int) extends WireMockStub(port, false) {
  override def configureDefaults(): Unit = {}

  def returnOKForTest(): Unit ={

    wireMock.register(
      get(
        urlEqualTo("/test")).willReturn(aResponse().withStatus(200)))

  }
}
