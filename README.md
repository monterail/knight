# scalac plugin case classes with default arguments and reflection problem

## Problem
```scala
case class A(f: Foo = Foo.default)
A(null).f # => null
```

## Building (sbt 0.10.x)
    $ sbt package

## Using

Command line:

    $ scalac -Xplugin:path/to/knight/target/scala-2.9.0.1/knight_2.9.0-1-0.1.0-SNAPSHOT.jar ...

sbt:

    scalacOptions   += "-Xplugin:path/to/knight/target/scala-2.9.0.1/knight_2.9.0-1-0.1.0-SNAPSHOT.jar"

