// DO NOT EDIT: generated file by scala-tsi

export function GreetFunction(arg0: string, arg1: number): string

export interface IDeepThought {
  query: "The Answer to Life, the Universe and Everything"
  answer: 42
  question: null
}

export interface IGenericCaseClass {
  optional?: string
  emails: string[]
  mapping: { [ key: string ]: IPerson }
}

export interface IGreeter {
  anonymousFunction(arg0: string): number
  namedFunction: typeof GreetFunction
  voidFunction(): void
}

export interface IJob {
  tasks: string[]
  boss: string
}

export interface IMyCaseClass {
  nested: IPerson
  bool: boolean
}

export interface IPerson {
  name: string
  email: string
  age?: number
  job: IJob
}

export interface ISealedOption1 {
  foo: string
  kind: "SealedOption1"
}

export interface ISealedOption2 {
  bar: number
  kind: "SealedOption2"
}

export type JavaEnum = ("OPTION1" | "OPTION2" | "OPTION3" | "OPTION4")

export type ScalaEnum = ("OPTION1" | "OPTION2" | "OPTION3" | "OPTION4")

export type Sealed = (ISealedOption1 | ISealedOption2)
