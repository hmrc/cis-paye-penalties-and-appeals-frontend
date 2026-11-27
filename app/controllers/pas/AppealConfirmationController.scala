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
import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes
import pages.pas.SendYourAppealPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppealSummaryBuilder
import utils.DateTimeFormats.formatTimestamp
import views.html.pas.AppealConfirmationView

import scala.concurrent.{ExecutionContext, Future}

class AppealConfirmationController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          appealSummaryBuilder: AppealSummaryBuilder,
                                          view: AppealConfirmationView
                                        )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging {

  // TODO: only added CIS org route. Once authentication is set up route will depend on if the user is from CIS/PAYE and if they are a contractor or agent
  private val onwardUrl = appConfig.manageCisAccountOrgUrl

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(SendYourAppealPage) match {
        case Some(receipt) =>
          val formattedTimestamp = formatTimestamp(receipt.submittedAt)

          appealSummaryBuilder.build(submitted = true).map {
            case Left(redirect) => redirect
            case Right(summaries) => Ok(view(
              summaries.penaltyDetailsSummaryList,
              summaries.answersSummaryList,
              formattedTimestamp,
              receipt.acknowledgementReference,
              onwardUrl
            ))
          } recover {
            case ex =>
              logger.error(s"[AppealConfirmationController][onPageLoad] Unexpected failure: ${ex.getMessage}")
              Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
        case None =>
          logger.error(s"[AppealConfirmationController][onPageLoad] Could not retrieve submission details for request: $request")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
