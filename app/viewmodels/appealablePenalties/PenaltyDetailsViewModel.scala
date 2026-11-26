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

import models.GetAppealablePenaltyItem
import play.api.i18n.Messages

import java.time.format.DateTimeFormatter

case class PenaltyDetailsViewModel(
                                          penaltyId: String,
                                          issueDate: String,
                                          taxPeriodEndDate: String,
                                          penaltyType: String,
                                          penaltyAmount: String
                                        )

object PenaltyDetailsViewModel {
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def from(item: GetAppealablePenaltyItem)(implicit messages: Messages): PenaltyDetailsViewModel = {

    val typeOfPenalty = item.penaltyType match {
      case "CIS3" => messages("appealablePenalties.penalty.cis3")
      case "CIS4" => messages("appealablePenalties.penalty.cis4")
      case "CIS5" => messages("appealablePenalties.penalty.cis5")
      case "CIS6" => messages("appealablePenalties.penalty.cis6")
      case other => throw new IllegalStateException(s"[PenaltyDetailsViewModel][from] Invalid penaltyType: $other")
    }

    PenaltyDetailsViewModel(
      penaltyId = item.penaltyId.toString,
      issueDate = item.issueDate.format(dateFormatter),
      taxPeriodEndDate = item.taxPeriodEndDate.format(dateFormatter),
      penaltyType = typeOfPenalty,
      penaltyAmount = f"£${item.penaltyAmount}%,.2f"
    )
  }
}
