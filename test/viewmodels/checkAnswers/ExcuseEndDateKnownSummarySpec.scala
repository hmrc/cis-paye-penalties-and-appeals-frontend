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

package viewmodels.checkAnswers

import base.SpecBase
import models.{CheckMode, WhyYouAreAppealing}
import pages.pas.{ExcuseEndDateKnownPage, WhyYouAreAppealingPage}
import play.api.i18n.Messages
import play.api.test.Helpers.running
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}

class ExcuseEndDateKnownSummarySpec extends SpecBase {

  "ExcuseEndDateKnownSummary" - {

    "when excuse end date known is present" - {

      "must return Some(Row) with 'yes' text and change link" in {

        val userAnswers = emptyUserAnswers
          .set(ExcuseEndDateKnownPage, true).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val row = ExcuseEndDateKnownSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }
          result.key.content.asHtml.toString() mustEqual msgs("excuseEndDateKnown.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual msgs("site.yes")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.pas.routes.ExcuseEndDateKnownController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("excuseEndDateKnown.change.hidden")
        }
      }

      "must return Some(Row) with 'no' text and change link" in {

        val userAnswers = emptyUserAnswers
          .set(ExcuseEndDateKnownPage, false).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val msgs: Messages = messages(application)

          val row = ExcuseEndDateKnownSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

          val result = row match {
            case Row(r) => r
            case _ => fail("Expected Row but got Missing")
          }
          result.key.content.asHtml.toString() mustEqual msgs("excuseEndDateKnown.checkYourAnswersLabel")

          val contentString = result.value.content.asHtml.toString()

          contentString mustEqual msgs("site.no")

          result.actions.get.items.size mustEqual 1
          result.actions.get.items.head.href mustEqual controllers.pas.routes.ExcuseEndDateKnownController.onPageLoad(CheckMode).url
          result.actions.get.items.head.content.asHtml.toString() must include(msgs("site.change"))
          result.actions.get.items.head.visuallyHiddenText.value mustEqual msgs("excuseEndDateKnown.change.hidden")
        }
      }
    }

    "when excuse end date known is absent" - {

      "when reasonable excuse is NoneOfThese" - {

        "must return Some(Missing) with the correct redirect call" in {

          val userAnswers = emptyUserAnswers
            .set(WhyYouAreAppealingPage, WhyYouAreAppealing.NoneOfThese).success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            val result = ExcuseEndDateKnownSummary.row(userAnswers).getOrElse(fail("Failed to get summary list row"))

            result match {
              case Missing(call) =>
                call mustEqual controllers.pas.routes.ExcuseEndDateKnownController.onPageLoad(CheckMode)

              case Row(_) =>
                fail("Expected Missing but got Row")
            }
          }
        }
      }

      "when reasonable excuse is not NoneOfThese" - {

        "must return None" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            val userAnswers = emptyUserAnswers
              .set(WhyYouAreAppealingPage, WhyYouAreAppealing.Contractor).success.value

            ExcuseEndDateKnownSummary.row(userAnswers) mustBe None
          }
        }
      }

      "when reasonable excuse is absent" - {

        "must return None" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            implicit val msgs: Messages = messages(application)

            ExcuseEndDateKnownSummary.row(emptyUserAnswers) mustBe None
          }
        }
      }
    }
  }
}
