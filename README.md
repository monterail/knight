# Knight
scalac plugin that fight with case classes with default arguments, Java Refleciton API and NullPointerExpection problem

## Problem
Having a case class with default constructor arguments like:

```scala
case class A(str: String = "default", list: List[Int] = Nil)
```

if new instance is constructed via Java Reflection API it is still possible to get `null` value and `NullPointerException`.
This can be shown with:

```scala
val a = A(null, null)
a.str   // => null
a.list  // => null
```

This situation can happen if for example when storing objects in database that uses reflection to load data and one field is added to case class.
Since it is not in database, that field gets null value, even if you specified default value in constructor (because constructors are not called when using reflection).

## Solution
The solution for described problem is quite straightforward - when there is a null, replace it with default value.
And exactly this simple functionality is provided by this compiler plugin. Adding `@knight` annotation before case class definition is the only required change in code.

```scala
@knight case class A(str: String = "default", list: List[Int] = Nil)
```

Then, when `null` happens:

```scala
val a = A(null, null)
a.str   // => "default"
a.list  // => Nil
```

## Usage

### Command line:

    $ scalac -Xplugin:path/to/knight/target/scala-2.9.0.1/knight_2.9.0-1-0.1.0-SNAPSHOT.jar ...

### sbt:

In plugins build file (`plugins/build.sbt`)

```scala
autoCompilerPlugins := true

addCompilerPlugin("com.monterail" % "knight" % "0.1.0")
```


## Internals
For describe `case class A(...)` the compiler generates code similar to this:

```scala

class A(val s = A.apply$default$1, val list = A.apply$default$2){
  def s = this.s
  def list = this.list
  // ...
}

object A {
  def apply$default$1: String = "default"
  def apply$default$2: List[Int] = Nil
  // ...
}

````

Currently, `@knight` copies the default values and produces code like this:

```scala

class A(val s = A.apply$default$1, val list = A.apply$default$2){
  def s = if(this.s != null) this.s else "default"
  def list = if(this.list != null) this.list else Nil
  // ...
}

object A {
  def apply$default$1: String = "default"
  def apply$default$2: List[Int] = Nil
  // ...
}

````

There is a simple "inline" on default arguments into accessor methods.

In future version it will probably look more like:

```scala

class A(val s = A.apply$default$1, val list = A.apply$default$2){
  def s = if(this.s != null) this.s else A.apply$default$1
  def list = if(this.list != null) this.list else A.apply$default$2
  // ...
}

object A {
  def apply$default$1: String = "default"
  def apply$default$2: List[Int] = Nil
  // ...
}

````