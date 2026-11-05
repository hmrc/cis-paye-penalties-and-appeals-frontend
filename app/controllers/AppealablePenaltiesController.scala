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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import controllers.routes.JourneyRecoveryController
import models.GetAppealablePenaltiesRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CisPayePasService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PaginationHelper
import viewmodels.appealablePenalties.AppealablePenaltiesDisplayRow
import views.html.AppealablePenaltiesView

import javax.inject.*
import scala.concurrent.ExecutionContext

@Singleton
class AppealablePenaltiesController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             val controllerComponents: MessagesControllerComponents,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             cisPayePasService: CisPayePasService,
                                             view: AppealablePenaltiesView
                                           )(implicit ec: ExecutionContext, appConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport with PaginationHelper with Logging {
  // TODO: remove once authentication set up
  private val hmrcOfficeNumber = "100"
  private val schemeReferenceNumber = "CD456"

  private val rowsPerPage = 10

  // TODO: only added CIS org route. Once authentication is set up route will depend on if the user is from CIS/PAYE and if they are a contractor or agent
  private val onwardUrl = appConfig.manageCisAccountOrgUrl
  
  def onPageLoad(page: Option[Int]): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    val currentPage = page.getOrElse(1)

    val startRow = if(currentPage == 1) 0 else ((currentPage - 1) * rowsPerPage) + 1
    val endRow = currentPage * rowsPerPage

    val request = GetAppealablePenaltiesRequest(
      hmrcOfficeNumber = hmrcOfficeNumber, schemeReferenceNumber = schemeReferenceNumber, startRow = startRow, endRow = endRow
    )
    
    cisPayePasService.getCisAppealablePenalties(request)
      .map { viewModel =>
        logger.info(s"[AppealablePenaltiesController][onPageLoad] - render page")
        val total = viewModel.total
        val rows = viewModel.rows.map(AppealablePenaltiesDisplayRow.fromRow)

        val pagination = getPaginationWithInfoText(
          rows = rows,
          totalRowCount = total,
          paginationIndex = Some(currentPage),
          urlSelector = (p: Int) => controllers.routes.AppealablePenaltiesController.onPageLoad(Some(p)).url
        )

        pagination match {
          case Some((_, paginationComponent, infoText)) =>
            Ok(view(total, rows, paginationComponent, infoText, onwardUrl))

          case None =>
            Redirect(controllers.routes.AppealablePenaltiesController.onPageLoad(Some(1)))
        }
      } recover {
      case ex =>
        logger.error(s"[AppealablePenaltiesController][onPageLoad] Unexpected failure: ${ex.getMessage}")
        Redirect(JourneyRecoveryController.onPageLoad())
    }
  }

}
