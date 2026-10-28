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

package services

import base.SpecBase
import connectors.CisPayePasConnector
import models.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ClientListServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  private val mockBackendConnector = mock[CisPayePasConnector]

  private val service = new ClientListService(mockBackendConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBackendConnector)
  }

  "ClientListService" - {

    "getClientListDownloadStatus" - {

      val testRequest = ClientListStatusRequest(credentialId = "123", serviceId = "CIS")
      val expectedResponse = ClientListStatusResponse(status = "Succeeded")

      "must return ClientListStatusResponse when ClientListStatusRequest provided" in {
        when(mockBackendConnector.getClientListDownloadStatus(eqTo(testRequest))(any()))
          .thenReturn(Future.successful(expectedResponse))

        val result = service.getClientListDownloadStatus(testRequest).futureValue

        result mustBe expectedResponse
        result.status mustBe "Succeeded"
        verify(mockBackendConnector, times(1)).getClientListDownloadStatus(eqTo(testRequest))(any())
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.getClientListDownloadStatus(eqTo(testRequest))(any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.getClientListDownloadStatus(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).getClientListDownloadStatus(eqTo(testRequest))(any())
      }
    }
  }
}
