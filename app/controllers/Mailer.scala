package controllers

import java.net.InetAddress

import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._
import play.api.Logger._
import play.api.Play.current
import play.modules.mailer._

import models._

object Mailer extends Controller {

  val host = InetAddress.getLocalHost.getHostName
  val fromName = current.configuration.getString("mail.from.name").getOrElse("GitLab Event Mailer")
  val fromAddress = current.configuration.getString("mail.from.address").getOrElse(s"glem-no-reply@$host")

  def push(recipients: String) = Action(parse.json) { request ⇒
    debug(s"Received push event ${request.body}")
    val push = request.body.as[Push]
    val email =
      Email(
        subject = s"[GitLab] [${push.repository.name}] ${push.user_name} pushed ${push.total_commits_count} commits in ${push.ref.substring(11)} from ${push.before.take(6)} to ${push.after.take(6)}",
        from = EmailAddress(fromName, fromAddress),
        text = "See html attachment.",
        htmlText = views.html.push(push).body)
    val addressedEmail =
      recipients
        .split(",")
        .toSet
        .foldLeft(email)(_ to ("", _))
    debug(s"Sending $addressedEmail")
    AsyncMailer
      .sendEmail(addressedEmail)
      .onFailure({
        case SendEmailException(email, cause) ⇒
          error(s"Sending $email", cause)
        case SendEmailTransportCloseException(result, cause) ⇒
          error(s"Sending email transport close $result", cause)
      })
    NoContent
  }

}
