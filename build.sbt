libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.7"

mainClass in assembly := Some("org.nfulton.weddingpics.MakeSite")
assemblyJarName in assembly := "makesite.jar"