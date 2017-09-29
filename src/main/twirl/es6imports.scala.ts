@(imports: Seq[TypescriptInterface[_ <: AnyRef]])

@for((fileName, interfaces) <- imports.groupBy(_.fileName)) {
    @if(interfaces.size > 4) {
import * from '@fileName'
    } else {
import { @interfaces.map(_.name).mkString(", ") } from '@fileName'
    }
}
