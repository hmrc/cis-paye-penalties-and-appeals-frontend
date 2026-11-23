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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import models.EmployerReference
import play.api.mvc.{Action, AnyContent, BodyParsers, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PayeIdentifierActionSpec extends SpecBase {

  class Harness(authAction: PayeIdentifierAction) {
    var seen: Option[EmployerReference] = None

    def onPageLoad(): Action[AnyContent] = authAction { request =>
      seen = Some(request.employerReference)
      Results.Ok
    }
  }

  private def irPaye(number: String = "754", reference: String = "EZ00100", state: String = "Activated") =
    Enrolment("IR-PAYE", Seq(EnrolmentIdentifier("TaxOfficeNumber", number), EnrolmentIdentifier("TaxOfficeReference", reference)), state)

  private def run(connector: AuthConnector)(test: (FrontendAppConfig, Harness, Future[Result]) => Any): Unit = {
    val application = applicationBuilder(userAnswers = None).build()

    running(application) {
      val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
      val appConfig   = application.injector.instanceOf[FrontendAppConfig]
      val harness     = new Harness(new AuthenticatedPayeIdentifierAction(connector, appConfig, bodyParsers))

      test(appConfig, harness, harness.onPageLoad()(FakeRequest()))
    }
  }

  private def organisation(enrolments: Enrolment*) =
    new FakeSucceedingAuthConnector(Some("id"), enrolments.toSet, Some(AffinityGroup.Organisation))

  private def mustBeUnauthorised(result: Future[Result]): Unit = {
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
  }

  "Paye Identifier Action" - {

    "when the user hasn't logged in" - {

      "must redirect the user to log in" in {
        run(new FakeFailingAuthConnector(new MissingBearerToken)) { (appConfig, _, result) =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "when the user's session has expired" - {

      "must redirect the user to log in" in {
        run(new FakeFailingAuthConnector(new BearerTokenExpired)) { (appConfig, _, result) =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "when authorisation fails" - {

      "must redirect the user to the unauthorised page" in {
        run(new FakeFailingAuthConnector(new InsufficientEnrolments)) { (_, _, result) => mustBeUnauthorised(result) }
      }
    }

    "when the user is an organisation without an IR-PAYE enrolment" - {

      "must redirect the user to the unauthorised page" in {
        run(organisation()) { (_, _, result) => mustBeUnauthorised(result) }
      }
    }

    "when the user only has an HMRC-CIS-ORG enrolment" - {

      "must redirect the user to the unauthorised page" in {
        val cisOrg = Enrolment("HMRC-CIS-ORG",
          Seq(EnrolmentIdentifier("TaxOfficeNumber", "754"), EnrolmentIdentifier("TaxOfficeReference", "EZ00100")), "Activated")
        run(organisation(cisOrg)) { (_, _, result) => mustBeUnauthorised(result) }
      }
    }

    "when the IR-PAYE enrolment is not activated" - {

      "must redirect the user to the unauthorised page" in {
        run(organisation(irPaye(state = "Inactive"))) { (_, _, result) => mustBeUnauthorised(result) }
      }
    }

    "when the tax office number is not 3 digits" - {

      "must redirect the user to the unauthorised page" in {
        run(organisation(irPaye(number = "75"))) { (_, _, result) => mustBeUnauthorised(result) }
      }
    }

    "when the tax office reference is blank" - {

      "must redirect the user to the unauthorised page" in {
        run(organisation(irPaye(reference = " "))) { (_, _, result) => mustBeUnauthorised(result) }
      }
    }

    "when the user is an agent" - {

      "must redirect the user to the unauthorised page" in {
        val agent = Enrolment("IR-PAYE-AGENT", Seq(EnrolmentIdentifier("IRAgentReference", "ABC123")), "Activated")
        run(new FakeSucceedingAuthConnector(Some("id"), Set(agent, irPaye()), Some(AffinityGroup.Agent))) { (_, _, result) =>
          mustBeUnauthorised(result)
        }
      }
    }

    "when the internal id is missing" - {

      "must redirect the user to the unauthorised page" in {
        run(new FakeSucceedingAuthConnector(None, Set(irPaye()), Some(AffinityGroup.Organisation))) { (_, _, result) =>
          mustBeUnauthorised(result)
        }
      }
    }

    "when the user is an organisation with an activated IR-PAYE enrolment" - {

      "must run the block with the employer reference" in {
        run(organisation(irPaye())) { (_, harness, result) =>
          status(result) mustBe OK
          harness.seen mustBe Some(EmployerReference("754", "EZ00100"))
        }
      }
    }
  }
}
