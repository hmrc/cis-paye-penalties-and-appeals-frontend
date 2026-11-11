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

package controllers

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes.JourneyRecoveryController
import models.GetAppealablePenaltiesRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CisPayePasService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PaginationHelper
import viewmodels.appealablePenalties.{AppealablePenaltiesDisplayRow, AppealablePenaltiesViewModel}
import views.html.AppealablePenaltiesView

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppealablePenaltiesController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             val controllerComponents: MessagesControllerComponents,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             cisPayePasService: CisPayePasService,
                                             view: AppealablePenaltiesView
                                           )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with Logging {

  // TODO: only added CIS org route. Once authentication is set up route will depend on if the user is from CIS/PAYE and if they are a contractor or agent
  private val onwardUrl = appConfig.manageCisAccountOrgUrl

  private val rowsPerPage = 10

  def onPageLoad(page: Option[Int]): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val currentPage = page.getOrElse(1)
    val (startRow, endRow) = getRowNumbers(currentPage)

    request.employerReference match {
      case Some(hmrcOfficeNumber, schemeReferenceNumber) =>
        val req = GetAppealablePenaltiesRequest(hmrcOfficeNumber, schemeReferenceNumber, startRow, endRow)
        
        cisPayePasService.getCisAppealablePenalties(req)
          .map { response =>
            AppealablePenaltiesViewModel.toViewModel(response, currentPage, onwardUrl) match {
              case Some(viewModel) => Ok(view(viewModel))
              case None            => Redirect(controllers.routes.AppealablePenaltiesController.onPageLoad(Some(1)))
            }
          } recover {
              case ex =>
                logger.error(s"[AppealablePenaltiesController][onPageLoad] Unexpected failure: ${ex.getMessage}")
                Redirect(JourneyRecoveryController.onPageLoad())
            }
      case _ =>
        logger.error("[AppealablePenaltiesController][onPageLoad] Could not find employer reference details")
        Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
    }
  }

  private def getRowNumbers(currentPage: Int): (Int, Int) =
    val startRow = if (currentPage == 1) 0 else ((currentPage - 1) * rowsPerPage) + 1
    val endRow = currentPage * rowsPerPage
    (startRow, endRow)

}
