// DO NOT EDIT: generated file by scala-tsi

export interface IClassWithUndefinedMember {
  notDefined: "Could not find TSType[models.NotDefined] in scope and could not generate it"
}

export interface IRecursiveA {
  b: IRecursiveB
}

export interface IRecursiveB {
  a: IRecursiveA
}
