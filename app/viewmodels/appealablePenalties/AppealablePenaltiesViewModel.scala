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

import controllers.pas.routes.BeforeYouStartController
import models.{GetAppealablePenaltiesResponse, GetAppealablePenaltyItem}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.Pagination
import utils.PaginationHelper

import java.time.format.DateTimeFormatter

case class AppealablePenaltiesViewModel(
                                       total: Int,
                                       rows : List[AppealablePenaltiesDisplayRow],
                                       paginationComponent: Option[Pagination],
                                       paginationText: Option[String],
                                       noticesUrl: Option[String] = None,
                                       onwardUrl: String
                                       )

object AppealablePenaltiesViewModel extends PaginationHelper {
  
  def toViewModel(
                   response: GetAppealablePenaltiesResponse,
                   currentPage: Int,
                   onwardUrl: String
                 )(implicit messages: Messages): Option[AppealablePenaltiesViewModel] = {
    val total = response.total
    val rows = response.appealablePenalties.map(AppealablePenaltiesDisplayRow.fromRow)

    val pagination = getPaginationWithInfoText(
      rows = rows,
      totalRowCount = total,
      paginationIndex = Some(currentPage),
      urlSelector = (p: Int) => controllers.pas.routes.AppealablePenaltiesController.onPageLoad(Some(p)).url
    )
    
    pagination match {
      case Some((_, paginationComponent, infoText)) =>
        Some(AppealablePenaltiesViewModel(
          total = total,
          rows  = rows,
          paginationComponent = paginationComponent,
          paginationText = infoText,
          noticesUrl = Option.when(total == 0)("#"),
          onwardUrl = onwardUrl
        ))
      case _ =>
        None
    }
  }
}

case class AppealablePenaltiesDisplayRow(
                                          penaltyId: Long,
                                          issueDate: String,
                                          taxPeriodEndDate: String,
                                          penaltyType: String,
                                          penaltyAmount: String,
                                          redirectUrl: String
                                        )

object AppealablePenaltiesDisplayRow {
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def fromRow(row: GetAppealablePenaltyItem)(implicit messages: Messages): AppealablePenaltiesDisplayRow = {

    val typeOfPenalty = row.penaltyType match {
      case "CIS3" => messages("appealablePenalties.penalty.cis3")
      case "CIS4" => messages("appealablePenalties.penalty.cis4")
      case "CIS5" => messages("appealablePenalties.penalty.cis5")
      case "CIS6" => messages("appealablePenalties.penalty.cis6")
      case other  => throw new IllegalStateException(s"[AppealablePenaltiesDisplayRow][fromRow] Invalid penaltyType: $other")
    }

    AppealablePenaltiesDisplayRow(
      penaltyId        = row.penaltyId,
      issueDate        = row.issueDate.format(dateFormatter),
      taxPeriodEndDate = row.taxPeriodEndDate.format(dateFormatter),
      penaltyType      = typeOfPenalty,
      penaltyAmount    = f"£${row.penaltyAmount}%,.2f",
      redirectUrl      = BeforeYouStartController.onPageLoad(row.penaltyId).url
    )
  }
}
