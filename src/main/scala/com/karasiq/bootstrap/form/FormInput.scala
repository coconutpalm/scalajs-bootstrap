package com.karasiq.bootstrap.form

import com.karasiq.bootstrap.BootstrapImplicits._
import com.karasiq.bootstrap.{Bootstrap, BootstrapComponent, BootstrapHtmlComponent}
import org.scalajs.dom
import rx._

import scalatags.JsDom.all
import scalatags.JsDom.all._

final class FormGenericInput(formLabel: Modifier, inputType: String = "text", inputId: String = Bootstrap.newId) extends BootstrapHtmlComponent[dom.html.Div] {
  override def renderTag(md: Modifier*): RenderedTag = {
    val controlId = s"$inputId-form-$inputType-input"
    div("form-group".addClass)(
      label(`for` := controlId, formLabel),
      input("form-control".addClass, `type` := inputType, id := controlId, md)
    )
  }
}

final class FormCheckbox(checkboxLabel: Modifier) extends BootstrapHtmlComponent[dom.html.Div] {
  override def renderTag(md: Modifier*): RenderedTag = {
    div("checkbox".addClass)(
      label(
        input(`type` := "checkbox", md),
        raw("&nbsp;"),
        checkboxLabel
      )
    )
  }
}

case class FormRadio(title: String, radioName: String, radioValue: String, radioId: String = Bootstrap.newId) extends BootstrapHtmlComponent[dom.html.Div] {
  override def renderTag(md: Modifier*): RenderedTag = {
    div("radio".addClass)(
      label(
        input(`type` := "radio", name := radioName, id := radioId, value := radioValue, md),
        title
      )
    )
  }
}

class FormRadioGroup(final val radioList: Rx[Seq[FormRadio]])(implicit ctx: Ctx.Owner) extends BootstrapComponent {
  final val value: Var[String] = Var(radioList.now.head.radioValue)

  private def valueWriter(r: FormRadio) = value.reactiveReadWrite("change", e ⇒ {
    val input = e.asInstanceOf[dom.html.Input]
    if (input.checked) {
      r.radioValue
    } else {
      value.now
    }
  }, (e, v) ⇒ {
    val input = e.asInstanceOf[dom.html.Input]
    input.checked = v == r.radioValue
  })

  override def render(md: Modifier*): Modifier = {
    Rx {
      val values = radioList()
      div(values.head.renderTag(md, checked, valueWriter(values.head)), values.tail.map(v ⇒ v.renderTag(md, valueWriter(v))))
    }
  }
}

class FormSelect(selectLabel: Modifier, allowMultiple: Boolean, final val options: Rx[Seq[String]], val inputId: String = Bootstrap.newId)(implicit ctx: Ctx.Owner) extends BootstrapHtmlComponent[dom.html.Div] {
  final val selected: Var[Seq[String]] = Var(Seq(options.now.head))

  override def renderTag(md: all.Modifier*): RenderedTag = {
    val controlId = s"$inputId-form-select-input"
    div("form-group".addClass)(
      label(`for` := controlId, selectLabel),
      Rx {
        select("form-control".addClass, if (allowMultiple) multiple else (), id := controlId, md)(
          for (o ← options()) yield option(o),
          selected.reactiveReadWrite("change", e ⇒ {
            val select = e.asInstanceOf[dom.html.Select]
            select.options.collect {
              case opt if opt.selected ⇒
                opt.value
            }
          }, (e, v) ⇒ {
            val select = e.asInstanceOf[dom.html.Select]
            select.options.foreach { opt ⇒
              opt.selected = v.contains(opt.value)
            }
          })
        )
      }
    )
  }
}


class FormTextArea(val textAreaLabel: Modifier, val inputId: String = Bootstrap.newId) extends BootstrapHtmlComponent[dom.html.Div] {
  override def renderTag(md: all.Modifier*): RenderedTag = {
    val controlId = s"$inputId-form-textarea-input"
    div("form-group".addClass)(
      label(`for` := controlId, textAreaLabel),
      textarea("form-control".addClass, id := controlId, md)
    )
  }
}

object FormInput {
  def ofType(tpe: String, label: Modifier, md: Modifier*): Tag = {
    new FormGenericInput(label, tpe).renderTag(md:_*)
  }

  def text(label: Modifier, md: Modifier*): Tag = this.ofType("text", label, md:_*)

  def number(label: Modifier, md: Modifier*): Tag = this.ofType("number", label, md:_*)

  def email(label: Modifier, md: Modifier*): Tag = this.ofType("email", label, md:_*)

  def password(label: Modifier, md: Modifier*): Tag = this.ofType("password", label, md:_*)

  def file(label: Modifier, md: Modifier*): Tag = this.ofType("file", label, "form-control".removeClass +: md:_*)

  def textArea(title: Modifier, md: Modifier*): Tag = {
    new FormTextArea(title).renderTag(md:_*)
  }

  def checkbox(label: Modifier, md: Modifier*): Tag = {
    new FormCheckbox(label).renderTag(md:_*)
  }

  def radio(title: String, radioName: String, radioValue: String, radioId: String = Bootstrap.newId): FormRadio = {
    new FormRadio(title, radioName, radioValue, radioId)
  }

  def radioGroup(radios: FormRadio*)(implicit ctx: Ctx.Owner): FormRadioGroup = {
    new FormRadioGroup(Rx(radios))
  }

  def radioGroup(radios: Rx[Seq[FormRadio]])(implicit ctx: Ctx.Owner): FormRadioGroup = {
    new FormRadioGroup(radios)
  }

  def select(title: Modifier, options: String*)(implicit ctx: Ctx.Owner): FormSelect = {
    new FormSelect(title, false, Rx(options))
  }

  def select(title: Modifier, options: Rx[Seq[String]])(implicit ctx: Ctx.Owner): FormSelect = {
    new FormSelect(title, false, options)
  }

  def multipleSelect(title: Modifier, options: String*)(implicit ctx: Ctx.Owner): FormSelect = {
    new FormSelect(title, true, Rx(options))
  }

  def multipleSelect(title: Modifier, options: Rx[Seq[String]])(implicit ctx: Ctx.Owner): FormSelect = {
    new FormSelect(title, true, options)
  }

  // Default
  def apply(label: Modifier, md: Modifier*): Tag = this.text(label, md:_*)
}
