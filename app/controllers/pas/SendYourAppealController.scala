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
import models.AppealSubmissionReceipt
import pages.pas.SendYourAppealPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppealSummaryBuilder
import views.html.pas.SendYourAppealView

import scala.concurrent.{ExecutionContext, Future}

class SendYourAppealController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            sessionRepository: SessionRepository,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            appealSummaryBuilder: AppealSummaryBuilder,
                                            view: SendYourAppealView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  // TODO: DTR-8978 - populate this information with iForms response
  private val timestamp = "2021-03-12T09:52:32.765Z"
  private val acknowledgementReference = "58S41GNKDMEQDUZ"

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      appealSummaryBuilder.build(submitted = false).map {
        case Left(redirect) => redirect
        case Right(summaries) => Ok(view(summaries.penaltyDetailsSummaryList, summaries.answersSummaryList))
        } recover {
          case ex =>
            logger.error(s"[SendYourAppealController][onPageLoad] Unexpected failure: ${ex.getMessage}")
            Redirect(routes.JourneyRecoveryController.onPageLoad())
        }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val receipt = AppealSubmissionReceipt(
        acknowledgementReference = acknowledgementReference,
        submittedAt = timestamp
      )

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(SendYourAppealPage, receipt))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.pas.routes.AppealConfirmationController.onPageLoad()) // TODO: DTR-9789 - update to Appeal received page
  }
}
