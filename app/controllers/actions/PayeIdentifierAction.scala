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
import models.EmployerReference
import models.requests.IdentifierRequest
import play.api.mvc.*
import uk.gov.hmrc.auth.core.*
import utils.AuthUtils

import scala.concurrent.ExecutionContext

trait PayeIdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

/** PAYE employer guard: an Organisation with an activated IR-PAYE enrolment. */
class AuthenticatedPayeIdentifierAction @Inject()(
                                                   authConnector: AuthConnector,
                                                   config: FrontendAppConfig,
                                                   parser: BodyParsers.Default
                                                 )
                                                 (implicit executionContext: ExecutionContext)
  extends EnrolmentIdentifierAction(authConnector, config, parser) with PayeIdentifierAction {

  override protected def enrolmentKey: String = AuthUtils.PayeEmployerEnrolmentKey

  override protected def employerReference(enrolments: Set[Enrolment]): Option[EmployerReference] =
    AuthUtils.payeEmployerReference(enrolments)
}
