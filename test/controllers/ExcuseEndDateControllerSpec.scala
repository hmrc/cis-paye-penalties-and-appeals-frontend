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
import forms.ExcuseEndDateFormProvider
import models.{CheckMode, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.ExcuseEndDatePage
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.TimeMachine
import views.html.ExcuseEndDateView

import java.time.LocalDate
import scala.concurrent.Future

class ExcuseEndDateControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("Get", "/foo")

  private implicit val msgs: Messages = stubMessages()
  private val timeMachine = TimeMachine()
  private val formProvider = new ExcuseEndDateFormProvider(timeMachine)
  private val form: Form[LocalDate] = formProvider()

  lazy private val excuseEndDateRoute = routes.ExcuseEndDateController.onPageLoad(NormalMode).url
  lazy private val excuseEndDateRouteCheckMode = routes.ExcuseEndDateController.onPageLoad(CheckMode).url

  private val validAnswer = timeMachine.today

  def postRequest(route: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, route)
      .withFormUrlEncodedBody(
        "value.day" -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year" -> validAnswer.getYear.toString
      )

  "ExcuseEndDate Controller" - {

    ".onPageLoad" - {

      "must return OK and the correct view for a GET" in {
        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, excuseEndDateRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ExcuseEndDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val userAnswers = emptyUserAnswers.set(ExcuseEndDatePage, validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, excuseEndDateRoute)

          val view = application.injector.instanceOf[ExcuseEndDateView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, excuseEndDateRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }

    ".onSubmit" - {

      "must redirect to additional details page when valid data is submitted in NormalMode" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val result = route(application, postRequest(excuseEndDateRoute)).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url // TODO: DTR-9783 - update to correct URL
        }
      }

      "must redirect to send your appeal page when valid data is submitted in CheckMode" in {
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val result = route(application, postRequest(excuseEndDateRouteCheckMode)).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url // TODO: DTR-9786 - update to correct URL
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request =
          FakeRequest(POST, excuseEndDateRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        running(application) {
          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[ExcuseEndDateView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {

          val result = route(application, postRequest(excuseEndDateRoute)).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
