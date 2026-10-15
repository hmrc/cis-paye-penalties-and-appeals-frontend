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

package services


import connectors.CisPayePasConnector
import models.shared.PaginationDetails
import models.{ContractorDetails, CreateCisAppealRequest, CreateCisAppealResponse, GetCisAppealablePenaltiesRequest}
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.appealablePenalties.{AppealablePenaltiesViewModel, AppealablePenaltiesViewModelRow}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasService @Inject()(backendConnector: CisPayePasConnector)(implicit ec: ExecutionContext) extends Logging {

  def createCisAppeal(penaltyId: Long)
                     (implicit hc: HeaderCarrier,  request: Request[_]): Future[CreateCisAppealResponse] = {
    logger.debug("[CisPayePasService][createCisAppeal] Creating Cis Appeal")
    val createCisAppealRequest = CreateCisAppealRequest(penaltyId)
    backendConnector.createCisAppeal(createCisAppealRequest)
  }

  def getCisAppealablePenalties(contractorDetails: ContractorDetails, paginationDetails: PaginationDetails)
                               (implicit hc: HeaderCarrier,  request: Request[_]): Future[AppealablePenaltiesViewModel] = {
    val dataRequest = GetCisAppealablePenaltiesRequest.convertToDataRequest(contractorDetails, paginationDetails)
    logger.debug("[CisPayePasService][getCisAppealablePenalties] Getting appealable penalties")
    for {
      appealablePenalties <- backendConnector.getCisAppealablePenalties(dataRequest)
    } yield AppealablePenaltiesViewModel(
      total = appealablePenalties.total,
      rows = appealablePenalties.appealablePenalties.map { penalty =>
        AppealablePenaltiesViewModelRow(
          uniqueId         = penalty.uniqueId,
          issueDate        = penalty.issueDate,
          taxPeriodEndDate = penalty.taxPeriodEndDate,
          typeOfPenalty    = penalty.penaltyType,
          amount           = penalty.penaltyAmount
        )
      }
    )
  }

}
