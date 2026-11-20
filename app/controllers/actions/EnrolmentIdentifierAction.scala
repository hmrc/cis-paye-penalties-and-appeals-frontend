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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

/** Shared flow for the identifier actions: the user must be an Organisation holding a valid, activated enrolment.
  * Anything else goes to the unauthorised page, and no session goes to sign in.
  */
abstract class EnrolmentIdentifierAction(
                                          connector: AuthConnector,
                                          config: FrontendAppConfig,
                                          val parser: BodyParsers.Default
                                        )
                                        (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest] with AuthorisedFunctions with Logging {

  override val authConnector: AuthConnector = connector

  /** The enrolment key this action requires, used in the log message. */
  protected def enrolmentKey: String

  /** The employer reference from the user's enrolments, or None when they don't hold a valid activated one. */
  protected def employerReference(enrolments: Set[Enrolment]): Option[EmployerReference]

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(
      Retrievals.internalId and
        Retrievals.allEnrolments and
        Retrievals.affinityGroup
    ) {
      case Some(internalId) ~ Enrolments(enrolments) ~ Some(Organisation) =>
        employerReference(enrolments) match {
          case Some(reference) =>
            block(IdentifierRequest(request, internalId, Some(reference)))
          case None =>
            logger.warn(s"${getClass.getSimpleName} - no activated $enrolmentKey enrolment")
            Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
        }
      case _ =>
        logger.warn(s"${getClass.getSimpleName} - Unable to retrieve internal id or affinity group")
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }
}
