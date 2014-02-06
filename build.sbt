name := "glem"

version := "0.1.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.typesafe" %% "play-plugins-mailer" % "2.2.0"
)

play.Project.playScalaSettings

