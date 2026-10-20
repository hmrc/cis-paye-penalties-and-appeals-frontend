/*
 * Copyright 2025 HM Revenue & Customs
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
import models.{CreateCisAppealRequest, GetCisAppealablePenaltyRequest}
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

import java.time.LocalDate

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

    "getCisAppealablePenalty()" - {

      val getCisAppealablePenaltyRequestJson = Json.obj(
        "penaltyId" -> 1,
        "hmrcOfficeNumber" -> "840123",
        "schemeReferenceNumber" -> "098345"
      )
      val getCisAppealablePenaltyResponseJson: JsValue = Json.obj(
        "issueDate" -> "2023-01-01",
        "taxPeriodEndDate" -> "2023-04-01",
        "penaltyType" -> "CIS003",
        "penaltyAmount" -> 999.23
      )

      val getCisAppealablePenaltyPath = "/cis-paye-penalties-and-appeals/cis/penalty"

      "must return GetCisAppealablePenaltyResponse when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo(getCisAppealablePenaltyPath)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(getCisAppealablePenaltyResponseJson.toString())))
        val request = getCisAppealablePenaltyRequestJson.as[GetCisAppealablePenaltyRequest]
        val result = connector.getCisAppealablePenalty(request).futureValue
        result.issueDate mustBe Some(LocalDate.of(2023, 1, 1))
        server.verify(postRequestedFor(urlPathEqualTo(getCisAppealablePenaltyPath)))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo(getCisAppealablePenaltyPath)).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = getCisAppealablePenaltyRequestJson.as[GetCisAppealablePenaltyRequest]
        val result = connector.getCisAppealablePenalty(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo(getCisAppealablePenaltyPath)).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = getCisAppealablePenaltyRequestJson.as[GetCisAppealablePenaltyRequest]
        val result = connector.getCisAppealablePenalty(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }

      "must make POST request to correct endpoint" in {
        server.stubFor(post(urlPathEqualTo(getCisAppealablePenaltyPath)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(getCisAppealablePenaltyResponseJson.toString())))
        val request = getCisAppealablePenaltyRequestJson.as[GetCisAppealablePenaltyRequest]
        connector.getCisAppealablePenalty(request).futureValue
        server.verify(1, postRequestedFor(urlPathEqualTo(getCisAppealablePenaltyPath)))
      }

      "must include correct headers in the request" in {
        server.stubFor(post(urlPathEqualTo(getCisAppealablePenaltyPath)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(getCisAppealablePenaltyResponseJson.toString())))
        val request = getCisAppealablePenaltyRequestJson.as[GetCisAppealablePenaltyRequest]
        connector.getCisAppealablePenalty(request).futureValue
        server.verify(postRequestedFor(urlPathEqualTo(getCisAppealablePenaltyPath)).withHeader("Content-Type", containing("application/json")))
      }
    }
  }
}