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
import models.CreatePayeAppealRequest
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.*
import utils.WireMockHelper

class CisPayePasConnectorPayeISpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with WireMockHelper {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.cis-paye-penalties-and-appeals.host" -> "localhost",
        "microservice.services.cis-paye-penalties-and-appeals.port" -> server.port(),
        "microservice.services.cis-paye-penalties-and-appeals.protocol" -> "http"
      )
      .build()

  private lazy val connector = app.injector.instanceOf[CisPayePasConnector]

  "CisPayePasConnector Integration Tests (PAYE)" - {

    "createPayeAppeal()" - {

      val createPayeAppealRequestJson = Json.obj(
        "penaltyId" -> 1
      )
      val createPayeAppealResponseJson: JsValue = Json.obj(
        "created" -> true
      )

      val createPayeAppealPath = "/cis-paye-penalties-and-appeals/paye/appeal"

      "must return CreatePayeAppealResponse when the stub returns 200 OK" in {
        server.stubFor(post(urlPathEqualTo(createPayeAppealPath)).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(createPayeAppealResponseJson.toString())))
        val request = createPayeAppealRequestJson.as[CreatePayeAppealRequest]
        val result = connector.createPayeAppeal(request).futureValue
        result.created mustBe true
        server.verify(1, postRequestedFor(urlPathEqualTo(createPayeAppealPath)))
      }

      "must throw UpstreamErrorResponse when stub returns 400 Bad Request" in {
        server.stubFor(post(urlPathEqualTo(createPayeAppealPath)).willReturn(aResponse().withStatus(400).withBody("Bad Request")))
        val request = createPayeAppealRequestJson.as[CreatePayeAppealRequest]
        val result = connector.createPayeAppeal(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 400
      }

      "must throw UpstreamErrorResponse when stub returns 500 Internal Server Error" in {
        server.stubFor(post(urlPathEqualTo(createPayeAppealPath)).willReturn(aResponse().withStatus(500).withBody("Internal Server Error")))
        val request = createPayeAppealRequestJson.as[CreatePayeAppealRequest]
        val result = connector.createPayeAppeal(request).failed.futureValue
        result mustBe an[UpstreamErrorResponse]
        result.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
      }
    }
  }
}
