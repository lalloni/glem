package models

import org.joda.time.DateTime

case class Author(
  name: String,
  email: String)

case class Repository(
  name: String,
  url: String,
  description: Option[String],
  homepage: String,
  `private`: Option[Boolean])

case class Commit(
  id: String,
  message: String,
  timestamp: DateTime,
  url: String,
  author: Author)

case class Push(
  before: String,
  after: String,
  ref: String,
  user_id: Int,
  user_name: String,
  repository: Repository,
  commits: List[Commit],
  total_commits_count: Int)
