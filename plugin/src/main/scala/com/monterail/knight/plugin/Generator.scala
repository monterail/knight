package com.monterail.knight.plugin

import scala.tools._
import nsc.Global
import nsc.plugins.PluginComponent
import nsc.transform.{Transform, TypingTransformers}
import nsc.symtab.Flags._
import nsc.ast.TreeDSL
import nsc.symtab

class Generator(plugin: KnightPlugin, val global: Global) extends PluginComponent with Transform with TypingTransformers with TreeDSL {
    import global._
    import definitions._

    // PluginComponent settings
    val runsAfter = List("refchecks")

    val phaseName = "knight-generator"

    val annotationClass = definitions.getClass("com.monterail.knight.annotation.knight")


    def newTransformer(unit: CompilationUnit) = new KnightTransformer(unit)

    class KnightTransformer(unit: CompilationUnit) extends TypingTransformer(unit) {
        def shouldProtect(sym: Symbol) = sym.isCaseClass && sym.annotations.exists(_.atp.typeSymbol == annotationClass)

        def zipWithIndexIf[T](list: List[T])(pred: T => Boolean): List[(T, Option[Int])] = {
            def doZip(zipped: List[(T, Option[Int])], left: List[T], index: Int): List[(T, Option[Int])] = left match {
                case x :: xs if pred(x) => doZip((x, Some(index)) :: zipped, xs, index + 1)
                case x :: xs => doZip((x, None) :: zipped, xs, index)
                case Nil => zipped
            }

            doZip(Nil, list, 0).reverse
        }

        override def transform(tree: Tree) = {
            val newTree = tree match {
               case pd @ PackageDef(_, stats) =>
                    val modules = collectModules(stats)

                    val newStats = stats collect {
                        case cd @ ClassDef(_, _, _, tpl) if shouldProtect(cd.symbol) =>
                            val module = modules(cd.symbol)
                            val applyDefaults = (Map[String, Tree]() /: module.impl.body) {
                                case (map, dd @ DefDef(mods, name, _, vparams, _, rhs)) if name.toString.startsWith("apply$default$") =>
                                    map + (name.toString -> rhs)
                                case (map, _) =>
                                    map
                            }

                            val newBody = zipWithIndexIf(tpl.body){
                                case df @ DefDef(mods, _, _, _, _, _) if mods.isParamAccessor => true
                                case _ => false
                            } collect {
                                case (df @ DefDef(_, name, _, _, _, rhs), Some(index)) =>
                                    val key = "apply$default$" + (index+1)
                                    applyDefaults get key match {
                                        case Some(default) =>
                                            val owner0 = localTyper.context1.enclClass.owner
                                            localTyper.context1.enclClass.owner = df.symbol.moduleClass

                                            val newRhs = localTyper typed {
                                                If(
                                                     Apply(
                                                         Select(rhs, newTermName(scala.reflect.NameTransformer.encode("!="))),
                                                         List(Literal(Constant(null)))
                                                     ),
                                                     rhs,
                                                     default
                                                 )

                                             }

                                             localTyper.context1.enclClass.owner = owner0

                                             treeCopy.DefDef(df, df.mods, df.name, df.tparams, df.vparamss, df.tpt, newRhs)

                                         case None =>
                                             df
                                   }
                                case (x, _) => x
                            }

                            val newTpl = treeCopy.Template(tpl, tpl.parents, tpl.self, newBody)

                            treeCopy.ClassDef(cd, cd.mods, cd.name, cd.tparams, newTpl)

                        case x => x
                    }

                    treeCopy.PackageDef(pd, pd.pid, newStats)
                case _ =>
                    tree
            }

            super.transform(newTree)
        }

        def collectModules(trees: List[Tree]) = (Map[Symbol, ClassDef]() /: trees) {
            case (xs, md @ ClassDef(_, _, _, _)) if md.symbol.isModuleClass => xs + (md.symbol.companionClass -> md)
            case (xs, _) => xs
        }
    }
}
