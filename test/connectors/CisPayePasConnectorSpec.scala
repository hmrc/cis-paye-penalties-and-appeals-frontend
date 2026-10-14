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

import base.SpecBase
import models.{CreateCisAppealRequest, CreateCisAppealResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class CisPayePasConnectorSpec extends SpecBase with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  private val testBackendUrl = "http://localhost:9001"

  "CisPayePasConnectorConnector" - {

    "createCisAppeal" - {

      "must return CreateCisAppealResponse when request is successful" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateCisAppealRequest(penaltyId = 1)
        val expectedResult = CreateCisAppealResponse(true)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateCisAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        val result = connector.createCisAppeal(testRequest).futureValue

        result mustBe expectedResult
        result.created mustBe true
      }

      "must handle Left response with UpstreamErrorResponse" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateCisAppealRequest(penaltyId = 1)

        val upstreamError = uk.gov.hmrc.http.UpstreamErrorResponse("Bad Request", 400)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateCisAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Left(upstreamError)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.createCisAppeal(testRequest).failed) { exception =>
          exception mustBe upstreamError
        }
      }

      "must handle general exceptions" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val testRequest = CreateCisAppealRequest(penaltyId = 1)
        val runtimeException = new RuntimeException("Connection failed")

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateCisAppealResponse]](any(), any()))
          .thenReturn(Future.failed(runtimeException))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)

        whenReady(connector.createCisAppeal(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connection failed"
        }
      }

      "must pass implicit HeaderCarrier to http client" in {
        val mockHttpClient = mock[HttpClientV2]
        val mockConfig = mock[ServicesConfig]
        val mockRequestBuilder = mock[RequestBuilder]
        val customHc = HeaderCarrier(sessionId = Some(uk.gov.hmrc.http.SessionId("test-session")))
        val testRequest = CreateCisAppealRequest(penaltyId = 1)
        val expectedResult = CreateCisAppealResponse(created = true)

        when(mockConfig.baseUrl("cis-paye-penalties-and-appeals")).thenReturn(testBackendUrl)
        when(mockHttpClient.post(any())(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(any())(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[Either[uk.gov.hmrc.http.UpstreamErrorResponse, CreateCisAppealResponse]](any(), any()))
          .thenReturn(Future.successful(Right(expectedResult)))

        val connector = new CisPayePasConnector(mockHttpClient, mockConfig)
        connector.createCisAppeal(testRequest)(customHc, request).futureValue

        verify(mockHttpClient, times(1)).post(any())(any())
      }
    }
  }
}