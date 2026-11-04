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

package controllers.cis

import controllers.actions.IdentifierAction
import controllers.routes.JourneyRecoveryController
import models.GetAppealablePenaltiesRequest
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CisPayePasService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.appealablePenalties.AppealablePenaltiesDisplayRow
import views.html.cis.CisAppealablePenaltiesView

import javax.inject.*
import scala.concurrent.ExecutionContext

@Singleton
class CisAppealablePenaltiesController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             val controllerComponents: MessagesControllerComponents,
                                             identify: IdentifierAction,
                                             cisPayePasService: CisPayePasService,
                                             view: CisAppealablePenaltiesView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {
  
  val hmrcOfficeNumber = "001"
  val schemeReferenceNumber = "AB123"
  
  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    val request = GetAppealablePenaltiesRequest(
      hmrcOfficeNumber = hmrcOfficeNumber, schemeReferenceNumber = schemeReferenceNumber, startRow = 0, endRow = 10
    )
    
    cisPayePasService.getCisAppealablePenalties(request)
      .map { viewModel =>
        logger.info(s"[CisAppealablePenaltiesController][onPageLoad] - render page")
        println(viewModel.rows)
        val total = viewModel.total
        val rows = viewModel.rows.map(AppealablePenaltiesDisplayRow.fromRow)
        Ok(view(total, rows))
      } recover {
      case ex =>
        logger.error(s"[CisAppealablePenaltiesController][onPageLoad] Unexpected failure: ${ex.getMessage}")
        Redirect(JourneyRecoveryController.onPageLoad())
    }
  }

}
