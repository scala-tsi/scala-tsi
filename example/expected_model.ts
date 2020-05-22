
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
       