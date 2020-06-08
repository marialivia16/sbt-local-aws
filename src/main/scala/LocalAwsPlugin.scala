/*
 * Copyright 2019+ sbt-aws-local contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    lazy val localAwsStackName = settingKey[String]("The name of the stack.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    localAwsCloudformationLocation := { sys.error("Please provide the path to the cloudformation file.") },
    localAwsStackName := { sys.error("Please provide a name for the local stack.") },

    localAwsStart := {
      //Spin up docker for localstack image with required services

      val cfLocation = localAwsCloudformationLocation.value
      val stackName = localAwsStackName.value

      val port = 4566

      val stackNameParam = s"--stack-name $stackName"
      val endpointUrlParam = s"--endpoint-url=http://localhost:$port"

      val cfServices: List[String] = YMLParser.getAwsResourcesNames(cfLocation)

      val services = (cfServices :+ "cloudformation").mkString(",")

      val dockerRunCmd = s"""docker run -d
         |-p $port:$port
         |-v /var/run/docker.sock:/var/run/docker.sock
         |-e SERVICES=$services
         |localstack/localstack""".stripMargin.replaceAll("\n", " ")

      println(s"==> $dockerRunCmd")

      dockerRunCmd.!

      Thread.sleep(10000)

      //Pass cloudformation to localstack service
      val createStackCmd = s"aws cloudformation create-stack --template-body file://$cfLocation $stackNameParam $endpointUrlParam"
      println(s"==> $createStackCmd")
      createStackCmd.!

      //Print the created resources
      s"aws cloudformation list-stack-resources $stackNameParam $endpointUrlParam".!
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
