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

import models.{GetAppealablePenaltiesResponse, GetAppealablePenaltyItem, GetAppealablePenaltyResponse}
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
          GetAppealablePenaltyItem(
            penaltyId = 1L,
            issueDate = LocalDate.of(2026, 1, 1),
            taxPeriodEndDate = LocalDate.of(2026, 4, 1),
            penaltyType = "CIS3",
            penaltyAmount = BigDecimal(100.00)
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
      model.rows.head.penaltyId mustBe 1L
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

    "transform a GetAppealablePenaltyResponse into a formatted display row for each penalty type" in new Fixture {
      val responseRows: Seq[GetAppealablePenaltyItem] = Seq(
        GetAppealablePenaltyItem(
          penaltyId = 1L,
          issueDate = LocalDate.of(2026, 1, 1),
          taxPeriodEndDate = LocalDate.of(2026, 4, 1),
          penaltyType = "CIS3",
          penaltyAmount = BigDecimal(1000.87)
        ),
        GetAppealablePenaltyItem(
          penaltyId = 1L,
          issueDate = LocalDate.of(2026, 1, 1),
          taxPeriodEndDate = LocalDate.of(2026, 4, 1),
          penaltyType = "CIS4",
          penaltyAmount = BigDecimal(1000.87)
        ),
        GetAppealablePenaltyItem(
          penaltyId = 1L,
          issueDate = LocalDate.of(2026, 1, 1),
          taxPeriodEndDate = LocalDate.of(2026, 4, 1),
          penaltyType = "CIS5",
          penaltyAmount = BigDecimal(1000.87)
        ),
        GetAppealablePenaltyItem(
          penaltyId = 1L,
          issueDate = LocalDate.of(2026, 1, 1),
          taxPeriodEndDate = LocalDate.of(2026, 4, 1),
          penaltyType = "CIS6",
          penaltyAmount = BigDecimal(1000.87)
        )
      )

      responseRows.foreach { responseRow =>
        val result: AppealablePenaltiesDisplayRow = AppealablePenaltiesDisplayRow.fromRow(responseRow)

        result.penaltyId mustBe 1L
        result.issueDate mustBe "1 January 2026"
        result.taxPeriodEndDate mustBe "1 April 2026"
        result.penaltyType mustBe messages(s"appealablePenalties.penalty.${responseRow.penaltyType.toLowerCase}")
        result.penaltyAmount mustBe "£1,000.87"
      }
    }

    "throw IllegalStateException when typeOfPenalty is invalid" in new Fixture {
      val emptyRow = GetAppealablePenaltyItem(
        penaltyId = 1L,
        issueDate = LocalDate.of(2026, 1, 1),
        taxPeriodEndDate = LocalDate.of(2026, 4, 1),
        penaltyType = "invalid",
        penaltyAmount = BigDecimal(1000.87)
      )

      val ex: IllegalStateException = intercept[IllegalStateException] {
        AppealablePenaltiesDisplayRow.fromRow(emptyRow)
      }

      ex.getMessage mustBe "[AppealablePenaltiesDisplayRow][fromRow] Invalid penaltyType: invalid"
    }
  }
}

trait Fixture {
  implicit val messages: Messages = stubMessages()
}
