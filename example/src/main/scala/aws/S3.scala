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
    case Dev => S3Client.builder().endpointOverride(URI.create("localhost:4569")).build()
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

    val bandsRows = priceBands.map { case (band, price) =>
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
        val (band, price): (String, String) = line.split(",")
        acc ++ Map((band, price.toInt))
      }

      (concertId, map)
    }
  }
}
