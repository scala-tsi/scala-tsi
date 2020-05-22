
export interface IBar {
  value: string
}


export interface IBaz {
  boo: boolean
  bar: number
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


export type Sealed = (ISealedOption1 | ISealedOption2)