package net.shrine.adapter.dao.scalaquery.tables

import org.scalaquery.ql._

/**
 * @author clint
 * @date Oct 22, 2012
 * 
 * NB: Enriches NamedColumn[T] to allow appending Projections
 */
object ProjectionHelpers {
  private[tables] final class HasProjectionHelpers[T](column: NamedColumn[T]) {
    //Column-prepending operator needs to be named something other than '~' to avoid clash with exiting operator (Not sure why) 
    def ~~[A, B](p: Projection2[A, B]): Projection3[T, A, B] =  column ~ p._1 ~ p._2
    def ~~[A, B, C](p: Projection3[A, B, C]): Projection4[T, A, B, C] =  column ~ p._1 ~ p._2 ~ p._3
    def ~~[A, B, C, D](p: Projection4[A, B, C, D]): Projection5[T, A, B, C, D] =  column ~ p._1 ~ p._2 ~ p._3 ~ p._4
    def ~~[A, B, C, D, E](p: Projection5[A, B, C, D, E]): Projection6[T, A, B, C, D, E] =  column ~ p._1 ~ p._2 ~ p._3 ~ p._4 ~ p._5
    def ~~[A, B, C, D, E, F](p: Projection6[A, B, C, D, E, F]): Projection7[T, A, B, C, D, E, F] =  column ~ p._1 ~ p._2 ~ p._3 ~ p._4 ~ p._5 ~ p._6
    def ~~[A, B, C, D, E, F, G](p: Projection7[A, B, C, D, E, F, G]): Projection8[T, A, B, C, D, E, F, G] =  column ~ p._1 ~ p._2 ~ p._3 ~ p._4 ~ p._5 ~ p._6 ~ p._7
    def ~~[A, B, C, D, E, F, G, H](p: Projection8[A, B, C, D, E, F, G, H]): Projection9[T, A, B, C, D, E, F, G, H] =  column ~ p._1 ~ p._2 ~ p._3 ~ p._4 ~ p._5 ~ p._6 ~ p._7 ~ p._8
    def ~~[A, B, C, D, E, F, G, H, I](p: Projection9[A, B, C, D, E, F, G, H, I]): Projection10[T, A, B, C, D, E, F, G, H, I] =  column ~ p._1 ~ p._2 ~ p._3 ~ p._4 ~ p._5 ~ p._6 ~ p._7 ~ p._8 ~ p._9
    def ~~[A, B, C, D, E, F, G, H, I, J](p: Projection10[A, B, C, D, E, F, G, H, I, J]): Projection11[T, A, B, C, D, E, F, G, H, I, J] =  column ~ p._1 ~ p._2 ~ p._3 ~ p._4 ~ p._5 ~ p._6 ~ p._7 ~ p._8 ~ p._9 ~ p._10
  }
  
  private[tables] implicit def column2HasProjectionHelpers[T](column: NamedColumn[T]): HasProjectionHelpers[T] = new HasProjectionHelpers(column)
}