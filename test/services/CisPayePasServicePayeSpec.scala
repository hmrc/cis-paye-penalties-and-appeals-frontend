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
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class CisPayePasServicePayeSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val mockBackendConnector = mock[CisPayePasConnector]

  private val service = new CisPayePasService(mockBackendConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBackendConnector)
  }

  "CisPayePasService" - {

    "createPayeAppeal" - {

      val createPayeAppealRequest = CreatePayeAppealRequest(1L)
      val createPayeAppealResponse = CreatePayeAppealResponse(true)

      "must return CreatePayeAppealResponse when penaltyId provided" in {
        when(mockBackendConnector.createPayeAppeal(eqTo(createPayeAppealRequest))(any()))
          .thenReturn(Future.successful(createPayeAppealResponse))

        val result = service.createPayeAppeal(createPayeAppealRequest.penaltyId).futureValue

        result mustBe createPayeAppealResponse
        result.created mustBe true
        verify(mockBackendConnector, times(1)).createPayeAppeal(eqTo(createPayeAppealRequest))(any())
      }

      "must propagate failures from the connector" in {
        val boom = UpstreamErrorResponse("Service unavailable", 503)

        when(mockBackendConnector.createPayeAppeal(eqTo(createPayeAppealRequest))(any()))
          .thenReturn(Future.failed(boom))

        service.createPayeAppeal(createPayeAppealRequest.penaltyId).failed.futureValue mustBe boom
      }
    }
  }
}
