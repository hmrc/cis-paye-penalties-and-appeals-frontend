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

package controllers.pas

import base.SpecBase
import forms.pas.WhyYouAreAppealingFormProvider
import models.{CheckMode, NormalMode, UserAnswers, WhyYouAreAppealing}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.pas.{AdditionalDetailsPage, CisContractorEndDatePage, ExcuseEndDateKnownPage, ExcuseEndDatePage, FileReturnDatePage, WhyYouAreAppealingPage}
import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.pas.WhyYouAreAppealingView

import java.time.LocalDate
import scala.concurrent.Future

class WhyYouAreAppealingControllerSpec extends SpecBase with MockitoSugar {

  private lazy val whyYouAreAppealingRoute = routes.WhyYouAreAppealingController.onPageLoad(NormalMode).url
  private lazy val whyYouAreAppealingRouteCheckMode = routes.WhyYouAreAppealingController.onPageLoad(CheckMode).url

  private val formProvider = new WhyYouAreAppealingFormProvider()
  private val form = formProvider()

  def postRequest(route: String, value: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, route)
      .withFormUrlEncodedBody(("value", value))

  def applicationWithSessionRepository(mockSessionRepository: SessionRepository): Application =
    applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
      .build()

  "WhyYouAreAppealing Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whyYouAreAppealingRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhyYouAreAppealingView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(WhyYouAreAppealingPage, WhyYouAreAppealing.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, whyYouAreAppealingRoute)

        val view = application.injector.instanceOf[WhyYouAreAppealingView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(WhyYouAreAppealing.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to excuse end date page when valid data is submitted for an excuse end date reason in NormalMode" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationWithSessionRepository(mockSessionRepository)

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRoute, "3")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ExcuseEndDateController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to excuse end date page when valid data is submitted for an excuse end date reason in CheckMode" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationWithSessionRepository(mockSessionRepository)

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRouteCheckMode, "3")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ExcuseEndDateController.onPageLoad(CheckMode).url
      }
    }

    "must redirect to excuse end date known page when valid data is submitted for reason 17" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationWithSessionRepository(mockSessionRepository)

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRoute, "17")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ExcuseEndDateKnownController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to group 5 page (CisContractorEndDateController) when valid data is submitted for reason 20" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationWithSessionRepository(mockSessionRepository)

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRoute, "20")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CisContractorEndDateController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to group 6 page (FileReturnDateController) when valid data is submitted for reason 5" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationWithSessionRepository(mockSessionRepository)

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRoute, "5")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.FileReturnDateController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to group 7 page (SendYourAppealController) when valid data is submitted for reason 18" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationWithSessionRepository(mockSessionRepository)

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRoute, "18")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SendYourAppealController.onPageLoad().url
      }
    }

    "must clear answers when WhyYouAreAppealing answer was previously another option than selected" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId)
        .set(WhyYouAreAppealingPage, WhyYouAreAppealing.Victim).success.value
        .set(ExcuseEndDateKnownPage, true).success.value
        .set(ExcuseEndDatePage, LocalDate.of(2022, 10, 26)).success.value
        .set(CisContractorEndDatePage, LocalDate.of(2022, 10, 26)).success.value
        .set(FileReturnDatePage, LocalDate.of(2022, 10, 26)).success.value
        .set(AdditionalDetailsPage, "Some details").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRoute, WhyYouAreAppealing.SeriousIllness.toString)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ExcuseEndDateController.onPageLoad(NormalMode).url
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue.get(ExcuseEndDateKnownPage).isDefined mustBe false
        uaCaptor.getValue.get(ExcuseEndDatePage).isDefined mustBe false
        uaCaptor.getValue.get(CisContractorEndDatePage).isDefined mustBe false
        uaCaptor.getValue.get(FileReturnDatePage).isDefined mustBe false
        uaCaptor.getValue.get(AdditionalDetailsPage).isDefined mustBe false
      }
    }

    "must not clear answers when WhyYouAreAppealing answer was previously the same option as selected" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers(userAnswersId)
        .set(WhyYouAreAppealingPage, WhyYouAreAppealing.Victim).success.value
        .set(ExcuseEndDateKnownPage, true).success.value
        .set(ExcuseEndDatePage, LocalDate.of(2022, 10, 26)).success.value
        .set(CisContractorEndDatePage, LocalDate.of(2022, 10, 26)).success.value
        .set(FileReturnDatePage, LocalDate.of(2022, 10, 26)).success.value
        .set(AdditionalDetailsPage, "Some details").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRoute, WhyYouAreAppealing.Victim.toString)).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ExcuseEndDateController.onPageLoad(NormalMode).url
        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture())
        uaCaptor.getValue.get(ExcuseEndDateKnownPage).isDefined mustBe true
        uaCaptor.getValue.get(ExcuseEndDatePage).isDefined mustBe true
        uaCaptor.getValue.get(CisContractorEndDatePage).isDefined mustBe true
        uaCaptor.getValue.get(FileReturnDatePage).isDefined mustBe true
        uaCaptor.getValue.get(AdditionalDetailsPage).isDefined mustBe true
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, whyYouAreAppealingRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[WhyYouAreAppealingView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, whyYouAreAppealingRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest(whyYouAreAppealingRoute, WhyYouAreAppealing.values.head.toString)).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
