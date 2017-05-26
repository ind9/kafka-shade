import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

lazy val root = (project in file(".")).
  settings(
    organization := "com.indix",
    name := "kafkaV010",
    version := "0.10.0.5",
    scalaVersion := "2.11.8",
    crossPaths := false,
    libraryDependencies += kafka
  ).
  settings(clientAssemblySettings: _*).
  settings(publishSettings: _*)


val kafka = "org.apache.kafka" % "kafka-clients" % "0.10.0.0"


lazy val _pomExtra =
  <url>http://github.com/ind9/abel</url>
    <licenses>
      <license>
        <name>Apache License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:ind9/abel.git</url>
      <connection>scm:git:git@github.com:ind9/kafka-shade.git</connection>
    </scm>
    <developers>
      <developer>
        <id>vinothkr</id>
        <name>Vinothkumar</name>
      </developer>
    </developers>

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishArtifact in(Compile, packageDoc) := true,
  publishArtifact in(Compile, packageSrc) := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := _pomExtra
)


lazy val clientAssemblySettings = Seq(
    assemblyExcludedJars in assembly := {
      val includedJars = List("kafka-clients", "slf4j-api", "lz4", "snappy-java")
      val cp = (fullClasspath in assembly).value
      cp filterNot { path =>
        includedJars.exists(path.data.getName.startsWith)
      }
    },
    assemblyShadeRules in assembly := Seq(
      // hackery to include only specific jars
      // since assembly doesn't provide a way to specifically include jars - we have to resort to this
      ShadeRule.rename("org.apache.kafka.**" -> "v010.kafka.@1").inAll,
      ShadeRule.rename("net.jpountz.util.**" -> "v010.lz4.@1").inAll,
      ShadeRule.zap("scala.*").inAll
    ),
    artifact in(Compile, assembly) := {
      val art = (artifact in(Compile, assembly)).value
      art.copy(`classifier` = Some("assembly"))  
    },
    assemblyOption in assembly := (assemblyOption in assembly).value,
    logLevel in assembly := Level.Debug
  )

addArtifact(artifact in(Compile, assembly), assembly)
