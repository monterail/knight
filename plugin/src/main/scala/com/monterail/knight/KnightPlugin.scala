package com.monterail.knight.plugin

import scala.tools.nsc
import nsc.Global
import nsc.plugins.{Plugin, PluginComponent}

class KnightPlugin(val global: Global) extends Plugin {
    import global._

    val name = "knight"
    val description = "support for @knight annotation - more safety for case classes even with reflection"

    val components: List[PluginComponent] = Nil
}
