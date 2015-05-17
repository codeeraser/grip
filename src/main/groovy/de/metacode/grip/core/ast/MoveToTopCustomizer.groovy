package de.metacode.grip.core.ast

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer

/**
 * Created by mloesch on 03.05.15.
 */
class MoveToTopCustomizer extends CompilationCustomizer {
    def methodName

    MoveToTopCustomizer(def methodName) {
        super(CompilePhase.SEMANTIC_ANALYSIS)
        this.methodName = methodName
    }

    @Override
    void call(SourceUnit sourceUnit, GeneratorContext generatorContext, ClassNode classNode) throws CompilationFailedException {
        def methods = classNode.getMethods()
        methods.each { MethodNode m ->
            m.code.each { Statement st ->
                if (!(st instanceof BlockStatement)) {
                    return
                }
                def doFirstStmt
                st.statements.each { Statement bst ->
                    if (bst instanceof ExpressionStatement) {
                        def ex = bst.expression
                        if (ex instanceof MethodCallExpression) {
                            if (ex.methodAsString.equals(methodName)) {
                                doFirstStmt = bst
                            }
                        }
                    }
                }
                if (doFirstStmt) {
                    st.statements.remove(doFirstStmt)
                    st.statements.add(0, doFirstStmt)
                }
            }
        }
    }
}
