package models

import io.circe.Error

sealed trait PluginError

case class CirceError(err: Error) extends PluginError
case class NotImplemented(msg: String) extends PluginError