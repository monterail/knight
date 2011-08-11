package com.monterail.knight.tests

import org.scalatest._
import org.scalatest.matchers._

import com.monterail.knight.annotation._

@knight
case class Foo(list: List[Int] = Nil)

class Test extends FlatSpec with ShouldMatchers {
    "@knight annotation" should "protect your code from nulls" in {
        val foo = Foo(null)
        foo.list should equal (Nil)
    }
}
