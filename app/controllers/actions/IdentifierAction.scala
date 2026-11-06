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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.EmployerReference
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results.*
import play.api.mvc.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(
      Retrievals.internalId and
        Retrievals.allEnrolments and
        Retrievals.affinityGroup
    ) {
      case Some(internalId) ~ Enrolments(enrolments) ~ Some(Organisation) =>
        hasCisOrgEnrolment(enrolments)
          .map { employerReference =>
            block(IdentifierRequest(request, internalId, Some(employerReference)))
          }
          .getOrElse(throw new UnauthorizedException("Unable to retrieve internal Id"))
      case _ =>
        logger.warn("EnrolmentAuthIdentifierAction - Unable to retrieve internal id or affinity group")
        throw new UnauthorizedException("Unable to retrieve internal Id")
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  def hasCisOrgEnrolment[A](enrolments: Set[Enrolment]): Option[EmployerReference] = {
    enrolments.find(_.key == "HMRC-CIS-ORG") match {
      case Some(enrolment) =>
        val taxOfficeNumber = enrolment.identifiers.find(id => id.key == "TaxOfficeNumber").map(_.value)
        val taxOfficeReference = enrolment.identifiers.find(id => id.key == "TaxOfficeReference").map(_.value)
        val isActivated = enrolment.isActivated
        
        (taxOfficeNumber, taxOfficeReference, isActivated) match {
          case (Some(number), Some(reference), true) if isValidTaxOfficeNumber(number) =>
            Some(EmployerReference(number, reference))
          case _ =>
            logger.warn("EnrolmentAuthIdentifierAction - Unable to retrieve activated cis enrolments")
            None
        }
      case _ => None
    }
  }
}

def isValidTaxOfficeNumber(taxOfficeNumber: String): Boolean =
  (taxOfficeNumber != null) && taxOfficeNumber.matches("[0-9]{3}")
