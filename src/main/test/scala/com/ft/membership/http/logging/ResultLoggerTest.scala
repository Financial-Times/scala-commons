package com.ft.membership.http.logging

import java.util.UUID

import akka.actor.ActorSystem
import com.codahale.metrics.MetricRegistry
import com.ft.membership.api.{ResultErrors, Result}
import com.ft.membership.logging.{LoggingContext, ResultLogger}
import org.junit.{After, Test}
import org.junit.Assert.fail
import org.slf4j.LoggerFactory
import org.hamcrest.MatcherAssert._
import org.hamcrest.core.Is.is

class ResultLoggerTest {

  implicit val system = ActorSystem()
  import system.dispatcher

  @After
  def tearDown(): Unit ={
    system.shutdown()
  }

  @Test
  def shouldReturnSuccessWhenNoFailuresOccurred(): Unit ={
    val resultLogger = new ResultLogger(new MetricRegistry)
    implicit val loggingContext = LoggingContext(UUID.randomUUID().toString)

    val result = resultLogger.info("test-operation",
      Nil,
      LoggerFactory.getLogger(this.getClass),
      Nil,
      Result.Success("PASS"))

    result.fold({
      case errors =>
        fail("There should be no Result failures.")
    }, { result =>
      assertThat(result, is("PASS"))
    })
  }

  @Test
  def shouldNotReturnSuccessWhenFailuresOccurred(): Unit ={
    val resultLogger = new ResultLogger(new MetricRegistry)
    implicit val loggingContext = LoggingContext(UUID.randomUUID().toString)

    val result = resultLogger.info("test-operation",
      Nil,
      LoggerFactory.getLogger(this.getClass),
      Nil,
      Result.Failure(ResultErrors.single("ERROR1", "message")))

    result.fold({
      case resultErrors@ResultErrors(errors) if errors.exists( error =>
        List("ERROR1").contains(error.code)) =>
        assertThat(resultErrors.errors.head.code, is("ERROR1"))
    }, { result =>
      fail("There should be no Result successes.")

    })
  }

}
