package controllers

import java.net.InetAddress
import scala.concurrent.ExecutionContext.Implicits.global
import play.api._
import play.api.mvc._
import play.api.Logger._
import play.api.Play.current
import play.modules.mailer._
import models._

object Application extends Controller {

  val host = InetAddress.getLocalHost.getHostName
  val fromName = current.configuration.getString("mail.from.name").getOrElse("GitLab Event Mailer")
  val fromAddress = current.configuration.getString("mail.from.address").getOrElse(s"glem-no-reply@$host")

  def commitsPublishedEmail(push: Push) = {
    val subject = s"[GitLab] [${push.repository.name}] ${push.user_name} pushed ${push.total_commits_count} commits in ${push.ref.substring(11)} from ${push.before.take(6)} to ${push.after.take(6)}"
    val title = s"${push.user_name} pushed ${push.total_commits_count} commit${if (push.total_commits_count > 1) "s" else ""}"
    Email(
      subject = subject,
      from = EmailAddress(fromName, fromAddress),
      text = "See html attachment.",
      htmlText = views.html.commitsPublished(title, push).body)
  }

  def branchCreatedEmail(push: Push) = {
    val subject = s"[GitLab] [${push.repository.name}] ${push.user_name} pushed ${push.total_commits_count} commits in new branch ${push.ref.substring(11)}"
    val title = s"${push.user_name} pushed a new branch ${push.ref.substring(11)}"
    Email(
      subject = subject,
      from = EmailAddress(fromName, fromAddress),
      text = "See html attachment.",
      htmlText = views.html.branchCreated(title, push).body)
  }

  def branchDeletedEmail(push: Push) = {
    val subject = s"[GitLab] [${push.repository.name}] ${push.user_name} deleted branch ${push.ref.substring(11)}"
    val title = s"${push.user_name} deleted branch ${push.ref.substring(11)}"
    Email(
      subject = subject,
      from = EmailAddress(fromName, fromAddress),
      text = "See html attachment.",
      htmlText = views.html.branchDeleted(title, push).body)
  }

  def push(recipients: String) = Action(parse.json) { request ⇒
    debug(s"Received push event ${request.body}")
    val push = request.body.as[Push]
    val email = PushMotive(push) match {
      case BranchCreated    ⇒ branchCreatedEmail(push)
      case BranchDeleted    ⇒ branchDeletedEmail(push)
      case CommitsPublished ⇒ commitsPublishedEmail(push)
    }
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
