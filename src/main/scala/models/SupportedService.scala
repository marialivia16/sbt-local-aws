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
package models

import enumeratum._

sealed abstract class SupportedService(val awsType: String) extends EnumEntry {
  val name: String
}

object SupportedService extends Enum[SupportedService] with CirceEnum[SupportedService] {

  def withAwsType(awsType: String): Option[SupportedService] = values find (_.awsType == awsType)

  case object DynamoDB extends SupportedService(awsType = "AWS::DynamoDB::Table") {

    override val name: String = "dynamodb"

  }

  case object S3 extends SupportedService(awsType = "AWS::S3::Bucket") {

    override val name: String = "s3"

  }

  val values = findValues

}