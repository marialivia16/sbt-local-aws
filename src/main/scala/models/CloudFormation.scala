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

//Not used
case class CloudFormation(parameters: Parameters, resources: List[Resource])

case class Parameters(params: Map[String, String])

case class Resource(`type`: String, properties: Properties)

case class Properties(sseSpecification: SSESpecification,
                      provisionedThroughput: ProvisionedThroughput,
                      attributeDefinitions: AttributeDefinitions,
                      globalSecondaryIndexes: List[GlobalSecondaryIndex],
                      keySchema: KeySchema,
                      tableName: String
                     )
case class SSESpecification(sseEnabled: Boolean)
case class ProvisionedThroughput(readCapacityUnits: Int, writeCapacityUnits: Int)
case class AttributeDefinitions(attributeName: String, attributeType: String)
case class GlobalSecondaryIndex(indexName: String, keySchema: List[KeySchema], projection: Projection, provisionedThroughput: ProvisionedThroughput)
case class KeySchema(attributeName: String, keyType: String)
case class Projection(projectionType: String)
