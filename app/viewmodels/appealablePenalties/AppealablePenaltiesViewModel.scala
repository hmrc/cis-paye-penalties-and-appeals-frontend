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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class AppealablePenaltiesViewModel(
                                       total: Int,
                                       rows : List[AppealablePenaltiesViewModelRow]
                                       )

case class AppealablePenaltiesViewModelRow(
                                            penaltyId        : Option[Long],
                                            issueDate        : Option[LocalDate],
                                            taxPeriodEndDate : Option[LocalDate],
                                            typeOfPenalty    : Option[String],
                                            amount           : Option[BigDecimal]
                                          )

case class AppealablePenaltiesDisplayRow(
                                          penaltyId: String,
                                          issueDate: String,
                                          taxPeriodEndDate: String,
                                          typeOfPenalty: String,
                                          amount: String
                                        )

object AppealablePenaltiesDisplayRow {
  def fromRow(row: AppealablePenaltiesViewModelRow): AppealablePenaltiesDisplayRow = {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    AppealablePenaltiesDisplayRow(
      penaltyId        = row.penaltyId.map(_.toString).getOrElse(""),
      issueDate        = row.issueDate.map(_.format(dateFormatter)).getOrElse(""),
      taxPeriodEndDate = row.taxPeriodEndDate.map(_.format(dateFormatter)).getOrElse(""),
      typeOfPenalty    = row.typeOfPenalty.getOrElse(""),
      amount           = row.amount.map(a => f"£$a%.2f").getOrElse("")
    )
  }
}