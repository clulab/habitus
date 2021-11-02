

import scala.collection.mutable

var sentIdFreq= mutable.Map("four"->4)
sentIdFreq+=("five"->5)
sentIdFreq+=("six"->6)
sentIdFreq+=("seven"->4)

val lowers=List("one","two","three")

print(sentIdFreq.keys.filter(_.length>4).map(_.toUpperCase()))


