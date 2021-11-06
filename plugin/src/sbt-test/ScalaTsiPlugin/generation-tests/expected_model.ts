// DO NOT EDIT: generated file by scala-tsi

export interface IA {
  field: string
}

export interface IB {
  a: IA
}

export interface IDeepNestingTopLevel {
  prop1: string
  prop2: INest1
}

export interface IListObj {
  a: string
  b: string
}

export interface INest1 {
  prop3: string
  prop4: INest2
}

export interface INest2 {
  prop5: string
  prop6: INest3
}

export interface INest3 {
  prop7: string
  prop8: INest4
}

export interface INest4 {
  prop8: string
  prop9: INest5
}

export interface INest5 {
  prop10: string
  prop11: INest6
}

export interface INest6 {
  prop12: string
  prop13: INest7
}

export interface INest7 {
  prop14: string
  prop15: number
}

export interface INestedGenerated {
  direct: IA
  indirect: IB
  inList: IA[]
  inDoubleList: IA[][]
}
