organization := "ru.makkarpov"
name := "Play Utils"
normalizedName := "play-utils"
description := "A small reusable pieces of code for Play Framework and Slick that will (hopefully) make your life easier."
version := "0.1.1-SNAPSHOT"

scalaVersion := "2.11.8"
//crossScalaVersions := Seq("2.11.8", "2.12.1") fails on 2.12.1 for some reason
libraryDependencies := Seq(
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "com.google.guava" % "guava" % "19.0",

  "org.scalatest" %% "scalatest" % "3.0.1" % Test,
  "com.github.tminglei" %% "slick-pg" % "0.13.0" % Test,
  "org.slf4j" % "slf4j-simple" % "1.7.22" % Test
)

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("https://github.com/makkarpov/play-utils"))
organizationName := "Maxim Karpov"
organizationHomepage := Some(url("https://github.com/makkarpov"))
scmInfo := Some(ScmInfo(
  browseUrl = url("https://github.com/makkarpov/play-utils"),
  connection = "scm:git://github.com/makkarpov/play-utils.git"
))

pomExtra := {
  <developers>
    <developer>
      <id>makkarpov</id>
      <name>Maxim Karpov</name>
      <url>https://github.com/makkarpov</url>
    </developer>
  </developers>
}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}