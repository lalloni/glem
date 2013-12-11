name := "glem"

version := "0.1.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

play.Project.playScalaSettings

resolvers += "Rhinofly Internal Release Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"

libraryDependencies += "play.modules.mailer" %% "play-mailer" % "2.1.3"
