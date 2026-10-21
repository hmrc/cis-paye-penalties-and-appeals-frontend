/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import models.*
import play.api.Logging
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits.*
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasConnector @Inject()(http: HttpClientV2,
                                    config: ServicesConfig)
                                   (implicit ec: ExecutionContext) extends Logging {

  private val base = config.baseUrl("cis-paye-penalties-and-appeals") + "/cis-paye-penalties-and-appeals"

  def createCisAppeal(createCisAppealRequest: CreateCisAppealRequest)(implicit hc: HeaderCarrier): Future[CreateCisAppealResponse] = {
    http.post(url"$base/cis/appeal")
      .withBody(Json.toJson(createCisAppealRequest))
      .execute[Either[UpstreamErrorResponse, CreateCisAppealResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][createCisAppeal] Penalty Id: ${createCisAppealRequest.penaltyId}, response: $resp")
          logger.info(s"[CisPayePasConnector][createCisAppeal] Create success with penalty Id: ${createCisAppealRequest.penaltyId}")
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

  def getCisAppealablePenalties(request: GetCisAppealablePenaltiesRequest)(implicit hc: HeaderCarrier): Future[GetCisAppealablePenaltiesResponse] = {
    http.post(url"$base/cis/penalties")
      .withBody(Json.toJson(request))
      .execute[Either[UpstreamErrorResponse, GetCisAppealablePenaltiesResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getCisAppealablePenalties] request: ${request}, response: $resp")
          logger.info(s"[CisPayePasConnector][getCisAppealablePenalties] Get success with request: ${request}")
          Future.successful(resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getCisAppealablePenalties]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getCisAppealablePenalties]")
      }
  }

  def getCisAppealablePenalty(getCisAppealablePenaltyRequest: GetCisAppealablePenaltyRequest)
                             (implicit hc: HeaderCarrier): Future[GetCisAppealablePenaltyResponse] = {
    http.post(url"$base/cis/penalty")
      .withBody(Json.toJson(getCisAppealablePenaltyRequest))
      .execute[Either[UpstreamErrorResponse, GetCisAppealablePenaltyResponse]]
      .flatMap {
        case Right(resp) =>
          logger.debug(s"[CisPayePasConnector][getCisAppealablePenalty] request: $getCisAppealablePenaltyRequest, response: $resp")
          logger.info(s"[CisPayePasConnector][getCisAppealablePenalty] Get success with request: $getCisAppealablePenaltyRequest")
          Future.successful(
            resp)
        case Left(error) =>
          logResponse(error, "[CisPayePasConnector][getCisAppealablePenalty]")
          Future.failed(error)
      }
      .recover {
        case e => throw logResponse(e, "[CisPayePasConnector][getCisAppealablePenalty]")
      }
  }

  private def logResponse(e: Throwable, method: String): Throwable = {
    logger.error(s"[$method] Error occurred: ${e.getMessage}", e)
    e
  }
}
