package core

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.mockito.scalatest.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.{Await, Future}

trait BaseServiceTest extends AnyWordSpec with Matchers with ScalatestRouteTest with MockitoSugar {

  def awaitForResult[T](futureResult: Future[T]): T =
    Await.result(futureResult, 5.seconds)

}
