package controllers

import java.net.InetAddress

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import com.typesafe.plugin._

import models._
import play.api._
import play.api.Logger._
import play.api.Play.current
import play.api.mvc._

object Application extends Controller {

  val host = InetAddress.getLocalHost.getHostName
  val fromName = current.configuration.getString("mail.from.name").getOrElse("GitLab Event Mailer")
  val fromAddress = current.configuration.getString("mail.from.address").getOrElse(s"glem-no-reply@$host")

  def sendMail(recipients: Seq[String], subject: String, html: String) =
    Future {
      val mail = use[MailerPlugin].email
      mail.setFrom(s"${fromName} <${fromAddress}>")
      mail.setRecipient(recipients: _*)
      mail.setSubject(subject)
      mail.sendHtml(html)
    }

  def sendCommitsPublishedEmail(recipients: Seq[String], push: Push) = {
    val subject = s"[GitLab] [${push.repository.name}] ${push.user_name} pushed ${push.total_commits_count} commits in ${push.ref.substring(11)} from ${push.before.take(6)} to ${push.after.take(6)}"
    val title = s"${push.user_name} pushed ${push.total_commits_count} commit${if (push.total_commits_count > 1) "s" else ""}"
    val content = views.html.commitsPublished(title, push).body
    sendMail(recipients, subject, content)
  }

  def sendBranchCreatedEmail(recipients: Seq[String], push: Push) = {
    val subject = s"[GitLab] [${push.repository.name}] ${push.user_name} pushed new branch ${push.ref.substring(11)}"
    val title = s"${push.user_name} pushed a new branch ${push.ref.substring(11)}"
    val content = views.html.branchCreated(title, push).body
    sendMail(recipients, subject, content)
  }

  def sendBranchDeletedEmail(recipients: Seq[String], push: Push) = {
    val subject = s"[GitLab] [${push.repository.name}] ${push.user_name} deleted branch ${push.ref.substring(11)}"
    val title = s"${push.user_name} deleted branch ${push.ref.substring(11)}"
    val content = views.html.branchDeleted(title, push).body
    sendMail(recipients, subject, content)
  }

  def push(recipients: String) = Action(parse.json) { request ⇒
    debug(s"Received push event ${request.body}")
    val push = request.body.as[Push]
    val recs = recipients.split(",").map(_.trim)
    val task = PushMotive(push) match {
      case BranchCreated    ⇒ sendBranchCreatedEmail(recs, push)
      case BranchDeleted    ⇒ sendBranchDeletedEmail(recs, push)
      case CommitsPublished ⇒ sendCommitsPublishedEmail(recs, push)
    }
    task onFailure {
      case e: Exception ⇒
        error(s"Sending push notification", e)
    }
    NoContent
  }

}
