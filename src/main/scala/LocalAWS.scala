import sbt._

import sys.process._
import scala.collection.immutable.Seq

object LocalAWS extends AutoPlugin {
  object autoImport {
    lazy val createLocalAWS = TaskKey[Unit]("createLocalAWS", "Creates a local AWS stack from provided cloudformation.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    createLocalAWS := {
      //Spin up docker with localstack image
      "docker run localstack/localstack".!

      //TODO: install localaws cli
      "pip install awscli-local".!

      //Parse cf
      YMLParser.getSupportedServices(new File("cf.yml"))

      //TODO: Execute aws cli commands
    }
  )
}
