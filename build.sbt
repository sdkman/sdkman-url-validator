name := "sdkman-url-validator"

organization := "io.sdkman"

scalaVersion := "2.12.5"

crossScalaVersions := Seq("2.11.8", "2.12.5")

parallelExecution in Test := false

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.2.2" % Test
)

bintrayOrganization := Some("sdkman")

bintrayReleaseOnPublish in ThisBuild := true

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
