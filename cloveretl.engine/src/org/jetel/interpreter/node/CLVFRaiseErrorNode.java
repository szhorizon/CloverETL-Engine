/* Generated By:JJTree: Do not edit this line. CLVFRaiseErrorNode.java */

package org.jetel.interpreter.node;

import org.jetel.interpreter.TransformLangParser;
import org.jetel.interpreter.TransformLangParserVisitor;

public class CLVFRaiseErrorNode extends SimpleNode {
  public CLVFRaiseErrorNode(int id) {
    super(id);
  }

  public CLVFRaiseErrorNode(TransformLangParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(TransformLangParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
