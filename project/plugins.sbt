addSbtPlugin("com.tapad" % "sbt-docker-compose" % "1.0.34")

resolvers += Resolver.bintrayIvyRepo("marialiviach", "sbt-plugins")
addSbtPlugin("marialivia.ch" % "sbt-local-aws" % "0.1.3")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
