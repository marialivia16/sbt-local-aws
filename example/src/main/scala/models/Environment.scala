package models

sealed trait Environment

case object Dev extends Environment
case object Code extends Environment
case object Prod extends Environment
