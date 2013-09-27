import play.api.libs.json.Json
import play.api.libs.json.Reads

package object models {

  implicit val dateTimeReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ssZ")
  implicit val authorReads = Json.reads[Author]
  implicit val repositoryReads = Json.reads[Repository]
  implicit val commitReads = Json.reads[Commit]
  implicit val pushReads = Json.reads[Push]

}
