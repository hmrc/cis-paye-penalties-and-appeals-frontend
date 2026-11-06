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

    "hasCisClient" - {

      val testRequest = HasCisClientRequest(
        agentDetails = AgentDetails(
          credentialId = "CRED01",
          irAgentId = "123456"
        ),
        contractorDetails = ContractorDetails(
          hmrcOfficeNumber = "754",
          schemeReferenceNumber = "EZ00100"
        )
      )

      "must return HasClientResponse with hasClient true when successful" in {
        val testResponse = HasClientResponse(hasClient = true)

        when(mockBackendConnector.hasCisClient(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.successful(testResponse))

        val result = service.hasCisClient(testRequest).futureValue

        result mustBe testResponse
        verify(mockBackendConnector, times(1)).hasCisClient(eqTo(testRequest))(any[HeaderCarrier])
      }

      "must return HasClientResponse with hasClient false when successful" in {
        val testResponse = HasClientResponse(hasClient = false)

        when(mockBackendConnector.hasCisClient(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.successful(testResponse))

        val result = service.hasCisClient(testRequest).futureValue

        result mustBe testResponse
        verify(mockBackendConnector, times(1)).hasCisClient(eqTo(testRequest))(any[HeaderCarrier])
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.hasCisClient(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.hasCisClient(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).hasCisClient(eqTo(testRequest))(any[HeaderCarrier])
      }
    }

    "hasPayeClient" - {

      val testRequest = HasPayeClientRequest(
        agentDetails = AgentDetails(
          credentialId = "CRED01",
          irAgentId = "123456"
        ),
        employerDetails = EmployerDetails(
          hmrcOfficeNumber = "840",
          schemeReferenceNumber = "MZ00064"
        )
      )

      "must return HasClientResponse with hasClient true when successful" in {
        val testResponse = HasClientResponse(hasClient = true)

        when(mockBackendConnector.hasPayeClient(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.successful(testResponse))

        val result = service.hasPayeClient(testRequest).futureValue

        result mustBe testResponse
        verify(mockBackendConnector, times(1)).hasPayeClient(eqTo(testRequest))(any[HeaderCarrier])
      }

      "must return HasClientResponse with hasClient false when successful" in {
        val testResponse = HasClientResponse(hasClient = false)

        when(mockBackendConnector.hasPayeClient(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.successful(testResponse))

        val result = service.hasPayeClient(testRequest).futureValue

        result mustBe testResponse
        verify(mockBackendConnector, times(1)).hasPayeClient(eqTo(testRequest))(any[HeaderCarrier])
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.hasPayeClient(eqTo(testRequest))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.hasPayeClient(testRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).hasPayeClient(eqTo(testRequest))(any[HeaderCarrier])
      }
    }
  }
}
