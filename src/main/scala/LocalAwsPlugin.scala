import sbt._

import sys.process._
import scala.collection.immutable.Seq
import YMLParser.Command
import models.SupportedService

object LocalAwsPlugin extends AutoPlugin {
  object autoImport {
    lazy val localAwsStart = TaskKey[Unit]("localAwsStart", "Creates a local AWS stack from provided cloudformation.")
    lazy val localAwsStop = TaskKey[Unit]("localAwsStop", "Stops the local AWS stack.")
    lazy val localAwsCliCommands = TaskKey[Unit]("localAwsCliCommands", "Prints out the AWS CLI commands.")

    lazy val localAwsCloudformationLocation = settingKey[File]("The location of the cloudformation file.")
    lazy val localAwsServices = settingKey[List[String]]("The list of services to spin up.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    localAwsCloudformationLocation := { sys.error("Please provide the path to the cloudformation file.") },
    localAwsServices := { sys.error("Please provide the services you need to spin up.") },

    localAwsStart := {
      //Spin up docker for localstack image with required services

      val servicesStrings: Seq[String] = localAwsServices.value
      val supportedServices: Seq[SupportedService] = servicesStrings.flatMap(SupportedService.fromName)

      val portMappings = servicesStrings.flatMap(SupportedService.portFromName).map(port => s"-p $port:$port").mkString(" ")
      val services = servicesStrings.mkString(",")

      s"docker run -d $portMappings -e SERVICES=$services localstack/localstack".!

      val requestedServices = if(supportedServices.isEmpty) SupportedService.values else supportedServices.toIndexedSeq

      println(s"The following services will start up: ${requestedServices.map(_.awsName).mkString(",")}")

      //Parse cloudformation
      val cmds: List[Command] = YMLParser.getAwsCommands(localAwsCloudformationLocation.value, requestedServices)

      //Execute aws cli commands or print list of errors
      cmds.foreach {
        case Right(cmd) =>
          println(cmd)
          cmd.!
        case Left(err) => err.map(println)
      }
    },

    localAwsCliCommands := {
      YMLParser.getAwsCommands(localAwsCloudformationLocation.value).foreach {
        case Right(cmd) => println(s"$cmd\n")
        case Left(err) => err.map(println)
      }
    },

    localAwsStop := {
      println("Removing localstack container...")
      Process(Seq("bash", "-c", "docker stop $(docker ps -q --filter ancestor=localstack/localstack)")).!
    }
  )
}
