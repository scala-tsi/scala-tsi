export type DiscriminatedUnion = (IDU1 | IDU2)

export interface IBar {
  value: string
}

export interface IBaz {
  boo: boolean
  bar: number
}

export interface IDU1 {
  type: "type1"
  field1: string
}

export interface IDU2 {
  type: "type2"
  field2: string
}

export interface IDeepThought {
  query: "The Answer to Life, the Universe and Everything"
  answer: 42
  question: null
}

export interface IFoo {
  bar: IBar
  bool: boolean
  num?: number
  baz?: IBaz
}

export interface IJob {
  tasks: string[]
  boss: string
}

export interface IPerson {
  name: string
  email: string
  age?: number
  job: IJob
}

export interface ISealedOption1 {
  foo: string
}

export interface ISealedOption2 {
  bar: number
}

export type JavaEnum = ("OPTION1" | "OPTION2" | "OPTION3" | "OPTION4")

export type ScalaEnum = ("OPTION1" | "OPTION2" | "OPTION3" | "OPTION4")

export type Sealed = (ISealedOption1 | ISealedOption2)
