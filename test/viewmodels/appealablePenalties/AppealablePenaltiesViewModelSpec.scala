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

package viewmodels.appealablePenalties

import models.{GetAppealablePenaltiesResponse, GetAppealablePenaltyResponse}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class AppealablePenaltiesViewModelSpec extends AnyWordSpec with Matchers {

  "AppealablePenaltiesViewModel.toViewModel" should {

    "transform a GetAppealablePenaltiesResponse into a view model" in new Fixture {
      val response = GetAppealablePenaltiesResponse(
        total = 1,
        appealablePenalties = List(
          GetAppealablePenaltyResponse(
            penaltyId = Some(1L),
            issueDate = Some(LocalDate.of(2026, 1, 1)),
            taxPeriodEndDate = Some(LocalDate.of(2026, 4, 1)),
            penaltyType = Some("CIS3"),
            penaltyAmount = Some(BigDecimal(100.00))
          )
        )
      )

      val result: Option[AppealablePenaltiesViewModel] = AppealablePenaltiesViewModel.toViewModel(
        response = response,
        currentPage = 1,
        onwardUrl = "#"
      )(messages)
      val model: AppealablePenaltiesViewModel = result.get

      result mustBe defined

      model.total mustBe 1
      model.rows.head.penaltyId mustBe "1"
      model.noticesUrl mustBe None
      model.onwardUrl mustBe "#"
    }

    "output noticesUrl when total is 0" in new Fixture {
      val response = GetAppealablePenaltiesResponse(
        total = 0,
        appealablePenalties = List.empty
      )

      val result: Option[AppealablePenaltiesViewModel] = AppealablePenaltiesViewModel.toViewModel(
        response = response,
        currentPage = 1,
        onwardUrl = "#"
      )(messages)
      val model: AppealablePenaltiesViewModel = result.get

      result mustBe defined

      model.total mustBe 0
      model.noticesUrl mustBe Some("#")
    }
  }

  "AppealablePenaltiesDisplayRow.fromRow" should {

    "transform a GetAppealablePenaltyResponse into a formatted display row" in new Fixture {
      val responseRow = GetAppealablePenaltyResponse(
        penaltyId = Some(1L),
        issueDate = Some(LocalDate.of(2026, 1, 1)),
        taxPeriodEndDate = Some(LocalDate.of(2026, 4, 1)),
        penaltyType = Some("CIS3"),
        penaltyAmount = Some(BigDecimal(1000.87))
      )

      val result: AppealablePenaltiesDisplayRow = AppealablePenaltiesDisplayRow.fromRow(responseRow)

      result.penaltyId mustBe "1"
      result.issueDate mustBe "1 January 2026"
      result.taxPeriodEndDate mustBe "1 April 2026"
      result.penaltyType mustBe messages("appealablePenalties.penalty.cis3")
      result.penaltyAmount mustBe "£1,000.87"
    }

    "parse missing optional fields using fallback blanks" in new Fixture {
      val emptyRow = GetAppealablePenaltyResponse(
        penaltyId = None,
        issueDate = None,
        taxPeriodEndDate = None,
        penaltyType = None,
        penaltyAmount = None
      )

      val result: AppealablePenaltiesDisplayRow = AppealablePenaltiesDisplayRow.fromRow(emptyRow)

      result.penaltyId mustBe ""
      result.issueDate mustBe ""
      result.taxPeriodEndDate mustBe ""
      result.penaltyType mustBe ""
      result.penaltyAmount mustBe ""
    }
  }

}

trait Fixture {
  implicit val messages: Messages = stubMessages()
}
