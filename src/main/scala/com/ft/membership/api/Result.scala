package com.ft.membership.api

import com.ft.membership.logging.{Logging, LoggingContext}

import scala.beans.BeanProperty
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.JavaConverters._

object Result {
  def Success[A](value: A): Result[A] = Result(Future.successful(scala.Right(value)))
  def Failure[A](error: ResultErrors): Result[A] = Result(Future.successful(scala.Left(error)))

  object Async {
    def Success[A](futureValue: Future[A])(implicit ex: ExecutionContext): Result[A] = Result(futureValue.map(scala.Right(_)))
    def Failure[A](futureError: Future[ResultErrors])(implicit ex: ExecutionContext): Result[A] = Result(futureError.map(scala.Left(_)))
    def FutureResult[A](futureResult: Future[Result[A]])(implicit ex: ExecutionContext, loggingContext: LoggingContext): Result[A] = Success(futureResult).flatMap[A](resultOfA => resultOfA)
  }
}

case class Result[A] private(underlying: Future[Either[ResultErrors, A]]) extends Logging {
  def map[B](f: A => B)(implicit ec: ExecutionContext, loggingContext: LoggingContext): Result[B] = {
    Result(asFuture.map(_.right.map(f)))
  }

  def mapFailure(f: ResultErrors => ResultErrors)(implicit ec: ExecutionContext, loggingContext: LoggingContext): Result[A] = {
    Result(asFuture.map(_.left.map(f)))
  }

  def flatMap[B](f: A => Result[B])(implicit ec: ExecutionContext, loggingContext: LoggingContext): Result[B] = {
    Result(
      asFuture.flatMap({
        case Right(value) => f(value).asFuture
        case Left(errors) => Future.successful(Left(errors))
      })
    )
  }

  def fold[B](failure: ResultErrors => B, success: A => B)(implicit ec: ExecutionContext, loggingContext: LoggingContext) = {
    asFuture.map(_.fold(failure, success))
  }

  def asFuture(implicit executionContext: ExecutionContext, loggingContext: LoggingContext): Future[Either[ResultErrors, A]] = {
    underlying recover { case error =>
      log(loggingContext).info("Exception Occurred", error)
      Left(ResultErrors(List(ResultError("unexpected-error", "Exception:" + error.getMessage, None))))
    }
  }

}

object ResultErrors {
  def single(code:String, message:String, field:Option[String]): ResultErrors = {
    ResultErrors(List(ResultError(code, message, field)))
  }

  def single(code:String, message:String): ResultErrors = {
    ResultErrors.single(code, message, None)
  }

  def single(code:String, message:String, field: String): ResultErrors = {
    ResultErrors.single(code, message, Some(field))
  }
}

case class ResultErrors(errors: List[ResultError]) {
  def mapMessages(fn: String => String) = copy(errors = errors.map(error => error.copy(message = fn(error.message))))

  def getErrors = {
    errors.map({ error =>
      Map( error.field.map("field" -> _).toList ::: List("code" -> error.code, "message" -> error.message):_* ).asJava
    }).asJava
  }
}

case class ResultError(@BeanProperty() code:String, @BeanProperty() message:String, @BeanProperty() field:Option[String] = None)
