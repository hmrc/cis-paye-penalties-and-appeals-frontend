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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import models.{GetAppealablePenaltiesResponse, GetAppealablePenaltyResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.CisPayePasService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.appealablePenalties.AppealablePenaltiesViewModel
import views.html.AppealablePenaltiesView

import java.time.LocalDate
import scala.concurrent.Future

class AppealablePenaltiesControllerSpec extends SpecBase {

  "AppealablePenalties Controller" - {

    "must return OK and the correct view for a GET with no returns" in new Fixture {
      val response = GetAppealablePenaltiesResponse(
        total = 0,
        appealablePenalties = List.empty
      )

      val viewModel: AppealablePenaltiesViewModel =
        AppealablePenaltiesViewModel
          .toViewModel(response, 1, cisOrgUrl)(messages(application)).value

      when(mockService.getCisAppealablePenalties(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      running(application) {
        val request = FakeRequest(GET, routes.AppealablePenaltiesController.onPageLoad(Some(1)).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AppealablePenaltiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        verify(mockService, times(1)).getCisAppealablePenalties(any())(any[HeaderCarrier])
      }
    }

    "must return OK and the correct view for a GET with single page returns" in new Fixture {
      val numOfRows = 7

      val response = GetAppealablePenaltiesResponse(
        total = numOfRows,
        appealablePenalties = generateRows(numOfRows)
      )

      val viewModel: AppealablePenaltiesViewModel =
        AppealablePenaltiesViewModel
          .toViewModel(response, 1, cisOrgUrl)(messages(application)).value

      when(mockService.getCisAppealablePenalties(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      running(application) {
        val request = FakeRequest(GET, routes.AppealablePenaltiesController.onPageLoad(Some(1)).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AppealablePenaltiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        verify(mockService, times(1)).getCisAppealablePenalties(any())(any[HeaderCarrier])
      }
    }

    "must return OK and the correct view for a GET with multiple page returns (page 1)" in new Fixture {
      val numOfRows = 23

      val response = GetAppealablePenaltiesResponse(
        total = numOfRows,
        appealablePenalties = generateRows(10)
      )

      val viewModel: AppealablePenaltiesViewModel =
        AppealablePenaltiesViewModel
          .toViewModel(response, 1, cisOrgUrl)(messages(application)).value

      when(mockService.getCisAppealablePenalties(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      running(application) {
        val request = FakeRequest(GET, routes.AppealablePenaltiesController.onPageLoad(Some(1)).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AppealablePenaltiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        verify(mockService, times(1)).getCisAppealablePenalties(any())(any[HeaderCarrier])
      }
    }

    "must return OK and the correct view for a GET with multiple page returns (page 2)" in new Fixture {
      val numOfRows = 23

      val response = GetAppealablePenaltiesResponse(
        total = numOfRows,
        appealablePenalties = generateRows(10)
      )

      val viewModel: AppealablePenaltiesViewModel =
        AppealablePenaltiesViewModel
          .toViewModel(response, 2, cisOrgUrl)(messages(application)).value

      when(mockService.getCisAppealablePenalties(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      running(application) {
        val request = FakeRequest(GET, routes.AppealablePenaltiesController.onPageLoad(Some(2)).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[AppealablePenaltiesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        verify(mockService, times(1)).getCisAppealablePenalties(any())(any[HeaderCarrier])
      }
    }

    "must redirect to page 1 when pagination index is out of scope" in new Fixture {
      val invalidPageIndex = 999
      val numOfRows = 12

      val response = GetAppealablePenaltiesResponse(
        total = numOfRows,
        appealablePenalties = generateRows(10)
      )

      val viewModel: AppealablePenaltiesViewModel =
        AppealablePenaltiesViewModel
          .toViewModel(response, 1, cisOrgUrl)(messages(application)).value

      when(mockService.getCisAppealablePenalties(any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(response))

      running(application) {
        val request = FakeRequest(GET, routes.AppealablePenaltiesController.onPageLoad(Some(invalidPageIndex)).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AppealablePenaltiesController.onPageLoad(Some(1)).url
        verify(mockService, times(1)).getCisAppealablePenalties(any())(any[HeaderCarrier])
      }
    }

    "must redirect to JourneyRecovery when service returns an error" in new Fixture {
      when(mockService.getCisAppealablePenalties(any())(any[HeaderCarrier]))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      running(application) {
        val request = FakeRequest(GET, routes.AppealablePenaltiesController.onPageLoad(Some(1)).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        verify(mockService, times(1)).getCisAppealablePenalties(any())(any[HeaderCarrier])
      }
    }
  }

  trait Fixture {
    val mockService: CisPayePasService = mock[CisPayePasService]

    lazy val application: Application = {
      applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[CisPayePasService].toInstance(mockService))
        .build()
    }

    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
    val cisOrgUrl: String = appConfig.manageCisAccountOrgUrl

    def generateRows(num: Int): List[GetAppealablePenaltyResponse] =
      (0 to num).toList.map { index =>
        GetAppealablePenaltyResponse(
          penaltyId = Some(index),
          issueDate = Some(LocalDate.of(2025, 1, 1)),
          taxPeriodEndDate = Some(LocalDate.of(2025, 4, 1)),
          penaltyType = Some("CIS3"),
          penaltyAmount = Some(BigDecimal(100.00))
        )
      }
  }
}
