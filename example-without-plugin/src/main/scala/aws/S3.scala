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
package aws

import java.net.URI
import java.nio.charset.Charset

import models.{Dev, Environment}
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.core.sync.{RequestBody, ResponseTransformer}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model._

import scala.collection.JavaConverters._

class S3(environment: Environment) {
  val client: S3Client = chooseClient(environment)

  private def chooseClient(environment: Environment): S3Client = environment match {
    case Dev => S3Client.builder().endpointOverride(URI.create("http://localhost:4572")).build()
    case _ => S3Client.builder()
      .region(Region.EU_WEST_1)
      .credentialsProvider(ProfileCredentialsProvider.builder()
        .profileName("ConcertTicketsCreds")
        .build())
      .build()
  }
}

object S3 {
  private val PriceBandsBucket = "PriceBandsBucket"

  def setConcertPrices(concertId: String, priceBands: Map[String, Int])(client: S3Client): PutObjectResponse = {
    val request = PutObjectRequest.builder().bucket(PriceBandsBucket).key(concertId).build()

    val bandsRows: String = priceBands.map { case (band, price) =>
      s"$band,$price"
    }.mkString("\n")

    client.putObject(request, RequestBody.fromString(bandsRows))
  }

  def getAllConcertPrices(client: S3Client): List[(String, Map[String, Int])] = {
    val request = ListObjectsV2Request.builder().bucket(PriceBandsBucket).build()
    val objects = client.listObjectsV2(request)
    objects.contents().asScala.toList.map { obj =>
      val concertId = obj.key()
      val getObjectRequest = GetObjectRequest.builder().key(concertId).bucket(PriceBandsBucket).build()
      val inputStream: ResponseBytes[GetObjectResponse] = client.getObject(getObjectRequest, ResponseTransformer.toBytes[GetObjectResponse])

      val map = inputStream.asString(Charset.defaultCharset()).split("\n").foldLeft(Map.empty[String, Int]) { (acc, line) =>
        line.split(",").toList match {
          case band :: price :: Nil => acc ++ Map((band, price.toInt))
          case _ => acc
        }
      }

      (concertId, map)
    }
  }
}
