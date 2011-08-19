package com.monterail.knight.tests

import org.scalatest._
import org.scalatest.matchers._

import com.monterail.knight.annotation._

@knight
case class Lord(a: String, b: String = "Default B", c: String = "Default C")
case class Fool(a: String, b: String = "Default B", c: String = "Default C")

@knight
case class Foo(a: Map[Any, Any] = Map(1 -> 2, "f" -> new Lord("xx")))


class Test extends FlatSpec with ShouldMatchers {
    "@knight annotation" should "protect lords" in {
        val lord = Lord(null, null, null)
        lord.a should equal (null)
        lord.b should equal ("Default B")
        lord.c should equal ("Default C")
    }

    it should "not protect fools" in {
        val fool = Fool(null, null, null)
        fool.a should equal (null)
        fool.b should equal (null)
        fool.c should equal (null)
    }
}
