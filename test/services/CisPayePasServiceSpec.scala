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
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import connectors.CisPayePasConnector
import models.{CreateCisAppealRequest, CreateCisAppealResponse, GetCisAppealablePenaltyRequest, GetCisAppealablePenaltyResponse}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val request: FakeRequest[_] = FakeRequest()

  private val mockBackendConnector = mock[CisPayePasConnector]

  private val service = new CisPayePasService(mockBackendConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBackendConnector)
  }

  "CisPayePasService" - {

    "createCisAppeal" - {

      val createCisAppealRequest = CreateCisAppealRequest(1L)
      val createCisAppealResponse = CreateCisAppealResponse(true)

      "must return CreateCisAppealResponse when penaltyId provided" in {
        when(mockBackendConnector.createCisAppeal(eqTo(createCisAppealRequest))(any(), any()))
          .thenReturn(Future.successful(createCisAppealResponse))

        val result = service.createCisAppeal(createCisAppealRequest.penaltyId).futureValue

        result mustBe createCisAppealResponse
        result.created mustBe true
        verify(mockBackendConnector, times(1)).createCisAppeal(eqTo(createCisAppealRequest))(any(), any())
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.createCisAppeal(eqTo(createCisAppealRequest))(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.createCisAppeal(createCisAppealRequest.penaltyId).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).createCisAppeal(eqTo(createCisAppealRequest))(any(), any())
      }
    }

    "getCisAppealablePenalty" - {

      val getCisAppealablePenaltyRequest = GetCisAppealablePenaltyRequest(
        penaltyId = 1L,
        hmrcOfficeNumber = "840123",
        schemeReferenceNumber = "098345"
      )

      val getCisAppealablePenaltyResponse = GetCisAppealablePenaltyResponse(
        uniqueId = None,
        issueDate = Some(LocalDate.of(2023, 1, 1)),
        taxPeriodEndDate = Some(LocalDate.of(2023, 4, 1)),
        penaltyType = Some("CIS003"),
        penaltyAmount = Some(BigDecimal("999.23"))
      )

      "must return GetCisAppealablePenaltyResponse when request provided" in {
        when(mockBackendConnector.getCisAppealablePenalty(eqTo(getCisAppealablePenaltyRequest))(any(), any()))
          .thenReturn(Future.successful(getCisAppealablePenaltyResponse))

        val result = service.getCisAppealablePenalty(getCisAppealablePenaltyRequest).futureValue

        result mustBe getCisAppealablePenaltyResponse
        result.issueDate mustBe Some(LocalDate.of(2023, 1, 1))
        verify(mockBackendConnector, times(1)).getCisAppealablePenalty(eqTo(getCisAppealablePenaltyRequest))(any(), any())
      }

      "must propagate connector failure" in {
        when(mockBackendConnector.getCisAppealablePenalty(eqTo(getCisAppealablePenaltyRequest))(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("Connector failed")))

        whenReady(service.getCisAppealablePenalty(getCisAppealablePenaltyRequest).failed) { exception =>
          exception mustBe a[RuntimeException]
          exception.getMessage mustBe "Connector failed"
        }
        verify(mockBackendConnector, times(1)).getCisAppealablePenalty(eqTo(getCisAppealablePenaltyRequest))(any(), any())
      }
    }
  }
}
