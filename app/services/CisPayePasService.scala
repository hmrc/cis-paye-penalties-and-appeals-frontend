package services


import connectors.CisPayePasConnector
import models.CreateCisAppealResponse
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CisPayePasService @Inject()(backendConnector: CisPayePasConnector)(implicit ec: ExecutionContext) extends Logging {

  def createCisAppeal(penaltyId: Long)
                   (implicit hc: HeaderCarrier,  request: Request[_]): Future[CreateCisAppealResponse] = {
    logger.debug("[CisPayePasService][createCisAppeal] Creating Cis Appeal")
    backendConnector.createCisAppeal(penaltyId)
  }

}
