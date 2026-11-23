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

import controllers.actions.*
import forms.pas.WhyYouAreAppealingFormProvider
import models.Mode
import pages.pas.WhyYouAreAppealingPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.pas.WhyYouAreAppealingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhyYouAreAppealingController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: WhyYouAreAppealingFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: WhyYouAreAppealingView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(WhyYouAreAppealingPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhyYouAreAppealingPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            value.toString match {
              case "3" | "6" | "21" | "7" | "8" | "13" =>
                Redirect(controllers.pas.routes.ExcuseEndDateController.onPageLoad(mode))
              case "17" =>
                Redirect(controllers.pas.routes.ExcuseEndDateKnownController.onPageLoad(mode))
              case "20" =>
                Redirect(controllers.pas.routes.CisContractorEndDateController.onPageLoad(mode))
              case "5" =>
                Redirect(controllers.pas.routes.FileReturnDateController.onPageLoad(mode))
              case "18" =>
                Redirect(controllers.pas.routes.WhyYouAreAppealingController.onPageLoad(mode)) //TODO: Change for group 7 DTR-9786
            }
          }
      )
  }
}
