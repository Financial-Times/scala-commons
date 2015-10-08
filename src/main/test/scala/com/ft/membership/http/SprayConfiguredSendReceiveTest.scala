package com.ft.membership.http

import java.net.URI

import akka.actor.ActorSystem
import com.ft.membership.http.testsupport.TestRules
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.is
import org.junit.{Rule, After, Test}
import org.junit.Assert.fail
import spray.client.pipelining._
import spray.http.HttpResponse
import spray.httpx.UnsuccessfulResponseException

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class SprayConfiguredSendReceiveTest {

  val testRule = new TestRules()
  @Rule
  def getRule: TestRules = testRule

  implicit val system = ActorSystem()
  import system.dispatcher

  @After
  def tearDown(): Unit ={
    system.shutdown()
  }

  def assertOnValidResponse(eventualResponse: Future[HttpResponse]): Unit = {
    val response = Await.result(eventualResponse, 5 seconds)
    assertThat(response.status.intValue, is(200))
  }

  @Test
  def shouldConfigureAndSendWithNoExceptions(): Unit ={

    testRule.simpleStub.returnOKForTest()

    val httpConfig  = new SprayConfig(3000, 3000, 3000, new URI("http://localhost:9000/test"))

    val pipeline = SprayConfiguredSendReceive.configuredSendAndReceive(httpConfig, 2000L) ~> unmarshal[HttpResponse]
    try {
      assertOnValidResponse(pipeline {
        Get(
          "http://localhost:9000/test"
        )
      })
    }
    catch {
      case e: UnsuccessfulResponseException =>
        fail(e.toString)
      case _ =>
        fail("Unexpected error thrown")
    }

  }
}
