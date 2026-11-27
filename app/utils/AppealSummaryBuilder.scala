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

package utils

import controllers.routes
import models.requests.DataRequest
import models.{GetAppealablePenaltyItem, GetAppealablePenaltyRequest}
import play.api.Logging
import play.api.i18n.Messages
import play.api.mvc.*
import play.api.mvc.Results.Redirect
import services.CisPayePasService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.HeaderCarrier
import utils.CheckAnswersUtil.redirectOrRender
import viewmodels.appealablePenalties.PenaltyDetailsViewModel
import viewmodels.checkAnswers.*
import viewmodels.checkAnswers.summary.SummaryRowResult
import viewmodels.checkAnswers.summary.SummaryRowResult.Row
import viewmodels.govuk.all.SummaryListViewModel

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class AppealSummaries(
                            penaltyDetailsSummaryList: SummaryList,
                            answersSummaryList: SummaryList
                          )

@Singleton
class AppealSummaryBuilder @Inject()(cisPayePasService: CisPayePasService)(implicit ec: ExecutionContext) extends Logging {

  def build(submitted: Boolean)(implicit request: DataRequest[_], messages: Messages, hc: HeaderCarrier): Future[Either[Result, AppealSummaries]] = {
    request.userAnswers.penaltyId.map { penaltyId =>
      val rowResults = getRowResults

      validateRows(submitted, rowResults) match {
        case Left(result) => Future.successful(Left(result))
        case Right(summaryList) =>
          getPenaltyDetailsSummaryList(penaltyId).map { penaltyDetails =>
            Right(AppealSummaries(penaltyDetails, summaryList))
          }
      }
    }.getOrElse {
      logger.info(s"[AppealSummaryBuilder][build] no penalty ID in user answers")
      Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
    }
  }

  private def validateRows(submitted: Boolean, rowResults: Seq[SummaryRowResult]): Either[Result, SummaryList] =
    if(submitted) {
      val hasMissingRows = rowResults.exists {
        case _: Row => false
        case _ => true
      }

      if (hasMissingRows) {
        logger.info("[AppealSummaryBuilder][buildSubmittedSummaries] UserAnswers has missing row")
        Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      } else {
        val rows = rowResults.collect { case Row(r) => r.copy(actions = None) }
        Right(SummaryListViewModel(rows))
      }
    } else {
      redirectOrRender(rowResults) match {
        case Left(call) => Left(Redirect(call))
        case Right(summaryList) => Right(summaryList)
      }
    }

  private def getPenaltyDetailsSummaryList(penaltyId: Long)(implicit request: DataRequest[_], messages: Messages, hc: HeaderCarrier): Future[SummaryList] =
    val getAppealablePenaltyRequest = GetAppealablePenaltyRequest(
      penaltyId = penaltyId,
      hmrcOfficeNumber = request.employerReference.hmrcOfficeNumber,
      schemeReferenceNumber = request.employerReference.schemeReferenceNumber
    )
    cisPayePasService.getCisAppealablePenalty(getAppealablePenaltyRequest).map { response =>
      val penaltyDetailsItem = GetAppealablePenaltyItem(
        penaltyId = penaltyId,
        issueDate = response.issueDate,
        taxPeriodEndDate = response.taxPeriodEndDate,
        penaltyType = response.penaltyType,
        penaltyAmount = response.penaltyAmount
      )
      PenaltyDetailsSummaryList(PenaltyDetailsViewModel.from(penaltyDetailsItem))
    }

  private def getRowResults(implicit request: DataRequest[_], messages: Messages): Seq[SummaryRowResult] =
    Seq(
      Some(WhyYouAreAppealingSummary.row(request.userAnswers)),
      ExcuseEndDateKnownSummary.row(request.userAnswers),
      ExcuseEndDateSummary.row(request.userAnswers),
      CisContractorEndDateSummary.row(request.userAnswers),
      FileReturnDateSummary.row(request.userAnswers),
      AdditionalDetailsSummary.row(request.userAnswers)
    ).flatten

}
