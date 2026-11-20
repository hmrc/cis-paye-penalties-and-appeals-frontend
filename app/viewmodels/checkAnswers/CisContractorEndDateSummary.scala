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

import controllers.routes
import models.{CheckMode, UserAnswers, WhyYouAreAppealing}
import pages.{CisContractorEndDatePage, WhyYouAreAppealingPage}
import play.api.i18n.Messages
import utils.DateTimeFormats.dateTimeFormat
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.{Missing, Row}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CisContractorEndDateSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryRowResult] = {
    val changeRoute = routes.CisContractorEndDateController.onPageLoad(CheckMode)
    
    answers.get(CisContractorEndDatePage).map { date =>
      Row(
        SummaryListRowViewModel(
          key     = "cisContractorEndDate.checkYourAnswersLabel",
          value   = ValueViewModel(date.format(dateTimeFormat()(messages.lang))),
          actions = Seq(
            ActionItemViewModel("site.change", changeRoute.url)
              .withVisuallyHiddenText(messages("cisContractorEndDate.change.hidden"))
          )
        )
      )
    }.orElse {
      answers.get(WhyYouAreAppealingPage) match {
        case Some(reason) if reason == WhyYouAreAppealing.NoLongerCis => Some(Missing(changeRoute))
        case _ => None
      }
    }
  }
}
