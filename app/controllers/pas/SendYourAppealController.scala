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

package controllers.pas

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import models.{GetAppealablePenaltyItem, GetAppealablePenaltyRequest}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CisPayePasService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckAnswersUtil.redirectOrRender
import viewmodels.appealablePenalties.PenaltyDetailsViewModel
import viewmodels.checkAnswers.*
import views.html.pas.SendYourAppealView

import scala.concurrent.{ExecutionContext, Future}

class SendYourAppealController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            cisPayePasService: CisPayePasService,
                                            view: SendYourAppealView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.penaltyId.map { penaltyId =>
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
          val penaltyDetailsSummaryList = PenaltyDetailsSummaryList(PenaltyDetailsViewModel.from(penaltyDetailsItem))

          val rowResults = Seq(
            Some(WhyYouAreAppealingSummary.row(request.userAnswers)),
            ExcuseEndDateKnownSummary.row(request.userAnswers),
            ExcuseEndDateSummary.row(request.userAnswers),
            CisContractorEndDateSummary.row(request.userAnswers),
            FileReturnDateSummary.row(request.userAnswers),
            AdditionalDetailsSummary.row(request.userAnswers)
          ).flatten

          redirectOrRender(rowResults) match {
            case Left(call) => Redirect(call)
            case Right(summaryList) => Ok(view(penaltyDetailsSummaryList, summaryList))
          }
        } recover {
          case ex =>
            logger.error(s"[SendYourAppealController][onPageLoad] Unexpected failure: ${ex.getMessage}")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
      }.getOrElse {
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
