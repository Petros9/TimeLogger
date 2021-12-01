/*
import akka.http.scaladsl.model.headers.RawHeader
import core.AuthTokenContent
import io.circe.generic.auto.exportEncoder
import io.circe.syntax.EncoderOps
import pdi.jwt.{Jwt, JwtAlgorithm}

import scala.concurrent.duration._

class GatlingTest extends Simulation {
  def buildAuthToken(id: String): String = Jwt.encode(AuthTokenContent(id).asJson.noSpaces, "secret", JwtAlgorithm.HS256)

  val httpProtocol = http
    .baseUrls("http://localhost:9000")
    .acceptHeader("text/plain,text/html,application/json,application/xml;")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn = scenario("BasicSimulation")
    .feed(jsonFile(classOf[GatlingTest].getResource("/search_data.json").getPath).random)
    .exec(
      http("search")
        .get(""" /projects/all_projects" """.strip())
        .header("Token", buildAuthToken("30313317-59c1-4a53-965e-20baf6f2e1f3"))
        .body(StringBody(s"""{"idList": ${Seq("13e8d694-906c-4095-9edc-aa1406502964", "148a0331-ba48-411c-a351-62e0fe273ba5").asJson},"startTime": "${0L}", "endTime": "${Long.MaxValue}", "deleted": ${false}, "creationTimeIncSorting": ${true}, "updateTimeIncSorting": ${true}}"""))
        .asJson
    )
    .pause(5)

  setUp(
    scn.inject(rampUsers(20000).during(1.minute))
  ).protocols(httpProtocol)
}*/
