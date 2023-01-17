package com.scalatsi

import scala.quoted.ToExpr
import com.scalatsi.TypescriptType.*
import scala.quoted.*
import scala.collection.immutable.ListMap

object TypescriptTypeToExpr {
  given ListMapToExpr[K: Type: ToExpr, V: Type: ToExpr]: ToExpr[ListMap[K, V]] with {
    def apply(map: ListMap[K, V])(using Quotes): Expr[ListMap[K, V]] =
      '{ ListMap(${ Expr(map.toSeq) }*) }

  }

  given TsLiterlTypeToExpr: ToExpr[TSLiteralType[?]] with {
    def apply(tsType: TSLiteralType[?])(using Quotes): Expr[TSLiteralType[?]] =
      tsType match
        case TSLiteralString(value)  => '{ TSLiteralString(${ Expr(value) }) }
        case TSLiteralNumber(value)  => '{ TSLiteralNumber(${ Expr(value) }) }
        case TSLiteralBoolean(value) => '{ TSLiteralBoolean(${ Expr(value) }) }
  }

  given ToExpr[TypescriptType] with {
    def apply(tsType: TypescriptType)(using Quotes): Expr[TypescriptType] =
      tsType match
        case TSAlias(name, underlying)         => '{ TSAlias(${ Expr(name) }, ${ Expr(underlying) }) }
        case TSAny                             => '{ TSAny }
        case TSArray(elementType)              => '{ TSArray(${ apply(elementType) }) }
        case TSBoolean                         => '{ TSBoolean }
        case tpe: TSLiteralType[?]             => TsLiterlTypeToExpr.apply(tpe)
        case TSEnum(name, const, entries)      => '{ TSEnum(${ Expr(name) }, ${ Expr(const) }, ${ Expr(entries) }) }
        case TSFunction(arguments, returnType) => '{ TSFunction(${ Expr(arguments) }, ${ Expr(returnType) }) }
        case TSFunctionNamed(name, signature) => '{ TSFunctionNamed(${ Expr(name) }, ${ apply(signature).asInstanceOf[Expr[TSFunction]] }) }
        case TSIndexedInterface(indexName, indexType, valueType) =>
          '{ TSIndexedInterface(${ Expr(indexName) }, ${ Expr(indexType) }, ${ Expr(valueType) }) }
        case TSInterfaceIndexed(name, indexName, indexType, valueType) =>
          '{ TSInterfaceIndexed(${ Expr(name) }, ${ Expr(indexName) }, ${ Expr(indexType) }, ${ Expr(valueType) }) }
        case TSInterface(name, members) => '{ TSInterface(${ Expr(name) }, ${ Expr(members) }) }
        case TSIntersection(of)         => '{ TSIntersection(${ Expr(of) }) }
        case TSNever                    => '{ TSNever }
        case TSNull                     => '{ TSNull }
        case TSNumber                   => '{ TSNumber }
        case TSObject                   => '{ TSObject }
        case TSString                   => '{ TSString }
        case TSTuple(of)                => '{ TSTuple(${ Expr(of) }) }
        case TSTypeReference(name, impl, discriminator, useTypeQuery) =>
          '{ TSTypeReference(${ Expr(name) }, ${ Expr(impl) }, ${ Expr(discriminator) }, ${ Expr(useTypeQuery) }) }
        case TSUndefined => '{ TSUndefined }
        case TSUnknown   => '{ TSUnknown }
        case TSUnion(of) => '{ TSUnion(${ Expr(of) }) }
        case TSVoid      => '{ TSVoid }
  }
}
