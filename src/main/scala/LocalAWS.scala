import sbt._

import scala.collection.immutable.Seq

object LocalAWS extends AutoPlugin {
  object autoImport {
    lazy val createLocalAWS = TaskKey[Unit]("createLocalAWS", "Creates a local AWS stack from provided cloudformation.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    createLocalAWS := {
      YMLParser.getAWSServices(new File("cf.yml"))
    }
  )
}

//object PluginApp extends App {
//  YMLParser.getAWSServices(new File("cf.yml"))
//}
