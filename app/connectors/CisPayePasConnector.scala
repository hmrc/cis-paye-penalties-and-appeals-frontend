package connectors


import models.CreateCisAppealResponse
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import play.api.mvc.Request
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasConnector @Inject()(http: HttpClientV2,
                                   config: ServicesConfig)
                                  (implicit ec: ExecutionContext) extends Logging {

  private val base = config.baseUrl("cis-paye-penalties-and-appeals")

  def createCisAppeal(penaltyId: Long) (implicit hc: HeaderCarrier,
                                               request: Request[_]): Future[CreateCisAppealResponse] = {
    http.post(url"$base/cis/appeal")
      .withBody(Json.toJson(penaltyId))
      .execute[Either[UpstreamErrorResponse, CreateCisAppealResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][createCisAppeal] Penalty Id: $penaltyId, response: $resp")
          logger.info(s"[CisPayePasConnector][createCisAppeal] Create success with penalty Id: $penaltyId")
          Future.successful(
            resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][createCisAppeal]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][createCisAppeal]")
      }
  }

  private def logResponse(e: Throwable, method: String): Throwable = {
    logger.error(s"[$method] Error occurred: ${e.getMessage}", e)
    e
  }
}
