package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.bytecode.BytecodeGenerator
import com.tambapps.marcel.parser.ast.FunctionCallNode
import com.tambapps.marcel.parser.ast.ScriptNode
import com.tambapps.marcel.parser.ast.TokenNodeType
import com.tambapps.marcel.parser.ast.TokenNodeWithValue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class MarcelCompilerTest {

    @Disabled
    @Test
    fun test() {
        val name = "Test"
        val node = ScriptNode(name, mutableListOf(
            FunctionCallNode("println").apply {
                addChild(TokenNodeWithValue(TokenNodeType.INTEGER, "8"))
            }
        ))
        val bytecodeGenerator = BytecodeGenerator()

        Files.write(Paths.get("/home/nfonkoua/Downloads/marcel/$name.class"), bytecodeGenerator.generate(node))
    }
}