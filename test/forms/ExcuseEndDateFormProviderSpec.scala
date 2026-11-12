package forms

import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import utils.TimeMachine

import java.time.LocalDate

class ExcuseEndDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val time = TimeMachine()
  private val form = new ExcuseEndDateFormProvider(time)()

  ".value" - {

    val maxDate   = time.today
    val validDate = LocalDate.of(2005, 1, 1)

    behave like dateField(form, "value", validDate)

    behave like dateFieldWithMax(
      form = form,
      key = "value",
      max = maxDate,
      formError = FormError("value", "reasonableExcuseEndDate.error.maxDate")
    )

    behave like mandatoryDateField(form, "value", "reasonableExcuseEndDate.error.allRequired")
  }
}