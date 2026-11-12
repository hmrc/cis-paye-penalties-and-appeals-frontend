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
import models.NormalMode
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import utils.TimeMachine
import views.html.{BeforeYouStartView, ExcuseEndDateView}

import java.time.LocalDate

class ExcuseEndDateControllerSpec extends SpecBase with MockitoSugar {

  "ExcuseEndDate Controller" - {

    "must return OK and the correct view for a GET" in new Setup {

      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.ExcuseEndDateController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ExcuseEndDateView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }
  }

  trait Setup {
    lazy val excuseEndDateRoute: Call = controllers.routes.ExcuseEndDateController.onPageLoad(NormalMode)
    private implicit val msgs: Messages = stubMessages()
    private val timeMachine = TimeMachine()
    private val formProvider = new ExcuseEndDateFormProvider(timeMachine)
    val form: Form[LocalDate] = formProvider()
  }
}
