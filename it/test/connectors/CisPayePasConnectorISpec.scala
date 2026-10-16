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

import com.github.tomakehurst.wiremock.client.WireMock.*
import models.{CreateCisAppealRequest, GetCisAppealablePenaltiesRequest, GetCisAppealablePenaltiesResponse}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.*
import utils.WireMockHelper

class CisPayePasConnectorISpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with WireMockHelper {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: FakeRequest[_] = FakeRequest()

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.cis-paye-penalties-and-appeals.host" -> "localhost",
        "microservice.services.cis-paye-penalties-and-appeals.port" -> server.port(),
        "microservice.services.cis-paye-penalties-and-appeals.protocol" -> "http"
      )
      .build()

  private lazy val connector = app.injector.instanceOf[CisPayePasConnector]

  "CisPayePasConnector Integration Tests" - {
    "createCisAppeal()" - {

      val createCisAppealRequestJson = Json.obj(
        "penaltyId" -> 1
      )
      val createCisAppealResponseJson: JsValue = Json.obj(
        "created" -> true
      )

      val createCisAppealPath = "/cis-paye-penalties-and-appeals/cis/appeal"

      "must return CreateCisAppealResponse when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo(createCisAppealPath)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createCisAppealResponseJson.toString())))
        val request = createCisAppealRequestJson.as[CreateCisAppealRequest]
        val result = connector.createCisAppeal(request).futureValue
        result.created mustBe true
        server.verify(postRequestedFor(urlPathEqualTo(createCisAppealPath)))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo(createCisAppealPath)).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createCisAppealRequestJson.as[CreateCisAppealRequest]
        val result = connector.createCisAppeal(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo(createCisAppealPath)).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createCisAppealRequestJson.as[CreateCisAppealRequest]
        val result = connector.createCisAppeal(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo(createCisAppealPath)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createCisAppealResponseJson.toString())))
        val request = createCisAppealRequestJson.as[CreateCisAppealRequest]
        connector.createCisAppeal(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo(createCisAppealPath)))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo(createCisAppealPath)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createCisAppealResponseJson.toString())))
        val request = createCisAppealRequestJson.as[CreateCisAppealRequest]
        connector.createCisAppeal(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo(createCisAppealPath)).withHeader("Content-Type", containing("application/json")))
      }
    }

    "getCisAppealablePenalties" - {

      val penaltiesRequestJson = Json.obj(
        "hmrcOfficeNumber"      -> "123",
        "schemeReferenceNumber" -> "AB456",
        "startRow"              -> 1,
        "endRow"                -> 10,
        "sortBy"                -> "unique_id",
        "order"                 -> "desc"
      )

      val penaltiesReturnJson = Json.obj(
        "total" -> 2,
        "appealablePenalties" -> Json.arr(
          Json.obj(
            "uniqueId" -> 1001,
            "issueDate" -> "2026-01-15",
            "taxPeriodEndDate" -> "2025-12-05",
            "penaltyType" -> "CIS003",
            "penaltyAmount" -> 100
          ),
          Json.obj(
            "uniqueId" -> 1002,
            "issueDate" -> "2026-02-20",
            "taxPeriodEndDate" -> "2026-01-05",
            "penaltyType" -> "CIS005",
            "penaltyAmount" -> 200
          )
        )
      )

      val getAppealablePenaltiesUrl = "/cis-paye-penalties-and-appeals/cis/penalties"

      "must return GetCisAppealablePenaltiesResponse when the stub returns 200 OK" in {
        server.stubFor(
          post(urlPathEqualTo(getAppealablePenaltiesUrl))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(penaltiesReturnJson.toString()))
        )
        val request = penaltiesRequestJson.as[GetCisAppealablePenaltiesRequest]
        val result = connector.getCisAppealablePenalties(request).futureValue
        result mustBe a[GetCisAppealablePenaltiesResponse]
        server.verify(postRequestedFor(urlPathEqualTo(getAppealablePenaltiesUrl)))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo(getAppealablePenaltiesUrl)).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = penaltiesRequestJson.as[GetCisAppealablePenaltiesRequest]
        val result = connector.getCisAppealablePenalties(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo(getAppealablePenaltiesUrl)).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = penaltiesRequestJson.as[GetCisAppealablePenaltiesRequest]
        val result = connector.getCisAppealablePenalties(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo(getAppealablePenaltiesUrl)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(penaltiesReturnJson.toString())))
        val request = penaltiesRequestJson.as[GetCisAppealablePenaltiesRequest]
        connector.getCisAppealablePenalties(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo(getAppealablePenaltiesUrl)))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo(getAppealablePenaltiesUrl)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(penaltiesReturnJson.toString())))
        val request = penaltiesRequestJson.as[GetCisAppealablePenaltiesRequest]
        connector.getCisAppealablePenalties(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo(getAppealablePenaltiesUrl)).withHeader("Content-Type", containing("application/json")))
      }
    }
  }
}
