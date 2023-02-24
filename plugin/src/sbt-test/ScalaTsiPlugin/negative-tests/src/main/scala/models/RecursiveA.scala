package models

case class RecursiveA(b: RecursiveB)
case class RecursiveB(a: RecursiveA)
