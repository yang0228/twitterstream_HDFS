import AssemblyKeys._

seq(assemblySettings: _*)

name := "Twitter Streaming"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	"org.apache.spark" %% "spark-core" % "1.3.0",
	"org.apache.spark" %% "spark-streaming" % "1.3.0",
	"org.apache.spark" %% "spark-streaming-twitter" % "1.3.0",
	"org.twitter4j" % "twitter4j-stream" % "3.0.3",
	"org.twitter4j" % "twitter4j-core" % "3.0.3"
)

assemblySettings

val meta = """META.INF(.)*""".r

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
    case PathList("javax", "activation", xs @ _*) => MergeStrategy.first
    case PathList("com", "google", xs @ _*) => MergeStrategy.first
    case PathList("org", "apache", xs @ _*) => MergeStrategy.first
    case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.first
    case PathList("plugin.properties") => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
    case meta(_) => MergeStrategy.discard
    case x => old(x)
  }
}
